package com.avh.practicas.vinculacion.service;

import com.avh.practicas.asignacion.entity.Asignacion;
import com.avh.practicas.asignacion.entity.EstadoAsignacion;
import com.avh.practicas.asignacion.repository.AsignacionRepository;
import com.avh.practicas.auth.entity.Usuario;
import com.avh.practicas.auth.repository.AuthUsuarioRepository;
import com.avh.practicas.empresa.entity.TutorEmpresarial;
import com.avh.practicas.empresa.repository.TutorEmpresarialRepository;
import com.avh.practicas.empresa.repository.EmpresaRepository;
import com.avh.practicas.empresa.entity.Empresa;
import com.avh.practicas.estudiante.entity.EstadoPractica;
import com.avh.practicas.estudiante.entity.Estudiante;
import com.avh.practicas.estudiante.entity.InstanciaPractica;
import com.avh.practicas.estudiante.repository.EstudianteRepository;
import com.avh.practicas.shared.enums.Rol;
import com.avh.practicas.shared.exception.AccesoNoAutorizadoException;
import com.avh.practicas.shared.exception.NegocioException;
import com.avh.practicas.shared.exception.RecursoNoEncontradoException;
import com.avh.practicas.vacante.entity.Vacante;
import com.avh.practicas.vacante.repository.VacanteRepository;
import com.avh.practicas.vinculacion.config.VinculacionProperties;
import com.avh.practicas.vinculacion.dto.*;
import com.avh.practicas.vinculacion.entity.*;
import com.avh.practicas.vinculacion.mediator.MediadorVinculacion;
import com.avh.practicas.vinculacion.port.AlmacenArchivosPort;
import com.avh.practicas.vinculacion.repository.*;
import com.avh.practicas.vinculacion.support.DocumentoVinculacionSupport;
import com.avh.practicas.vinculacion.support.ValidadorArchivoVinculacion;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VinculacionServiceImpl implements VinculacionService {

    private final AsignacionRepository asignacionRepository;
    private final ConvenioRepository convenioRepository;
    private final DocumentoPracticaRepository documentoPracticaRepository;
    private final PracticaVinculacionRepository practicaRepository;
    private final VacanteRepository vacanteRepository;
    private final EstudianteRepository estudianteRepository;
    private final EmpresaRepository empresaRepository;
    private final TutorEmpresarialRepository tutorRepository;
    private final AlmacenArchivosPort almacenArchivos;
    private final ValidadorArchivoVinculacion validadorArchivo;
    private final MediadorVinculacion mediadorVinculacion;
    private final AsignacionService asignacionService;
    private final VinculacionProperties vinculacionProperties;
    private final AuthUsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<VinculacionListadoResponse> listar(
            String busqueda,
            Long programaId,
            Long empresaId,
            EstadoAsignacion estado,
            Pageable pageable
    ) {
        String estadoParam = estado != null ? estado.name() : null;
        Long tutorId = resolverTutorAutenticado().map(TutorEmpresarial::getId).orElse(null);
        return asignacionRepository.buscarVinculaciones(
                        busqueda, programaId, empresaId, estadoParam, tutorId, pageable)
                .map(this::mapearListado);
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentosAsignacionResponse obtenerDocumentosAsignacion(Long asignacionId) {
        Asignacion asignacion = obtenerAsignacion(asignacionId);
        validarAccesoAsignacion(asignacion);
        ContextoVinculacion contexto = cargarContexto(asignacion);
        Long practicaId = asignacion.getInstanciaPracticaId();

        List<DocumentoPractica> documentos = documentoPracticaRepository.findByAsignacionIdOrderByFechaDesc(asignacionId);
        if (documentos.isEmpty() && practicaId != null) {
            documentos = documentoPracticaRepository.findByInstanciaPracticaIdOrderByFechaDesc(practicaId);
        }

        Optional<Convenio> convenio = convenioRepository.findByAsignacionId(asignacionId);
        List<DocumentoVinculacionDto> paneles = DocumentoVinculacionSupport.construirPaneles(documentos, convenio);

        return new DocumentosAsignacionResponse(
                asignacionId,
                practicaId,
                convenio.map(Convenio::getId).orElse(null),
                contexto.estudiante(),
                contexto.vacante(),
                contexto.tutorEmpresarial(),
                paneles
        );
    }

    @Override
    @Transactional
    public DocumentoCargadoResponse cargarDocumento(Long asignacionId, CategoriaDocumento categoria, MultipartFile archivo) {
        validarNoEsTutorSubiendo();
        DocumentoVinculacionSupport.AlmacenCategoria almacen = DocumentoVinculacionSupport.almacenPara(categoria);
        DocumentoCargadoResponse respuesta = registrarDocumento(
                asignacionId, archivo, categoria, almacen.toPort());

        if (categoria == CategoriaDocumento.CONVENIO_PRACTICA) {
            vincularConvenio(asignacionId, respuesta);
        }
        return respuesta;
    }

    @Override
    @Transactional
    public DocumentoCargadoResponse cargarCarta(Long asignacionId, MultipartFile archivo) {
        return cargarDocumento(asignacionId, CategoriaDocumento.CARTA_PRESENTACION, archivo);
    }

    @Override
    @Transactional
    public DocumentoCargadoResponse cargarConvenio(Long asignacionId, MultipartFile archivo) {
        return cargarDocumento(asignacionId, CategoriaDocumento.CONVENIO_PRACTICA, archivo);
    }

    @Override
    @Transactional(readOnly = true)
    public Resource descargarDocumento(Long documentoId) {
        DocumentoPractica documento = documentoPracticaRepository.findById(documentoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Documento no encontrado: " + documentoId));

        if (documento.getAsignacionId() != null) {
            Asignacion asignacion = obtenerAsignacion(documento.getAsignacionId());
            validarAccesoAsignacion(asignacion);
        }

        try {
            Path base = Paths.get(vinculacionProperties.getDirectorioUpload()).toAbsolutePath().normalize();
            Path archivo = Paths.get(documento.getUrl()).toAbsolutePath().normalize();
            if (!archivo.startsWith(base)) {
                throw new NegocioException("Ruta de archivo no permitida.");
            }
            Resource resource = new UrlResource(archivo.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new RecursoNoEncontradoException("No se pudo leer el archivo solicitado.");
            }
            return resource;
        } catch (NegocioException | RecursoNoEncontradoException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new NegocioException("No se pudo descargar el archivo: " + ex.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public String nombreDescargaDocumento(Long documentoId) {
        return documentoPracticaRepository.findById(documentoId)
                .map(DocumentoPractica::getNombre)
                .orElse("documento.pdf");
    }

    @Override
    @Transactional
    public void confirmarFirma(Long convenioId, RolFirmaConvenio rol) {
        Convenio convenio = obtenerConvenio(convenioId);
        validarAccesoConvenio(convenio);
        validarRolFirma(rol);
        LocalDateTime ahora = LocalDateTime.now();

        switch (rol) {
            case TUTOR_EMPRESARIAL -> {
                if (convenio.getFirmaTutorAt() != null) {
                    throw new NegocioException("La firma del tutor empresarial ya fue registrada.");
                }
                convenio.setFirmaTutorAt(ahora);
            }
            case ESTUDIANTE -> {
                if (convenio.getFirmaEstudianteAt() != null) {
                    throw new NegocioException("La firma del estudiante ya fue registrada.");
                }
                convenio.setFirmaEstudianteAt(ahora);
            }
        }

        convenioRepository.save(convenio);

        if (convenio.tieneFirmasCompletas()) {
            Long practicaId = convenio.getInstanciaPracticaId();
            if (practicaId == null) {
                throw new NegocioException("El convenio no tiene práctica asociada para completar la vinculación.");
            }
            validarPracticaPendienteDeVinculacion(practicaId);
            mediadorVinculacion.confirmarVinculacion(
                    practicaId,
                    convenio.getFechaInicio(),
                    convenio.getFechaFin()
            );
            marcarAsignacionVinculada(convenio.getAsignacionId());
        }
    }

    @Override
    @Transactional
    public void confirmarVinculacion(Long practicaId, ConfirmarVinculacionRequest request) {
        validarNoEsTutorSubiendo();
        Convenio convenio = convenioRepository.findByInstanciaPracticaId(practicaId)
                .orElseThrow(() -> new NegocioException(
                        "No existe convenio asociado a la práctica " + practicaId));

        validarFirmasCompletas(convenio);
        validarPracticaPendienteDeVinculacion(practicaId);

        if (request.fechaFin().isBefore(request.fechaInicio())) {
            throw new NegocioException("La fecha de fin no puede ser anterior a la fecha de inicio.");
        }

        InstanciaPractica practica = practicaRepository.findByIdConExpediente(practicaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Práctica no encontrada: " + practicaId));
        practica.setDocenteAsesorId(request.docenteAsesorId());
        practicaRepository.save(practica);

        mediadorVinculacion.confirmarVinculacion(practicaId, request.fechaInicio(), request.fechaFin());
        marcarAsignacionVinculada(convenio.getAsignacionId());
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentosPorCategoriaResponse listarDocumentosPorPractica(Long practicaId) {
        if (!practicaRepository.existsById(practicaId)) {
            throw new RecursoNoEncontradoException("No se encontró la práctica con id: " + practicaId);
        }

        List<DocumentoPractica> documentos = documentoPracticaRepository.findByInstanciaPracticaIdOrderByFechaDesc(practicaId);

        Map<String, List<DocumentoPracticaDto>> porCategoria = Arrays.stream(CategoriaDocumento.values())
                .collect(Collectors.toMap(
                        Enum::name,
                        categoria -> documentos.stream()
                                .filter(doc -> categoria == doc.getCategoria())
                                .map(DocumentoPracticaDto::desde)
                                .toList(),
                        (a, b) -> b,
                        LinkedHashMap::new
                ));

        return new DocumentosPorCategoriaResponse(practicaId, porCategoria);
    }

    @Override
    @Transactional(readOnly = true)
    public com.avh.practicas.estudiante.entity.Estudiante obtenerEstudiantePorPractica(Long practicaId) {
        InstanciaPractica practica = practicaRepository.findByIdConExpediente(practicaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Práctica no encontrada: " + practicaId));
        return practica.getExpediente().getEstudiante();
    }

    private DocumentoCargadoResponse registrarDocumento(
            Long asignacionId,
            MultipartFile archivo,
            CategoriaDocumento categoria,
            AlmacenArchivosPort.CategoriaAlmacen almacenCategoria
    ) {
        validadorArchivo.validar(archivo);

        Asignacion asignacion = obtenerAsignacion(asignacionId);
        Long practicaId = resolverPracticaId(asignacion);
        enriquecerPracticaDesdeVacante(practicaId, asignacion.getVacanteId());

        String url = almacenArchivos.guardar(asignacionId, almacenCategoria, archivo);

        DocumentoPractica documento = documentoPracticaRepository.save(DocumentoPractica.builder()
                .instanciaPracticaId(practicaId)
                .asignacionId(asignacionId)
                .nombre(archivo.getOriginalFilename())
                .url(url)
                .tipo(validadorArchivo.resolverTipo(archivo))
                .categoria(categoria)
                .build());

        if (asignacion.getEstado() == EstadoAsignacion.ASIGNADA) {
            asignacionService.cambiarEstado(asignacionId, EstadoAsignacion.EN_PROCESO_VINCULACION);
        }

        return new DocumentoCargadoResponse(
                documento.getId(),
                asignacionId,
                practicaId,
                categoria,
                url
        );
    }

    private void vincularConvenio(Long asignacionId, DocumentoCargadoResponse respuesta) {
        Asignacion asignacion = obtenerAsignacion(asignacionId);
        Long practicaId = respuesta.practicaId();
        Vacante vacante = obtenerVacante(asignacion.getVacanteId());

        Convenio convenio = convenioRepository.findByAsignacionId(asignacionId)
                .orElseGet(() -> crearConvenioBase(asignacion, practicaId, vacante.getEmpresaId()));

        convenio.setUrlDocumento(respuesta.url());
        convenio.setInstanciaPracticaId(practicaId);
        convenioRepository.save(convenio);
    }

    private VinculacionListadoResponse mapearListado(Asignacion asignacion) {
        ContextoVinculacion contexto = cargarContexto(asignacion);
        Long practicaId = asignacion.getInstanciaPracticaId();

        List<DocumentoPractica> documentos = documentoPracticaRepository.findByAsignacionIdOrderByFechaDesc(asignacion.getId());
        if (documentos.isEmpty() && practicaId != null) {
            documentos = documentoPracticaRepository.findByInstanciaPracticaIdOrderByFechaDesc(practicaId);
        }

        Optional<Convenio> convenio = convenioRepository.findByAsignacionId(asignacion.getId());
        List<DocumentoVinculacionDto> paneles = DocumentoVinculacionSupport.construirPaneles(documentos, convenio);

        return new VinculacionListadoResponse(
                asignacion.getId(),
                practicaId,
                asignacion.getEstado(),
                contexto.estudiante(),
                contexto.vacante(),
                paneles
        );
    }

    private ContextoVinculacion cargarContexto(Asignacion asignacion) {
        Estudiante estudiante = estudianteRepository.findById(asignacion.getEstudianteId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Estudiante no encontrado: " + asignacion.getEstudianteId()));

        Vacante vacante = obtenerVacante(asignacion.getVacanteId());
        Empresa empresa = empresaRepository.findById(vacante.getEmpresaId()).orElse(null);

        EstudianteVinculacionDto estudianteDto = new EstudianteVinculacionDto(
                estudiante.getId(),
                estudiante.getNombre(),
                estudiante.getIdentificacion(),
                estudiante.getPrograma() != null ? estudiante.getPrograma().getNombre() : null
        );

        VacanteVinculacionDto vacanteDto = new VacanteVinculacionDto(
                vacante.getId(),
                vacante.getCargo(),
                empresa != null ? empresa.getRazonSocial() : null
        );

        String nombreTutor = resolverNombreTutor(asignacion, vacante.getEmpresaId());

        return new ContextoVinculacion(estudianteDto, vacanteDto, nombreTutor);
    }

    private String resolverNombreTutor(Asignacion asignacion, Long empresaId) {
        if (asignacion.getInstanciaPracticaId() != null) {
            Optional<String> nombreDesdePractica = practicaRepository.findById(asignacion.getInstanciaPracticaId())
                    .filter(practica -> practica.getTutorId() != null)
                    .flatMap(practica -> tutorRepository.findById(practica.getTutorId()))
                    .map(TutorEmpresarial::getNombre);
            if (nombreDesdePractica.isPresent()) {
                return nombreDesdePractica.get();
            }
        }

        return tutorRepository.findByEmpresaIdAndActivoTrue(empresaId).stream()
                .findFirst()
                .map(TutorEmpresarial::getNombre)
                .orElse("Tutor no asignado");
    }

    private record ContextoVinculacion(EstudianteVinculacionDto estudiante, VacanteVinculacionDto vacante, String tutorEmpresarial) {
    }

    private Convenio crearConvenioBase(Asignacion asignacion, Long practicaId, Long empresaId) {
        InstanciaPractica practica = practicaRepository.findByIdConExpediente(practicaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Práctica no encontrada: " + practicaId));

        LocalDate inicio = LocalDate.now();
        LocalDate fin = inicio.plusWeeks(practica.getDuracionSemanas() != null ? practica.getDuracionSemanas() : 16);

        return Convenio.builder()
                .asignacionId(asignacion.getId())
                .instanciaPracticaId(practicaId)
                .empresaId(empresaId)
                .fechaInicio(inicio)
                .fechaFin(fin)
                .estado("ACTIVO")
                .build();
    }

    private void enriquecerPracticaDesdeVacante(Long practicaId, Long vacanteId) {
        Vacante vacante = obtenerVacante(vacanteId);
        InstanciaPractica practica = practicaRepository.findByIdConExpediente(practicaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Práctica no encontrada: " + practicaId));

        if (practica.getEmpresaId() == null) {
            practica.setEmpresaId(vacante.getEmpresaId());
        }
        if (practica.getTutorId() == null) {
            tutorRepository.findByEmpresaIdAndActivoTrue(vacante.getEmpresaId()).stream()
                    .findFirst()
                    .ifPresent(tutor -> practica.setTutorId(tutor.getId()));
        }
        practicaRepository.save(practica);
    }

    private Long resolverPracticaId(Asignacion asignacion) {
        if (asignacion.getInstanciaPracticaId() != null) {
            return asignacion.getInstanciaPracticaId();
        }

        InstanciaPractica practica = practicaRepository
                .findFirstByExpedienteEstudianteIdAndEstadoOrderByNumeroPracticaDesc(
                        asignacion.getEstudianteId(),
                        EstadoPractica.ASIGNADA_PENDIENTE_INICIO
                )
                .orElseThrow(() -> new NegocioException(
                        "El estudiante no tiene una práctica pendiente de inicio para vincular."));

        asignacion.setInstanciaPracticaId(practica.getId());
        asignacionRepository.save(asignacion);
        return practica.getId();
    }

    private void validarFirmasCompletas(Convenio convenio) {
        if (!convenio.tieneFirmasCompletas()) {
            throw new NegocioException("El convenio debe tener las firmas del tutor empresarial y del estudiante antes de vincular.");
        }
    }

    private void validarPracticaPendienteDeVinculacion(Long practicaId) {
        InstanciaPractica practica = practicaRepository.findById(practicaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Práctica no encontrada: " + practicaId));
        if (practica.getEstado() != EstadoPractica.ASIGNADA_PENDIENTE_INICIO) {
            throw new NegocioException("La práctica ya fue vinculada o no está pendiente de inicio.");
        }
    }

    private void marcarAsignacionVinculada(Long asignacionId) {
        if (asignacionId == null) {
            return;
        }
        asignacionRepository.findById(asignacionId).ifPresent(asignacion -> {
            if (asignacion.getEstado() != EstadoAsignacion.VINCULADA) {
                asignacionService.cambiarEstado(asignacionId, EstadoAsignacion.VINCULADA);
            }
        });
    }

    private Asignacion obtenerAsignacion(Long asignacionId) {
        return asignacionRepository.findById(asignacionId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Asignación no encontrada: " + asignacionId));
    }

    private Convenio obtenerConvenio(Long convenioId) {
        return convenioRepository.findById(convenioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Convenio no encontrado: " + convenioId));
    }

    private Vacante obtenerVacante(Long vacanteId) {
        return vacanteRepository.findById(vacanteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Vacante no encontrada: " + vacanteId));
    }

    private Optional<TutorEmpresarial> resolverTutorAutenticado() {
        Usuario usuario = obtenerUsuarioActual();
        if (usuario == null || usuario.getRol() != Rol.TUTOR_EMPRESARIAL) {
            return Optional.empty();
        }
        return tutorRepository.findByCorreo(usuario.getCorreo());
    }

    private Usuario obtenerUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        String correo = (String) auth.getPrincipal();
        return usuarioRepository.findByCorreo(correo).orElse(null);
    }

    private void validarNoEsTutorSubiendo() {
        if (resolverTutorAutenticado().isPresent()) {
            throw new AccesoNoAutorizadoException(
                    "El tutor empresarial no puede subir documentos ni activar la práctica; solo puede firmar el convenio.");
        }
    }

    private void validarRolFirma(RolFirmaConvenio rol) {
        Optional<TutorEmpresarial> tutor = resolverTutorAutenticado();
        if (tutor.isPresent() && rol != RolFirmaConvenio.TUTOR_EMPRESARIAL) {
            throw new AccesoNoAutorizadoException("Como tutor empresarial solo puede registrar su propia firma.");
        }
    }

    private void validarAccesoAsignacion(Asignacion asignacion) {
        Optional<TutorEmpresarial> tutor = resolverTutorAutenticado();
        if (tutor.isEmpty()) {
            return;
        }

        if (!tieneAccesoTutorAsignacion(asignacion, tutor.get().getId())) {
            throw new AccesoNoAutorizadoException("No tiene acceso a la vinculación de este estudiante.");
        }
    }

    private void validarAccesoConvenio(Convenio convenio) {
        Optional<TutorEmpresarial> tutor = resolverTutorAutenticado();
        if (tutor.isEmpty()) {
            return;
        }

        Long tutorId = tutor.get().getId();
        if (convenio.getInstanciaPracticaId() != null) {
            boolean esSuPractica = practicaRepository.findById(convenio.getInstanciaPracticaId())
                    .map(practica -> tutorId.equals(practica.getTutorId()))
                    .orElse(false);
            if (!esSuPractica) {
                throw new AccesoNoAutorizadoException("No puede firmar el convenio de este estudiante.");
            }
            return;
        }

        if (convenio.getAsignacionId() != null) {
            Asignacion asignacion = obtenerAsignacion(convenio.getAsignacionId());
            if (!tieneAccesoTutorAsignacion(asignacion, tutorId)) {
                throw new AccesoNoAutorizadoException("No puede firmar el convenio de este estudiante.");
            }
        }
    }

    private boolean tieneAccesoTutorAsignacion(Asignacion asignacion, Long tutorId) {
        if (asignacion.getInstanciaPracticaId() != null) {
            return practicaRepository.findById(asignacion.getInstanciaPracticaId())
                    .map(practica -> tutorId.equals(practica.getTutorId()))
                    .orElse(false);
        }
        return practicaRepository.existsByExpedienteEstudianteIdAndTutorId(asignacion.getEstudianteId(), tutorId);
    }
}
