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
import com.avh.practicas.estudiante.entity.DocenteAsesor;
import com.avh.practicas.estudiante.entity.EstadoPractica;
import com.avh.practicas.estudiante.entity.Estudiante;
import com.avh.practicas.estudiante.entity.InstanciaPractica;
import com.avh.practicas.estudiante.repository.DocenteAsesorRepository;
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
import org.springframework.data.domain.PageImpl;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
    private final DocenteAsesorRepository docenteRepository;
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
        Optional<Estudiante> estudianteAutenticado = resolverEstudianteAutenticado();
        if (estudianteAutenticado.isPresent()) {
            return listarPorPracticasEstudiante(estudianteAutenticado.get(), pageable);
        }

        String estadoParam = estado != null ? estado.name() : null;
        Optional<TutorEmpresarial> tutorAutenticado = resolverTutorAutenticado();
        Long tutorId = tutorAutenticado.map(TutorEmpresarial::getId).orElse(null);
        Long estudianteId = null;
        Optional<Empresa> empresaAutenticada = resolverEmpresaAutenticada();
        if (empresaAutenticada.isPresent()) {
            empresaId = empresaAutenticada.get().getId();
        } else if (tutorAutenticado.isPresent()) {
            Empresa empresaTutor = tutorAutenticado.get().getEmpresa();
            if (empresaTutor != null) {
                empresaId = empresaTutor.getId();
            }
        }
        return asignacionRepository.buscarVinculaciones(
                        busqueda, programaId, empresaId, estadoParam, tutorId, estudianteId, pageable)
                .map(this::mapearListado);
    }

    private Page<VinculacionListadoResponse> listarPorPracticasEstudiante(Estudiante estudiante, Pageable pageable) {
        Long estudianteId = estudiante.getId();
        List<VinculacionListadoResponse> items = new ArrayList<>();
        Set<Long> practicasIncluidas = new HashSet<>();

        for (InstanciaPractica practica : practicaRepository.findByExpedienteEstudianteIdOrderByNumeroPracticaDesc(estudianteId)) {
            practicasIncluidas.add(practica.getId());
            Optional<Asignacion> asignacion = asignacionRepository.findFirstByInstanciaPracticaIdAndEstadoNot(
                    practica.getId(), EstadoAsignacion.CANCELADA);
            items.add(asignacion.map(this::mapearListado).orElseGet(() -> mapearListadoDesdePractica(practica, estudiante)));
        }

        for (Asignacion asignacion : asignacionRepository.findByEstudianteId(estudianteId)) {
            if (asignacion.getEstado() == EstadoAsignacion.CANCELADA) {
                continue;
            }
            Long practicaId = asignacion.getInstanciaPracticaId();
            if (practicaId == null || !practicasIncluidas.contains(practicaId)) {
                items.add(mapearListado(asignacion));
            }
        }

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), items.size());
        List<VinculacionListadoResponse> pageContent = start >= items.size() ? List.of() : items.subList(start, end);
        return new PageImpl<>(pageContent, pageable, items.size());
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
        if (convenio.isEmpty() && practicaId != null) {
            convenio = convenioRepository.findByInstanciaPracticaId(practicaId);
        }
        List<DocumentoVinculacionDto> paneles = DocumentoVinculacionSupport.construirPaneles(documentos, convenio);

        Long docenteAsesorId = null;
        LocalDate fechaInicio = null;
        LocalDate fechaFin = null;
        EstadoPractica estadoPractica = null;

        Optional<InstanciaPractica> practicaOpt = practicaId != null ? practicaRepository.findById(practicaId) : Optional.empty();
        if (practicaOpt.isPresent()) {
            InstanciaPractica practica = practicaOpt.get();
            docenteAsesorId = practica.getDocenteAsesorId();
            fechaInicio = practica.getFechaInicio();
            fechaFin = practica.getFechaFin();
            estadoPractica = practica.getEstado();
        }

        return new DocumentosAsignacionResponse(
                asignacionId,
                practicaId,
                convenio.map(Convenio::getId).orElse(null),
                contexto.estudiante(),
                contexto.vacante(),
                contexto.tutorEmpresarial(),
                docenteAsesorId,
                fechaInicio,
                fechaFin,
                asignacion.getEstado(),
                estadoPractica,
                paneles
        );
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentosAsignacionResponse obtenerDocumentosPractica(Long practicaId) {
        InstanciaPractica practica = practicaRepository.findByIdConExpediente(practicaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Práctica no encontrada: " + practicaId));
        validarAccesoPractica(practicaId);

        Optional<Asignacion> asignacionOpt = asignacionRepository.findFirstByInstanciaPracticaIdAndEstadoNot(
                practicaId, EstadoAsignacion.CANCELADA);
        if (asignacionOpt.isPresent()) {
            return obtenerDocumentosAsignacion(asignacionOpt.get().getId());
        }

        Estudiante estudiante = practica.getExpediente().getEstudiante();
        List<DocumentoPractica> documentos = documentoPracticaRepository.findByInstanciaPracticaIdOrderByFechaDesc(practicaId);
        Optional<Convenio> convenio = convenioRepository.findByInstanciaPracticaId(practicaId);
        List<DocumentoVinculacionDto> paneles = DocumentoVinculacionSupport.construirPaneles(documentos, convenio);

        EstudianteVinculacionDto estudianteDto = new EstudianteVinculacionDto(
                estudiante.getId(),
                estudiante.getNombre(),
                estudiante.getIdentificacion(),
                estudiante.getPrograma() != null ? estudiante.getPrograma().getNombre() : null
        );

        return new DocumentosAsignacionResponse(
                null,
                practicaId,
                convenio.map(Convenio::getId).orElse(null),
                estudianteDto,
                vacanteDesdePractica(practica),
                resolverNombreTutorPractica(practica),
                practica.getDocenteAsesorId(),
                practica.getFechaInicio(),
                practica.getFechaFin(),
                EstadoAsignacion.ASIGNADA,
                practica.getEstado(),
                paneles
        );
    }

    @Override
    @Transactional
    public DocumentoCargadoResponse cargarDocumento(Long asignacionId, CategoriaDocumento categoria, MultipartFile archivo) {
        validarNoEsTutorSubiendo();
        validarAccesoAsignacion(obtenerAsignacion(asignacionId));
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
    public DocumentoCargadoResponse cargarDocumentoPorPractica(
            Long practicaId,
            CategoriaDocumento categoria,
            MultipartFile archivo
    ) {
        validarNoEsTutorSubiendo();
        validarAccesoPractica(practicaId);

        Optional<Asignacion> asignacion = asignacionRepository.findFirstByInstanciaPracticaIdAndEstadoNot(
                practicaId, EstadoAsignacion.CANCELADA);
        if (asignacion.isPresent()) {
            return cargarDocumento(asignacion.get().getId(), categoria, archivo);
        }

        DocumentoVinculacionSupport.AlmacenCategoria almacen = DocumentoVinculacionSupport.almacenPara(categoria);
        DocumentoCargadoResponse respuesta = registrarDocumentoPorPractica(
                practicaId, archivo, categoria, almacen.toPort());

        if (categoria == CategoriaDocumento.CONVENIO_PRACTICA) {
            vincularConvenioPorPractica(practicaId, respuesta);
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
        } else if (documento.getInstanciaPracticaId() != null) {
            validarAccesoPractica(documento.getInstanciaPracticaId());
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
        DocumentoPractica documento = documentoPracticaRepository.findById(documentoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Documento no encontrado: " + documentoId));
        if (documento.getAsignacionId() != null) {
            validarAccesoAsignacion(obtenerAsignacion(documento.getAsignacionId()));
        } else if (documento.getInstanciaPracticaId() != null) {
            validarAccesoPractica(documento.getInstanciaPracticaId());
        }
        return documento.getNombre();
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
                vincularTutorFirmanteEnPractica(convenio);
            }
            case ESTUDIANTE -> {
                if (convenio.getFirmaEstudianteAt() != null) {
                    throw new NegocioException("La firma del estudiante ya fue registrada.");
                }
                convenio.setFirmaEstudianteAt(ahora);
            }
        }

        convenioRepository.save(convenio);
    }

    @Override
    @Transactional
    public void confirmarVinculacion(Long practicaId, ConfirmarVinculacionRequest request) {
        validarSoloCoordPractica();
        Convenio convenio = convenioRepository.findByInstanciaPracticaId(practicaId)
                .orElseThrow(() -> new NegocioException(
                        "No existe convenio asociado a la práctica " + practicaId));

        validarFirmasCompletas(convenio);
        validarPracticaPendienteDeVinculacion(practicaId);

        if (request.fechaFin().isBefore(request.fechaInicio())) {
            throw new NegocioException("La fecha de fin no puede ser anterior a la fecha de inicio.");
        }

        enriquecerPracticaParaActivacion(practicaId, convenio);

        InstanciaPractica practica = practicaRepository.findByIdConExpediente(practicaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Práctica no encontrada: " + practicaId));

        if (practica.getEmpresaId() == null || practica.getTutorId() == null) {
            throw new NegocioException(
                    "La práctica no tiene empresa o tutor empresarial configurados. "
                            + "Verifique que la vacante tenga empresa asignada y que exista un tutor empresarial vinculado.");
        }

        practica.setDocenteAsesorId(request.docenteAsesorId());
        practicaRepository.save(practica);

        mediadorVinculacion.confirmarVinculacion(practicaId, request.fechaInicio(), request.fechaFin());
        sincronizarConvenioConAsignacionActiva(practicaId, convenio.getAsignacionId());
        marcarAsignacionVinculada(practicaId, convenio.getAsignacionId());
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentosPorCategoriaResponse listarDocumentosPorPractica(Long practicaId) {
        if (!practicaRepository.existsById(practicaId)) {
            throw new RecursoNoEncontradoException("No se encontró la práctica con id: " + practicaId);
        }

        validarAccesoPractica(practicaId);
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
        validarAccesoPractica(practicaId);
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
        } else if (asignacion.getEstado() == EstadoAsignacion.CANCELADA) {
            resolverAsignacionActiva(practicaId, asignacionId).ifPresent(activa -> {
                if (activa.getEstado() == EstadoAsignacion.ASIGNADA) {
                    asignacionService.cambiarEstado(activa.getId(), EstadoAsignacion.EN_PROCESO_VINCULACION);
                }
            });
        }

        return new DocumentoCargadoResponse(
                documento.getId(),
                asignacionId,
                practicaId,
                categoria,
                url
        );
    }

    private DocumentoCargadoResponse registrarDocumentoPorPractica(
            Long practicaId,
            MultipartFile archivo,
            CategoriaDocumento categoria,
            AlmacenArchivosPort.CategoriaAlmacen almacenCategoria
    ) {
        validadorArchivo.validar(archivo);

        InstanciaPractica practica = practicaRepository.findByIdConExpediente(practicaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Práctica no encontrada: " + practicaId));

        String url = almacenArchivos.guardar(practicaId, almacenCategoria, archivo);

        DocumentoPractica documento = documentoPracticaRepository.save(DocumentoPractica.builder()
                .instanciaPracticaId(practicaId)
                .asignacionId(null)
                .nombre(archivo.getOriginalFilename())
                .url(url)
                .tipo(validadorArchivo.resolverTipo(archivo))
                .categoria(categoria)
                .build());

        return new DocumentoCargadoResponse(
                documento.getId(),
                null,
                practicaId,
                categoria,
                url
        );
    }

    private void vincularConvenioPorPractica(Long practicaId, DocumentoCargadoResponse respuesta) {
        InstanciaPractica practica = practicaRepository.findByIdConExpediente(practicaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Práctica no encontrada: " + practicaId));

        Long empresaId = practica.getEmpresaId();
        if (empresaId == null) {
            throw new NegocioException(
                    "La práctica aún no tiene empresa asignada. Solicita al coordinador que complete la vinculación.");
        }

        LocalDate inicio = LocalDate.now();
        LocalDate fin = inicio.plusWeeks(practica.getDuracionSemanas() != null ? practica.getDuracionSemanas() : 16);

        Convenio convenio = convenioRepository.findByInstanciaPracticaId(practicaId)
                .orElseGet(() -> Convenio.builder()
                        .instanciaPracticaId(practicaId)
                        .empresaId(empresaId)
                        .fechaInicio(inicio)
                        .fechaFin(fin)
                        .estado("ACTIVO")
                        .build());

        convenio.setUrlDocumento(respuesta.url());
        convenio.setInstanciaPracticaId(practicaId);
        convenioRepository.save(convenio);
    }

    private void vincularConvenio(Long asignacionId, DocumentoCargadoResponse respuesta) {
        Asignacion asignacion = obtenerAsignacion(asignacionId);
        Long practicaId = respuesta.practicaId();
        Vacante vacante = obtenerVacante(asignacion.getVacanteId());

        Convenio convenio = convenioRepository.findByAsignacionId(asignacionId)
                .or(() -> convenioRepository.findByInstanciaPracticaId(practicaId))
                .orElseGet(() -> crearConvenioBase(asignacion, practicaId, vacante.getEmpresaId()));

        convenio.setAsignacionId(asignacionId);
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
        if (convenio.isEmpty() && practicaId != null) {
            convenio = convenioRepository.findByInstanciaPracticaId(practicaId);
        }
        List<DocumentoVinculacionDto> paneles = DocumentoVinculacionSupport.construirPaneles(documentos, convenio);

        Integer numeroPractica = null;
        EstadoPractica estadoPractica = null;
        if (practicaId != null) {
            Optional<InstanciaPractica> practicaOpt = practicaRepository.findById(practicaId);
            if (practicaOpt.isPresent()) {
                numeroPractica = practicaOpt.get().getNumeroPractica();
                estadoPractica = practicaOpt.get().getEstado();
            }
        }

        return new VinculacionListadoResponse(
                asignacion.getId(),
                practicaId,
                asignacion.getEstado(),
                contexto.estudiante(),
                contexto.vacante(),
                paneles,
                numeroPractica,
                estadoPractica,
                convenio.map(c -> c.getFirmaEstudianteAt() != null).orElse(false)
        );
    }

    private VinculacionListadoResponse mapearListadoDesdePractica(InstanciaPractica practica, Estudiante estudiante) {
        Long practicaId = practica.getId();
        List<DocumentoPractica> documentos = documentoPracticaRepository.findByInstanciaPracticaIdOrderByFechaDesc(practicaId);
        Optional<Convenio> convenio = convenioRepository.findByInstanciaPracticaId(practicaId);
        List<DocumentoVinculacionDto> paneles = DocumentoVinculacionSupport.construirPaneles(documentos, convenio);

        EstudianteVinculacionDto estudianteDto = new EstudianteVinculacionDto(
                estudiante.getId(),
                estudiante.getNombre(),
                estudiante.getIdentificacion(),
                estudiante.getPrograma() != null ? estudiante.getPrograma().getNombre() : null
        );

        return new VinculacionListadoResponse(
                null,
                practicaId,
                EstadoAsignacion.ASIGNADA,
                estudianteDto,
                vacanteDesdePractica(practica),
                paneles,
                practica.getNumeroPractica(),
                practica.getEstado(),
                convenio.map(c -> c.getFirmaEstudianteAt() != null).orElse(false)
        );
    }

    private VacanteVinculacionDto vacanteDesdePractica(InstanciaPractica practica) {
        String empresa = null;
        if (practica.getEmpresaId() != null) {
            empresa = empresaRepository.findById(practica.getEmpresaId())
                    .map(Empresa::getRazonSocial)
                    .orElse(null);
        }
        return new VacanteVinculacionDto(null, practica.getNombre(), empresa);
    }

    private String resolverNombreTutorPractica(InstanciaPractica practica) {
        if (practica.getTutorId() == null) {
            return null;
        }
        return tutorRepository.findById(practica.getTutorId())
                .map(TutorEmpresarial::getNombre)
                .orElse(null);
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
            asignarTutorDesdeEmpresa(practica, vacante.getEmpresaId());
        }
        practicaRepository.save(practica);
    }

    private void enriquecerPracticaParaActivacion(Long practicaId, Convenio convenio) {
        Long vacanteId = resolverVacanteId(convenio);
        if (vacanteId != null) {
            enriquecerPracticaDesdeVacante(practicaId, vacanteId);
        }

        InstanciaPractica practica = practicaRepository.findByIdConExpediente(practicaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Práctica no encontrada: " + practicaId));

        Long empresaId = practica.getEmpresaId() != null ? practica.getEmpresaId() : convenio.getEmpresaId();
        if (practica.getEmpresaId() == null && empresaId != null) {
            practica.setEmpresaId(empresaId);
        }
        if (practica.getTutorId() == null && empresaId != null) {
            asignarTutorDesdeEmpresa(practica, empresaId);
        }
        practicaRepository.save(practica);
    }

    private void asignarTutorDesdeEmpresa(InstanciaPractica practica, Long empresaId) {
        tutorRepository.findByEmpresaIdAndActivoTrue(empresaId).stream()
                .findFirst()
                .or(() -> tutorRepository.findByEmpresaId(empresaId).stream().findFirst())
                .ifPresent(tutor -> practica.setTutorId(tutor.getId()));
    }

    private void vincularTutorFirmanteEnPractica(Convenio convenio) {
        Long practicaId = convenio.getInstanciaPracticaId();
        if (practicaId == null) {
            return;
        }

        resolverTutorAutenticado().ifPresent(tutor -> {
            InstanciaPractica practica = practicaRepository.findById(practicaId).orElse(null);
            if (practica == null) {
                return;
            }
            practica.setTutorId(tutor.getId());
            if (practica.getEmpresaId() == null && convenio.getEmpresaId() != null) {
                practica.setEmpresaId(convenio.getEmpresaId());
            }
            practicaRepository.save(practica);
        });
    }

    private Long resolverVacanteId(Convenio convenio) {
        if (convenio.getAsignacionId() == null) {
            return null;
        }
        return obtenerAsignacion(convenio.getAsignacionId()).getVacanteId();
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

    private void marcarAsignacionVinculada(Long practicaId, Long asignacionIdConvenio) {
        Optional<Asignacion> asignacionActiva = resolverAsignacionActiva(practicaId, asignacionIdConvenio);
        if (asignacionActiva.isEmpty()) {
            return;
        }
        Asignacion asignacion = asignacionActiva.get();
        if (asignacion.getEstado() != EstadoAsignacion.VINCULADA) {
            asignacionService.cambiarEstado(asignacion.getId(), EstadoAsignacion.VINCULADA);
        }
    }

    private void sincronizarConvenioConAsignacionActiva(Long practicaId, Long asignacionIdConvenio) {
        resolverAsignacionActiva(practicaId, asignacionIdConvenio).ifPresent(asignacion ->
                convenioRepository.findByInstanciaPracticaId(practicaId).ifPresent(convenio -> {
                    if (!asignacion.getId().equals(convenio.getAsignacionId())) {
                        convenio.setAsignacionId(asignacion.getId());
                        convenioRepository.save(convenio);
                    }
                }));
    }

    private Optional<Asignacion> resolverAsignacionActiva(Long practicaId, Long asignacionIdConvenio) {
        if (asignacionIdConvenio != null) {
            Optional<Asignacion> asignacion = asignacionRepository.findById(asignacionIdConvenio);
            if (asignacion.isPresent() && asignacion.get().getEstado() != EstadoAsignacion.CANCELADA) {
                return asignacion;
            }
        }
        return asignacionRepository.findFirstByInstanciaPracticaIdAndEstadoNot(
                        practicaId, EstadoAsignacion.CANCELADA)
                .filter(a -> a.getEstado() == EstadoAsignacion.ASIGNADA
                        || a.getEstado() == EstadoAsignacion.EN_PROCESO_VINCULACION
                        || a.getEstado() == EstadoAsignacion.VINCULADA);
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
        return tutorRepository.findByUsuarioId(usuario.getId())
                .or(() -> tutorRepository.findByCorreoIgnoreCase(usuario.getCorreo()));
    }

    private Optional<DocenteAsesor> resolverDocenteAutenticado() {
        Usuario usuario = obtenerUsuarioActual();
        if (usuario == null || usuario.getRol() != Rol.DOCENTE_ASESOR) {
            return Optional.empty();
        }
        return docenteRepository.findByUsuario_Id(usuario.getId())
                .or(() -> docenteRepository.findByCorreoIgnoreCase(usuario.getCorreo()));
    }

    private Optional<Empresa> resolverEmpresaAutenticada() {
        Usuario usuario = obtenerUsuarioActual();
        if (usuario == null || usuario.getRol() != Rol.EMPRESA) {
            return Optional.empty();
        }
        return empresaRepository.findByUsuarioId(usuario.getId());
    }

    private Optional<Estudiante> resolverEstudianteAutenticado() {
        Usuario usuario = obtenerUsuarioActual();
        if (usuario == null || usuario.getRol() != Rol.ESTUDIANTE) {
            return Optional.empty();
        }
        return estudianteRepository.findByCorreoIgnoreCase(usuario.getCorreo())
                .or(() -> estudianteRepository.findByUsuario_Id(usuario.getId()));
    }

    private Usuario obtenerUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        String correo = (String) auth.getPrincipal();
        return usuarioRepository.findByCorreoIgnoreCase(correo).orElse(null);
    }

    private void validarNoEsTutorSubiendo() {
        if (resolverTutorAutenticado().isPresent()) {
            throw new AccesoNoAutorizadoException(
                    "El tutor empresarial no puede subir documentos ni activar la práctica; solo puede firmar el convenio.");
        }
    }

    private void validarSoloCoordPractica() {
        Usuario usuario = obtenerUsuarioActual();
        if (usuario == null || usuario.getRol() != Rol.COORD_PRACTICA) {
            throw new AccesoNoAutorizadoException(
                    "Solo Coordinación de Prácticas puede activar la práctica.");
        }
    }

    private void validarRolFirma(RolFirmaConvenio rol) {
        Optional<TutorEmpresarial> tutor = resolverTutorAutenticado();
        if (tutor.isPresent() && rol != RolFirmaConvenio.TUTOR_EMPRESARIAL) {
            throw new AccesoNoAutorizadoException("Como tutor empresarial solo puede registrar su propia firma.");
        }

        Optional<Estudiante> estudiante = resolverEstudianteAutenticado();
        if (estudiante.isPresent() && rol != RolFirmaConvenio.ESTUDIANTE) {
            throw new AccesoNoAutorizadoException("Como estudiante solo puede registrar su propia firma.");
        }
    }

    private void validarAccesoAsignacion(Asignacion asignacion) {
        Optional<Empresa> empresa = resolverEmpresaAutenticada();
        if (empresa.isPresent()) {
            Vacante vacante = obtenerVacante(asignacion.getVacanteId());
            if (!empresa.get().getId().equals(vacante.getEmpresaId())) {
                throw new AccesoNoAutorizadoException("No tiene acceso a la vinculacion de este estudiante.");
            }
            return;
        }

        Optional<DocenteAsesor> docente = resolverDocenteAutenticado();
        if (docente.isPresent()) {
            Long docenteId = docente.get().getId();
            boolean asignada = asignacion.getInstanciaPracticaId() != null
                    && practicaRepository.findById(asignacion.getInstanciaPracticaId())
                    .map(p -> docenteId.equals(p.getDocenteAsesorId()))
                    .orElse(false);
            if (!asignada) {
                throw new AccesoNoAutorizadoException("No tiene acceso a la vinculacion de este estudiante.");
            }
            return;
        }

        Optional<TutorEmpresarial> tutor = resolverTutorAutenticado();
        if (tutor.isPresent()) {
            if (!tieneAccesoTutorAsignacion(asignacion, tutor.get().getId())) {
                throw new AccesoNoAutorizadoException("No tiene acceso a la vinculación de este estudiante.");
            }
            return;
        }

        Optional<Estudiante> estudiante = resolverEstudianteAutenticado();
        if (estudiante.isPresent() && !estudiante.get().getId().equals(asignacion.getEstudianteId())) {
            throw new AccesoNoAutorizadoException("No tiene acceso a la vinculación de este estudiante.");
        }
    }

    private void validarAccesoPractica(Long practicaId) {
        Usuario usuario = obtenerUsuarioActual();
        if (usuario == null || usuario.getRol() == Rol.ADMIN || usuario.getRol() == Rol.COORD_PRACTICA) {
            return;
        }
        InstanciaPractica practica = practicaRepository.findByIdConExpediente(practicaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Practica no encontrada: " + practicaId));
        if (usuario.getRol() == Rol.ESTUDIANTE) {
            Long propioId = resolverEstudianteAutenticado()
                    .map(Estudiante::getId)
                    .orElseThrow(() -> new AccesoNoAutorizadoException("Perfil de estudiante no encontrado."));
            if (!propioId.equals(practica.getExpediente().getEstudiante().getId())) {
                throw new AccesoNoAutorizadoException("No tiene acceso a documentos de otra practica.");
            }
        }
        if (usuario.getRol() == Rol.DOCENTE_ASESOR) {
            Long docenteId = resolverDocenteAutenticado()
                    .map(DocenteAsesor::getId)
                    .orElseThrow(() -> new AccesoNoAutorizadoException("Perfil de docente no encontrado."));
            if (!docenteId.equals(practica.getDocenteAsesorId())) {
                throw new AccesoNoAutorizadoException("No tiene acceso a documentos de estudiantes no asignados.");
            }
        }
        if (usuario.getRol() == Rol.EMPRESA) {
            Long empresaId = resolverEmpresaAutenticada()
                    .map(Empresa::getId)
                    .orElseThrow(() -> new AccesoNoAutorizadoException("Empresa asociada no encontrada."));
            if (!empresaId.equals(practica.getEmpresaId())) {
                throw new AccesoNoAutorizadoException("No tiene acceso a documentos fuera de su empresa.");
            }
        }
        if (usuario.getRol() == Rol.TUTOR_EMPRESARIAL) {
            Long tutorId = resolverTutorAutenticado()
                    .map(TutorEmpresarial::getId)
                    .orElseThrow(() -> new AccesoNoAutorizadoException("Tutor empresarial no encontrado."));
            if (!tutorId.equals(practica.getTutorId())) {
                throw new AccesoNoAutorizadoException("No tiene acceso a documentos fuera de su empresa.");
            }
        }
    }

    private void validarAccesoConvenio(Convenio convenio) {
        Optional<TutorEmpresarial> tutor = resolverTutorAutenticado();
        if (tutor.isPresent()) {
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
            return;
        }

        Optional<Estudiante> estudiante = resolverEstudianteAutenticado();
        if (estudiante.isPresent() && !tieneAccesoEstudianteConvenio(convenio, estudiante.get().getId())) {
            throw new AccesoNoAutorizadoException("No puede firmar el convenio de este estudiante.");
        }
    }

    private boolean tieneAccesoTutorAsignacion(Asignacion asignacion, Long tutorId) {
        TutorEmpresarial tutor = tutorRepository.findById(tutorId).orElse(null);
        if (tutor == null || tutor.getEmpresa() == null) {
            return false;
        }
        Long empresaTutorId = tutor.getEmpresa().getId();

        Vacante vacante = vacanteRepository.findById(asignacion.getVacanteId()).orElse(null);
        if (vacante == null || !empresaTutorId.equals(vacante.getEmpresaId())) {
            return false;
        }

        if (asignacion.getInstanciaPracticaId() != null) {
            return practicaRepository.findById(asignacion.getInstanciaPracticaId())
                    .map(practica -> tutorId.equals(practica.getTutorId()))
                    .orElse(false);
        }

        return true;
    }

    private boolean tieneAccesoEstudianteConvenio(Convenio convenio, Long estudianteId) {
        if (convenio.getAsignacionId() != null) {
            return obtenerAsignacion(convenio.getAsignacionId()).getEstudianteId().equals(estudianteId);
        }
        if (convenio.getInstanciaPracticaId() != null) {
            return practicaRepository.findByIdConExpediente(convenio.getInstanciaPracticaId())
                    .map(practica -> estudianteId.equals(practica.getExpediente().getEstudiante().getId()))
                    .orElse(false);
        }
        return false;
    }

    @Override
    @Transactional
    public void asignarDocenteAsesor(Long asignacionId, Long docenteAsesorId) {
        validarNoEsTutorSubiendo();
        Asignacion asignacion = obtenerAsignacion(asignacionId);
        Long practicaId = resolverPracticaId(asignacion);

        InstanciaPractica practica = practicaRepository.findByIdConExpediente(practicaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Práctica no encontrada: " + practicaId));

        practica.setDocenteAsesorId(docenteAsesorId);
        practicaRepository.save(practica);
    }
}
