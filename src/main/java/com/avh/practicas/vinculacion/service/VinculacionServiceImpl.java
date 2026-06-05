package com.avh.practicas.vinculacion.service;

import com.avh.practicas.asignacion.entity.Asignacion;
import com.avh.practicas.asignacion.entity.EstadoAsignacion;
import com.avh.practicas.asignacion.repository.AsignacionRepository;
import com.avh.practicas.empresa.repository.TutorEmpresarialRepository;
import com.avh.practicas.estudiante.entity.EstadoPractica;
import com.avh.practicas.estudiante.entity.InstanciaPractica;
import com.avh.practicas.shared.exception.NegocioException;
import com.avh.practicas.shared.exception.RecursoNoEncontradoException;
import com.avh.practicas.vacante.entity.Vacante;
import com.avh.practicas.vacante.repository.VacanteRepository;
import com.avh.practicas.vinculacion.dto.ConfirmarVinculacionRequest;
import com.avh.practicas.vinculacion.dto.DocumentoCargadoResponse;
import com.avh.practicas.vinculacion.dto.DocumentoPracticaDto;
import com.avh.practicas.vinculacion.dto.DocumentosPorCategoriaResponse;
import com.avh.practicas.vinculacion.entity.*;
import com.avh.practicas.vinculacion.mediator.MediadorVinculacion;
import com.avh.practicas.vinculacion.port.AlmacenArchivosPort;
import com.avh.practicas.vinculacion.repository.*;
import com.avh.practicas.vinculacion.support.ValidadorArchivoVinculacion;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VinculacionServiceImpl implements VinculacionService {

    private final AsignacionRepository asignacionRepository;
    private final ConvenioRepository convenioRepository;
    private final DocumentoPracticaRepository documentoPracticaRepository;
    private final PracticaVinculacionRepository practicaRepository;
    private final VacanteRepository vacanteRepository;
    private final TutorEmpresarialRepository tutorRepository;
    private final AlmacenArchivosPort almacenArchivos;
    private final ValidadorArchivoVinculacion validadorArchivo;
    private final MediadorVinculacion mediadorVinculacion;
    private final AsignacionService asignacionService;

    @Override
    @Transactional
    public DocumentoCargadoResponse cargarCarta(Long asignacionId, MultipartFile archivo) {
        return registrarDocumento(asignacionId, archivo, CategoriaDocumento.VINCULACION, AlmacenArchivosPort.CategoriaAlmacen.CARTA);
    }

    @Override
    @Transactional
    public DocumentoCargadoResponse cargarConvenio(Long asignacionId, MultipartFile archivo) {
        DocumentoCargadoResponse respuesta = registrarDocumento(
                asignacionId, archivo, CategoriaDocumento.CONVENIO, AlmacenArchivosPort.CategoriaAlmacen.CONVENIO);

        Asignacion asignacion = obtenerAsignacion(asignacionId);
        Long practicaId = resolverPracticaId(asignacion);
        Vacante vacante = obtenerVacante(asignacion.getVacanteId());

        Convenio convenio = convenioRepository.findByAsignacionId(asignacionId)
                .orElseGet(() -> crearConvenioBase(asignacion, practicaId, vacante.getEmpresaId()));

        convenio.setUrlDocumento(respuesta.url());
        convenio.setInstanciaPracticaId(practicaId);
        convenioRepository.save(convenio);

        return respuesta;
    }

    @Override
    @Transactional
    public void confirmarFirma(Long convenioId, RolFirmaConvenio rol) {
        Convenio convenio = obtenerConvenio(convenioId);
        LocalDateTime ahora = LocalDateTime.now();

        switch (rol) {
            case COORDINADOR -> {
                if (convenio.getFirmaCoordinadorAt() != null) {
                    throw new NegocioException("La firma del coordinador ya fue registrada.");
                }
                convenio.setFirmaCoordinadorAt(ahora);
            }
            case TUTOR -> {
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

        if (convenio.tieneTresFirmas()) {
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
        Convenio convenio = convenioRepository.findByInstanciaPracticaId(practicaId)
                .orElseThrow(() -> new NegocioException(
                        "No existe convenio asociado a la práctica " + practicaId));

        validarTresFirmas(convenio);
        validarPracticaPendienteDeVinculacion(practicaId);

        if (request.fechaFin().isBefore(request.fechaInicio())) {
            throw new NegocioException("La fecha de fin no puede ser anterior a la fecha de inicio.");
        }

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

    private void validarTresFirmas(Convenio convenio) {
        if (!convenio.tieneTresFirmas()) {
            throw new NegocioException("El convenio debe tener las tres firmas confirmadas antes de vincular.");
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
}
