package com.avh.practicas.vacante.service;

import com.avh.practicas.shared.evento.EventoSistema;
import com.avh.practicas.shared.evento.NotificadorEventos;
import com.avh.practicas.shared.evento.TipoEventoSistema;
import com.avh.practicas.vacante.dto.VacanteRequest;
import com.avh.practicas.vacante.dto.VacanteResponse;
import com.avh.practicas.vacante.entity.EstadoVacanteEnum;
import com.avh.practicas.vacante.entity.Vacante;
import com.avh.practicas.vacante.repository.VacanteRepository;
import com.avh.practicas.vacante.state.VacanteContext;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Servicio KBM S3 para ciclo de vida de vacantes.
 * Usa VacanteContext como patron State para aprobar, rechazar, pausar, cerrar y manejar cupos.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VacanteService {

    private final VacanteRepository repository;
    private final NotificadorEventos notificadorEventos;

    @Transactional
    public VacanteResponse crear(VacanteRequest request) {
        Vacante vacante = Vacante.builder()
                .empresaId(request.empresaId())
                .programaId(request.programaId())
                .creadoPorId(request.creadoPorId())
                .cargo(request.cargo())
                .descripcionPerfil(request.descripcionPerfil())
                .requisitos(request.requisitos())
                .modalidad(request.modalidad())
                .area(request.area())
                .cuposTotales(request.cuposTotales())
                .cuposDisponibles(request.cuposTotales())
                .estado(EstadoVacanteEnum.PENDIENTE_APROBACION)
                .fechaInicioDisponibilidad(request.fechaInicioDisponibilidad())
                .fechaFinDisponibilidad(request.fechaFinDisponibilidad())
                .build();

        Vacante guardada = repository.save(vacante);
        notificar(TipoEventoSistema.VACANTE_CREADA, guardada, request.correoEmpresa());
        return VacanteResponse.desdeEntidad(guardada);
    }

    @Transactional
    // Patron State: PENDIENTE_APROBACION -> ACTIVA.
    public VacanteResponse aprobar(Long id, Long aprobadoPorId) {
        Vacante vacante = obtenerEntidad(id);
        new VacanteContext(vacante).aprobar();
        vacante.setAprobadoPorId(aprobadoPorId);
        Vacante guardada = repository.save(vacante);
        notificar(TipoEventoSistema.VACANTE_APROBADA, guardada, null);
        return VacanteResponse.desdeEntidad(guardada);
    }

    @Transactional
    // Patron State: rechaza la vacante dejando motivo obligatorio.
    public VacanteResponse rechazar(Long id, String motivo) {
        Vacante vacante = obtenerEntidad(id);
        new VacanteContext(vacante).rechazar(motivo);
        Vacante guardada = repository.save(vacante);
        notificar(TipoEventoSistema.VACANTE_RECHAZADA, guardada, null);
        return VacanteResponse.desdeEntidad(guardada);
    }

    @Transactional
    // Patron State: ACTIVA -> PAUSADA.
    public VacanteResponse pausar(Long id) {
        Vacante vacante = obtenerEntidad(id);
        new VacanteContext(vacante).pausar();
        Vacante guardada = repository.save(vacante);
        notificar(TipoEventoSistema.VACANTE_PAUSADA, guardada, null);
        return VacanteResponse.desdeEntidad(guardada);
    }

    @Transactional
    // Patron State: PAUSADA -> ACTIVA.
    public VacanteResponse reactivar(Long id) {
        Vacante vacante = obtenerEntidad(id);
        new VacanteContext(vacante).reactivar();
        Vacante guardada = repository.save(vacante);
        notificar(TipoEventoSistema.VACANTE_REACTIVADA, guardada, null);
        return VacanteResponse.desdeEntidad(guardada);
    }

    @Transactional
    // Patron State: pasa la vacante a estado CERRADA.
    public VacanteResponse cerrar(Long id) {
        Vacante vacante = obtenerEntidad(id);
        new VacanteContext(vacante).cerrar();
        Vacante guardada = repository.save(vacante);
        notificar(TipoEventoSistema.VACANTE_CERRADA, guardada, null);
        return VacanteResponse.desdeEntidad(guardada);
    }

    @Transactional
    // Cupos: descuenta cupo y puede mover la vacante a CUPOS_COMPLETOS.
    public VacanteResponse descontarCupo(Long id) {
        Vacante vacante = obtenerEntidad(id);
        new VacanteContext(vacante).descontarCupo();
        Vacante guardada = repository.save(vacante);
        if (guardada.getEstado() == EstadoVacanteEnum.CUPOS_COMPLETOS) {
            notificar(TipoEventoSistema.VACANTE_CUPOS_COMPLETOS, guardada, null);
        }
        return VacanteResponse.desdeEntidad(guardada);
    }

    @Transactional
    // Cupos: libera cupo al cancelar una asignacion.
    public VacanteResponse liberarCupo(Long id) {
        Vacante vacante = obtenerEntidad(id);
        new VacanteContext(vacante).liberarCupo();
        return VacanteResponse.desdeEntidad(repository.save(vacante));
    }

    @Transactional(readOnly = true)
    public VacanteResponse obtener(Long id) {
        return VacanteResponse.desdeEntidad(obtenerEntidad(id));
    }

    @Transactional(readOnly = true)
    public Page<VacanteResponse> listar(Long empresaId,
                                        Long programaId,
                                        EstadoVacanteEnum estado,
                                        String modalidad,
                                        String area,
                                        Pageable pageable) {
        return repository.findAll(conFiltros(empresaId, programaId, estado, modalidad, area), pageable)
                .map(VacanteResponse::desdeEntidad);
    }


    @Transactional(readOnly = true)
    // Solo retorna vacantes ACTIVA con cupos, útil para el apartado visible al estudiante.
    public List<VacanteResponse> listarDisponiblesParaEstudiante(Long programaId) {
        return repository.findAll()
                .stream()
                .filter(v -> v.getEstado() == EstadoVacanteEnum.ACTIVA)
                .filter(v -> v.getCuposDisponibles() != null && v.getCuposDisponibles() > 0)
                .filter(v -> programaId == null || programaId.equals(v.getProgramaId()))
                .map(VacanteResponse::desdeEntidad)
                .toList();
    }

    private Vacante obtenerEntidad(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vacante no encontrada"));
    }

    private Specification<Vacante> conFiltros(Long empresaId,
                                              Long programaId,
                                              EstadoVacanteEnum estado,
                                              String modalidad,
                                              String area) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (empresaId != null) {
                predicates.add(cb.equal(root.get("empresaId"), empresaId));
            }
            if (programaId != null) {
                predicates.add(cb.equal(root.get("programaId"), programaId));
            }
            if (estado != null) {
                predicates.add(cb.equal(root.get("estado"), estado));
            }
            if (modalidad != null && !modalidad.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("modalidad")), modalidad.toLowerCase()));
            }
            if (area != null && !area.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("area")), area.toLowerCase()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private void notificar(TipoEventoSistema tipo, Vacante vacante, String correoEmpresa) {
        try {
            notificadorEventos.notificar(EventoSistema.crear(
                    tipo,
                    vacante.getCreadoPorId(),
                    "VACANTES",
                    vacante.getId(),
                    Map.of(
                            "cargo", vacante.getCargo(),
                            "estado", vacante.getEstado().name(),
                            "empresaId", vacante.getEmpresaId(),
                            "programaId", vacante.getProgramaId(),
                            "correoEmpresa", correoEmpresa == null ? "" : correoEmpresa
                    )
            ));
        } catch (Exception ex) {
            log.warn("Vacante {} creada/actualizada, pero falló la notificación por correo: {}",
                    vacante.getId(), ex.getMessage());
        }
    }
}
