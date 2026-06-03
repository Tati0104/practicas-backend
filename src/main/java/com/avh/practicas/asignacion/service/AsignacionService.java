package com.avh.practicas.asignacion.service;

import com.avh.practicas.asignacion.dto.AsignacionDetalleResponse;
import com.avh.practicas.asignacion.dto.AsignacionRequest;
import com.avh.practicas.asignacion.dto.AsignacionResponse;
import com.avh.practicas.asignacion.dto.EstudianteAptoResponse;
import com.avh.practicas.asignacion.dto.HistorialAsignacionResponse;
import com.avh.practicas.asignacion.entity.Asignacion;
import com.avh.practicas.asignacion.entity.EstadoAsignacion;
import com.avh.practicas.asignacion.entity.HistorialAsignacion;
import com.avh.practicas.asignacion.repository.AsignacionRepository;
import com.avh.practicas.asignacion.repository.HistorialAsignacionRepository;
import com.avh.practicas.asignacion.state.AsignacionContext;
import com.avh.practicas.bitacora.entity.TipoAccion;
import com.avh.practicas.bitacora.service.BitacoraService;
import com.avh.practicas.estudiante.entity.EstadoAptitud;
import com.avh.practicas.estudiante.entity.EstadoPractica;
import com.avh.practicas.estudiante.entity.Estudiante;
import com.avh.practicas.estudiante.entity.InstanciaPractica;
import com.avh.practicas.estudiante.repository.EstudianteRepository;
import com.avh.practicas.shared.evento.EventoSistema;
import com.avh.practicas.shared.evento.NotificadorEventos;
import com.avh.practicas.shared.evento.TipoEventoSistema;
import com.avh.practicas.shared.exception.NegocioException;
import com.avh.practicas.shared.exception.RecursoNoEncontradoException;
import com.avh.practicas.vacante.dto.VacanteResponse;
import com.avh.practicas.vacante.entity.EstadoVacanteEnum;
import com.avh.practicas.vacante.entity.Vacante;
import com.avh.practicas.vacante.repository.VacanteRepository;
import com.avh.practicas.vacante.state.VacanteContext;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Servicio principal de S4.
 * Centraliza reglas de negocio de asignacion/postulacion:
 * estudiante APTO, vacante ACTIVA, cupos disponibles y trazabilidad.
 * Aplica State mediante AsignacionContext y Observer mediante NotificadorEventos.
 */
@Service
@RequiredArgsConstructor
public class AsignacionService {

    private static final List<EstadoAsignacion> ESTADOS_ASIGNACION_ACTIVA = List.of(
            EstadoAsignacion.ASIGNADA,
            EstadoAsignacion.EN_PROCESO_VINCULACION,
            EstadoAsignacion.VINCULADA
    );

    private static final List<EstadoPractica> ESTADOS_PRACTICA_ACTIVA = List.of(
            EstadoPractica.EN_CURSO
    );

    private final AsignacionRepository asignacionRepository;
    private final HistorialAsignacionRepository historialRepository;
    private final EstudianteRepository estudianteRepository;
    private final VacanteRepository vacanteRepository;
    private final BitacoraService bitacoraService;
    private final NotificadorEventos notificadorEventos;

    @Transactional
    public AsignacionResponse asignar(AsignacionRequest request) {
        // S4 PE-30: Coordinacion de Practicas crea la postulacion/asignacion formal.
        // No se permite que el estudiante se asigne automaticamente a una vacante.
        Estudiante estudiante = estudianteRepository.findById(request.estudianteId())
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el estudiante con id: " + request.estudianteId()));

        Vacante vacante = vacanteRepository.findById(request.vacanteId())
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la vacante con id: " + request.vacanteId()));

        // Validaciones importantes antes de guardar: aptitud, programa, vacante activa, cupos y asignaciones activas.
        validarPostulacionPorCoordinacion(estudiante, vacante);

        // Patron State de Vacante: se descuenta cupo y si llega a cero puede pasar a CUPOS_COMPLETOS.
        new VacanteContext(vacante).descontarCupo();
        vacanteRepository.save(vacante);

        Asignacion asignacion = Asignacion.builder()
                .coordinadorId(request.coordinadorId())
                .estudianteId(request.estudianteId())
                .vacanteId(request.vacanteId())
                .notaJustificacion(request.notaJustificacion())
                .estado(EstadoAsignacion.ASIGNADA)
                .build();

        Asignacion guardada = asignacionRepository.save(asignacion);
        // Trazabilidad PE-31: cada cambio queda en historial para auditoria y seguimiento.
        registrarHistorial(guardada, null, EstadoAsignacion.ASIGNADA, request.coordinadorId(), "Asignación/postulación creada por Coordinación de Prácticas");
        // Bitacora: deja evidencia de la accion realizada sobre el recurso.
        registrarBitacora(guardada, TipoAccion.CREACION, request.coordinadorId(), null, guardada.getEstado());
        // Observer: se emite evento para correo, panel y bitacora cuando aplique.
        notificar(TipoEventoSistema.ASIGNACION_CREADA, guardada, request.coordinadorId());

        return AsignacionResponse.desdeEntidad(guardada);
    }

    @Transactional
    public AsignacionResponse iniciarVinculacion(Long id, Long responsableId, String motivo) {
        Asignacion asignacion = obtenerEntidad(id);
        EstadoAsignacion anterior = asignacion.getEstado();
        // Patron State: ASIGNADA -> EN_PROCESO_VINCULACION.
        new AsignacionContext(asignacion).iniciarVinculacion();
        Asignacion guardada = asignacionRepository.save(asignacion);
        registrarHistorial(guardada, anterior, guardada.getEstado(), responsableId, motivo);
        registrarBitacora(guardada, TipoAccion.MODIFICACION, responsableId, anterior, guardada.getEstado());
        notificar(TipoEventoSistema.ASIGNACION_EN_VINCULACION, guardada, responsableId);
        return AsignacionResponse.desdeEntidad(guardada);
    }

    @Transactional
    public AsignacionResponse completarVinculacion(Long id, Long responsableId, String motivo) {
        Asignacion asignacion = obtenerEntidad(id);
        EstadoAsignacion anterior = asignacion.getEstado();
        // Patron State: EN_PROCESO_VINCULACION -> VINCULADA.
        new AsignacionContext(asignacion).completarVinculacion();
        Asignacion guardada = asignacionRepository.save(asignacion);
        registrarHistorial(guardada, anterior, guardada.getEstado(), responsableId, motivo);
        registrarBitacora(guardada, TipoAccion.MODIFICACION, responsableId, anterior, guardada.getEstado());
        notificar(TipoEventoSistema.ASIGNACION_VINCULADA, guardada, responsableId);
        return AsignacionResponse.desdeEntidad(guardada);
    }

    @Transactional
    public AsignacionResponse cancelar(Long id, Long responsableId, String motivo) {
        Asignacion asignacion = obtenerEntidad(id);
        EstadoAsignacion anterior = asignacion.getEstado();
        // Patron State: cancela solo si el estado actual permite la transicion.
        new AsignacionContext(asignacion).cancelar(motivo);

        Vacante vacante = vacanteRepository.findById(asignacion.getVacanteId())
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la vacante asociada a la asignación"));
        // Al cancelar se libera el cupo para que otra asignacion pueda usar la vacante.
        new VacanteContext(vacante).liberarCupo();
        vacanteRepository.save(vacante);

        Asignacion guardada = asignacionRepository.save(asignacion);
        registrarHistorial(guardada, anterior, guardada.getEstado(), responsableId, motivo);
        registrarBitacora(guardada, TipoAccion.CANCELACION, responsableId, anterior, guardada.getEstado());
        notificar(TipoEventoSistema.ASIGNACION_CANCELADA, guardada, responsableId);
        return AsignacionResponse.desdeEntidad(guardada);
    }

    @Transactional(readOnly = true)
    public AsignacionDetalleResponse obtener(Long id) {
        Asignacion asignacion = obtenerEntidad(id);
        List<HistorialAsignacionResponse> historial = historialRepository.findByAsignacionIdOrderByFechaAsc(id)
                .stream()
                .map(HistorialAsignacionResponse::desdeEntidad)
                .toList();
        return new AsignacionDetalleResponse(AsignacionResponse.desdeEntidad(asignacion), historial);
    }

    @Transactional(readOnly = true)
    public Page<AsignacionResponse> listar(Long estudianteId,
                                           Long vacanteId,
                                           Long coordinadorId,
                                           EstadoAsignacion estado,
                                           Pageable pageable) {
        return asignacionRepository.findAll(conFiltros(estudianteId, vacanteId, coordinadorId, estado), pageable)
                .map(AsignacionResponse::desdeEntidad);
    }

    @Transactional(readOnly = true)
    public List<EstudianteAptoResponse> listarEstudiantesAptos(Long programaId) {
        return estudianteRepository.findAll()
                .stream()
                .filter(e -> e.getEstadoAptitud() == EstadoAptitud.APTO)
                .filter(e -> programaId == null || (e.getPrograma() != null && programaId.equals(e.getPrograma().getId())))
                .filter(e -> !tieneAsignacionActiva(e.getId()))
                .filter(e -> !tienePracticaActiva(e))
                .map(EstudianteAptoResponse::desdeEntidad)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<VacanteResponse> listarVacantesActivas(Long programaId) {
        return vacanteRepository.findAll()
                .stream()
                .filter(v -> v.getEstado() == EstadoVacanteEnum.ACTIVA)
                .filter(v -> v.getCuposDisponibles() != null && v.getCuposDisponibles() > 0)
                .filter(v -> programaId == null || programaId.equals(v.getProgramaId()))
                .map(VacanteResponse::desdeEntidad)
                .toList();
    }

    /**
     * Reglas de negocio principales antes de crear la asignacion.
     * Aqui se protege que solo se asignen estudiantes APTO a vacantes ACTIVA del mismo programa.
     */
    private void validarPostulacionPorCoordinacion(Estudiante estudiante, Vacante vacante) {
        if (estudiante.getEstadoAptitud() != EstadoAptitud.APTO) {
            throw new NegocioException("Solo se pueden postular/asignar estudiantes con estado APTO.");
        }

        if (estudiante.getPrograma() == null || !estudiante.getPrograma().getId().equals(vacante.getProgramaId())) {
            throw new NegocioException("La vacante no pertenece al programa del estudiante.");
        }

        if (vacante.getEstado() != EstadoVacanteEnum.ACTIVA) {
            throw new NegocioException("La vacante debe estar ACTIVA para poder crear la postulación/asignación.");
        }

        if (vacante.getCuposDisponibles() == null || vacante.getCuposDisponibles() <= 0) {
            throw new NegocioException("La vacante no tiene cupos disponibles.");
        }

        if (tieneAsignacionActiva(estudiante.getId())) {
            throw new NegocioException("El estudiante ya tiene una asignación/postulación activa.");
        }

        if (tienePracticaActiva(estudiante)) {
            throw new NegocioException("El estudiante ya tiene una práctica activa en curso.");
        }
    }

    private boolean tieneAsignacionActiva(Long estudianteId) {
        return asignacionRepository.existsByEstudianteIdAndEstadoIn(estudianteId, ESTADOS_ASIGNACION_ACTIVA);
    }

    private boolean tienePracticaActiva(Estudiante estudiante) {
        if (estudiante.getExpediente() == null || estudiante.getExpediente().getInstanciasPractica() == null) {
            return false;
        }
        return estudiante.getExpediente().getInstanciasPractica()
                .stream()
                .map(InstanciaPractica::getEstado)
                .anyMatch(ESTADOS_PRACTICA_ACTIVA::contains);
    }

    private Asignacion obtenerEntidad(Long id) {
        return asignacionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la asignación con id: " + id));
    }

    /**
     * Guarda la trazabilidad de cambios de estado solicitada en PE-31.
     */
    private void registrarHistorial(Asignacion asignacion,
                                    EstadoAsignacion estadoAnterior,
                                    EstadoAsignacion estadoNuevo,
                                    Long responsableId,
                                    String motivo) {
        historialRepository.save(HistorialAsignacion.builder()
                .asignacionId(asignacion.getId())
                .estadoAnterior(estadoAnterior)
                .estadoNuevo(estadoNuevo)
                .responsableId(responsableId)
                .motivo(motivo)
                .build());
    }

    /**
     * Registra la accion en bitacora para auditoria general del sistema.
     */
    private void registrarBitacora(Asignacion asignacion,
                                   TipoAccion accion,
                                   Long usuarioId,
                                   EstadoAsignacion anterior,
                                   EstadoAsignacion nuevo) {
        bitacoraService.registrar(
                usuarioId,
                "ASIGNACIONES",
                accion,
                asignacion.getId(),
                anterior == null ? null : "estado=" + anterior.name(),
                nuevo == null ? null : "estado=" + nuevo.name()
        );
    }

    private void notificar(TipoEventoSistema tipo, Asignacion asignacion, Long usuarioId) {
        notificadorEventos.notificar(EventoSistema.crear(
                tipo,
                usuarioId,
                "ASIGNACIONES",
                asignacion.getId(),
                Map.of(
                        "estudianteId", asignacion.getEstudianteId(),
                        "vacanteId", asignacion.getVacanteId(),
                        "estado", asignacion.getEstado().name()
                )
        ));
    }

    private Specification<Asignacion> conFiltros(Long estudianteId,
                                                 Long vacanteId,
                                                 Long coordinadorId,
                                                 EstadoAsignacion estado) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (estudianteId != null) {
                predicates.add(cb.equal(root.get("estudianteId"), estudianteId));
            }
            if (vacanteId != null) {
                predicates.add(cb.equal(root.get("vacanteId"), vacanteId));
            }
            if (coordinadorId != null) {
                predicates.add(cb.equal(root.get("coordinadorId"), coordinadorId));
            }
            if (estado != null) {
                predicates.add(cb.equal(root.get("estado"), estado));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
