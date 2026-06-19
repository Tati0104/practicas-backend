package com.avh.practicas.vacante.service;

import com.avh.practicas.auth.entity.Usuario;
import com.avh.practicas.auth.repository.AuthUsuarioRepository;
import com.avh.practicas.configuracion.repository.CatalogoPracticaRepository;
import com.avh.practicas.empresa.repository.EmpresaRepository;
import com.avh.practicas.empresa.repository.TutorEmpresarialRepository;
import com.avh.practicas.shared.enums.Rol;
import com.avh.practicas.shared.evento.EventoSistema;
import com.avh.practicas.shared.evento.NotificadorEventos;
import com.avh.practicas.shared.evento.TipoEventoSistema;
import com.avh.practicas.shared.exception.AccesoNoAutorizadoException;
import com.avh.practicas.shared.exception.CatalogoPracticaNoEncontradaException;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final VacanteResponseMapper responseMapper;
    private final AuthUsuarioRepository usuarioRepository;
    private final EmpresaRepository empresaRepository;
    private final TutorEmpresarialRepository tutorRepository;
    private final CatalogoPracticaRepository catalogoPracticaRepository;

    @Transactional
    public VacanteResponse crear(VacanteRequest request) {
        Long empresaId = resolverEmpresaIdPermitida(request.empresaId());
        validarCatalogoPractica(request.catalogoPracticaId(), request.programaId());

        Vacante vacante = Vacante.builder()
                .empresaId(empresaId)
                .programaId(request.programaId())
                .catalogoPracticaId(request.catalogoPracticaId())
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
        return responseMapper.toResponse(guardada);
    }

    @Transactional
    // Patron State: PENDIENTE_APROBACION -> ACTIVA.
    public VacanteResponse aprobar(Long id, Long aprobadoPorId) {
        Vacante vacante = obtenerEntidad(id);
        validarAccesoVacante(vacante);
        new VacanteContext(vacante).aprobar();
        vacante.setAprobadoPorId(aprobadoPorId);
        Vacante guardada = repository.save(vacante);
        notificar(TipoEventoSistema.VACANTE_APROBADA, guardada, null);
        return responseMapper.toResponse(guardada);
    }

    @Transactional
    // Patron State: rechaza la vacante dejando motivo obligatorio.
    public VacanteResponse rechazar(Long id, String motivo) {
        Vacante vacante = obtenerEntidad(id);
        validarAccesoVacante(vacante);
        new VacanteContext(vacante).rechazar(motivo);
        Vacante guardada = repository.save(vacante);
        notificar(TipoEventoSistema.VACANTE_RECHAZADA, guardada, null);
        return responseMapper.toResponse(guardada);
    }

    @Transactional
    // Patron State: ACTIVA -> PAUSADA.
    public VacanteResponse pausar(Long id) {
        Vacante vacante = obtenerEntidad(id);
        validarAccesoVacante(vacante);
        new VacanteContext(vacante).pausar();
        Vacante guardada = repository.save(vacante);
        notificar(TipoEventoSistema.VACANTE_PAUSADA, guardada, null);
        return responseMapper.toResponse(guardada);
    }

    @Transactional
    // Patron State: PAUSADA -> ACTIVA.
    public VacanteResponse reactivar(Long id) {
        Vacante vacante = obtenerEntidad(id);
        validarAccesoVacante(vacante);
        new VacanteContext(vacante).reactivar();
        Vacante guardada = repository.save(vacante);
        notificar(TipoEventoSistema.VACANTE_REACTIVADA, guardada, null);
        return responseMapper.toResponse(guardada);
    }

    @Transactional
    // Patron State: pasa la vacante a estado CERRADA.
    public VacanteResponse cerrar(Long id) {
        Vacante vacante = obtenerEntidad(id);
        validarAccesoVacante(vacante);
        new VacanteContext(vacante).cerrar();
        Vacante guardada = repository.save(vacante);
        notificar(TipoEventoSistema.VACANTE_CERRADA, guardada, null);
        return responseMapper.toResponse(guardada);
    }

    @Transactional
    // Cupos: descuenta cupo y puede mover la vacante a CUPOS_COMPLETOS.
    public VacanteResponse descontarCupo(Long id) {
        Vacante vacante = obtenerEntidad(id);
        validarAccesoVacante(vacante);
        new VacanteContext(vacante).descontarCupo();
        Vacante guardada = repository.save(vacante);
        if (guardada.getEstado() == EstadoVacanteEnum.CUPOS_COMPLETOS) {
            notificar(TipoEventoSistema.VACANTE_CUPOS_COMPLETOS, guardada, null);
        }
        return responseMapper.toResponse(guardada);
    }

    @Transactional
    // Cupos: libera cupo al cancelar una asignacion.
    public VacanteResponse liberarCupo(Long id) {
        Vacante vacante = obtenerEntidad(id);
        validarAccesoVacante(vacante);
        new VacanteContext(vacante).liberarCupo();
        return responseMapper.toResponse(repository.save(vacante));
    }

    @Transactional(readOnly = true)
    public VacanteResponse obtener(Long id) {
        Vacante vacante = obtenerEntidad(id);
        validarAccesoVacante(vacante);
        return responseMapper.toResponse(vacante);
    }

    @Transactional(readOnly = true)
    public Page<VacanteResponse> listar(Long empresaId,
                                        Long programaId,
                                        EstadoVacanteEnum estado,
                                        String modalidad,
                                        String area,
                                        Pageable pageable) {
        Long empresaScoped = resolverEmpresaIdFiltro(empresaId);
        return repository.findAll(conFiltros(empresaScoped, programaId, estado, modalidad, area), pageable)
                .map(responseMapper::toResponse);
    }


    @Transactional(readOnly = true)
    // Solo retorna vacantes ACTIVA con cupos, útil para el apartado visible al estudiante.
    public List<VacanteResponse> listarDisponiblesParaEstudiante(Long programaId) {
        return repository.findAll()
                .stream()
                .filter(v -> v.getEstado() == EstadoVacanteEnum.ACTIVA)
                .filter(v -> v.getCuposDisponibles() != null && v.getCuposDisponibles() > 0)
                .filter(v -> programaId == null || programaId.equals(v.getProgramaId()))
                .map(responseMapper::toResponse)
                .toList();
    }

    private Vacante obtenerEntidad(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vacante no encontrada"));
    }

    private void validarCatalogoPractica(Long catalogoPracticaId, Long programaId) {
        if (catalogoPracticaId == null) {
            return;
        }
        catalogoPracticaRepository.findById(catalogoPracticaId)
                .filter(c -> c.getPrograma() != null && c.getPrograma().getId().equals(programaId))
                .orElseThrow(() -> new CatalogoPracticaNoEncontradaException(
                        "El nivel de práctica seleccionado no pertenece al programa de la vacante."));
    }

    private Long resolverEmpresaIdPermitida(Long empresaIdSolicitada) {
        Usuario usuario = obtenerUsuarioActual();
        if (usuario == null) {
            return empresaIdSolicitada;
        }
        if (usuario.getRol() == Rol.EMPRESA || usuario.getRol() == Rol.TUTOR_EMPRESARIAL) {
            Long propia = resolverEmpresaIdUsuario(usuario);
            if (empresaIdSolicitada != null && !propia.equals(empresaIdSolicitada)) {
                throw new AccesoNoAutorizadoException("Acceso denegado: no puede crear vacantes para otra empresa.");
            }
            return propia;
        }
        return empresaIdSolicitada;
    }

    private Long resolverEmpresaIdFiltro(Long empresaIdSolicitada) {
        Usuario usuario = obtenerUsuarioActual();
        if (usuario == null) {
            return empresaIdSolicitada;
        }
        if (usuario.getRol() == Rol.EMPRESA || usuario.getRol() == Rol.TUTOR_EMPRESARIAL) {
            Long propia = resolverEmpresaIdUsuario(usuario);
            if (empresaIdSolicitada != null && !propia.equals(empresaIdSolicitada)) {
                throw new AccesoNoAutorizadoException("Acceso denegado: vacantes fuera de su empresa.");
            }
            return propia;
        }
        return empresaIdSolicitada;
    }

    private void validarAccesoVacante(Vacante vacante) {
        Usuario usuario = obtenerUsuarioActual();
        if (usuario == null || usuario.getRol() == Rol.ADMIN || usuario.getRol() == Rol.COORD_PRACTICA) {
            return;
        }
        if (usuario.getRol() == Rol.EMPRESA || usuario.getRol() == Rol.TUTOR_EMPRESARIAL) {
            Long propia = resolverEmpresaIdUsuario(usuario);
            if (!propia.equals(vacante.getEmpresaId())) {
                throw new AccesoNoAutorizadoException("Acceso denegado: vacante fuera de su empresa.");
            }
        }
    }

    private Long resolverEmpresaIdUsuario(Usuario usuario) {
        if (usuario.getRol() == Rol.TUTOR_EMPRESARIAL) {
            return tutorRepository.findByUsuarioId(usuario.getId())
                    .or(() -> tutorRepository.findByCorreoIgnoreCase(usuario.getCorreo()))
                    .map(t -> t.getEmpresa().getId())
                    .orElseThrow(() -> new AccesoNoAutorizadoException(
                            "Acceso denegado: tutor empresarial no encontrado"));
        }
        return empresaRepository.findByUsuarioId(usuario.getId())
                .map(e -> e.getId())
                .orElseThrow(() -> new AccesoNoAutorizadoException(
                        "Acceso denegado: empresa asociada no encontrada"));
    }

    private Usuario obtenerUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        return usuarioRepository.findByCorreoIgnoreCase(auth.getName()).orElse(null);
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
