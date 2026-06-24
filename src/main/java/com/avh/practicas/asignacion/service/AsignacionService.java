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
import com.avh.practicas.auth.entity.Usuario;
import com.avh.practicas.auth.repository.AuthUsuarioRepository;
import com.avh.practicas.configuracion.repository.CatalogoPracticaRepository;
import com.avh.practicas.bitacora.entity.TipoAccion;
import com.avh.practicas.bitacora.service.BitacoraService;
import com.avh.practicas.empresa.repository.EmpresaRepository;
import com.avh.practicas.empresa.entity.TutorEmpresarial;
import com.avh.practicas.empresa.repository.TutorEmpresarialRepository;
import com.avh.practicas.estudiante.repository.DocenteAsesorRepository;
import com.avh.practicas.estudiante.entity.EstadoAptitud;
import com.avh.practicas.estudiante.entity.EstadoPractica;
import com.avh.practicas.estudiante.entity.Estudiante;
import com.avh.practicas.estudiante.entity.InstanciaPractica;
import com.avh.practicas.estudiante.repository.EstudianteRepository;
import com.avh.practicas.estudiante.repository.InstanciaPracticaRepository;
import com.avh.practicas.shared.evento.EventoSistema;
import com.avh.practicas.shared.evento.NotificadorEventos;
import com.avh.practicas.shared.evento.TipoEventoSistema;
import com.avh.practicas.shared.enums.Rol;
import com.avh.practicas.shared.exception.AccesoNoAutorizadoException;
import com.avh.practicas.shared.exception.NegocioException;
import com.avh.practicas.shared.exception.RecursoNoEncontradoException;
import com.avh.practicas.vacante.dto.VacanteResponse;
import com.avh.practicas.vacante.entity.EstadoVacanteEnum;
import com.avh.practicas.vacante.entity.Vacante;
import com.avh.practicas.vacante.repository.VacanteRepository;
import com.avh.practicas.vacante.service.VacanteResponseMapper;
import com.avh.practicas.vacante.state.VacanteContext;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
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
    private final VacanteResponseMapper vacanteResponseMapper;
    private final AsignacionResponseMapper asignacionResponseMapper;
    private final CatalogoPracticaRepository catalogoPracticaRepository;
    private final BitacoraService bitacoraService;
    private final NotificadorEventos notificadorEventos;
    private final InstanciaPracticaRepository instanciaPracticaRepository;
    private final TutorEmpresarialRepository tutorEmpresarialRepository;
    private final AuthUsuarioRepository usuarioRepository;
    private final EmpresaRepository empresaRepository;
    private final DocenteAsesorRepository docenteAsesorRepository;

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
        // Fix de raiz: la InstanciaPractica pendiente del estudiante hereda empresa/tutor
        // de la vacante asignada, para que la vinculacion no dependa de que antes se suba un documento.
        vincularInstanciaPractica(guardada, vacante);
        // Trazabilidad PE-31: cada cambio queda en historial para auditoria y seguimiento.
        registrarHistorial(guardada, null, EstadoAsignacion.ASIGNADA, request.coordinadorId(), "Asignación/postulación creada por Coordinación de Prácticas");
        // Bitacora: deja evidencia de la accion realizada sobre el recurso.
        registrarBitacora(guardada, TipoAccion.CREACION, request.coordinadorId(), null, guardada.getEstado());
        // Observer: se emite evento para correo, panel y bitacora cuando aplique.
        notificar(TipoEventoSistema.ASIGNACION_CREADA, guardada, request.coordinadorId());

        return asignacionResponseMapper.toResponse(guardada);
    }

    @Transactional
    public AsignacionResponse iniciarVinculacion(Long id, Long responsableId, String motivo) {
        Asignacion asignacion = obtenerEntidad(id);
        validarAccesoAsignacion(asignacion);
        EstadoAsignacion anterior = asignacion.getEstado();
        // Patron State: ASIGNADA -> EN_PROCESO_VINCULACION.
        new AsignacionContext(asignacion).iniciarVinculacion();
        Asignacion guardada = asignacionRepository.save(asignacion);
        registrarHistorial(guardada, anterior, guardada.getEstado(), responsableId, motivo);
        registrarBitacora(guardada, TipoAccion.MODIFICACION, responsableId, anterior, guardada.getEstado());
        notificar(TipoEventoSistema.ASIGNACION_EN_VINCULACION, guardada, responsableId);
        return asignacionResponseMapper.toResponse(guardada);
    }

    @Transactional
    public AsignacionResponse completarVinculacion(Long id, Long responsableId, String motivo) {
        Asignacion asignacion = obtenerEntidad(id);
        validarAccesoAsignacion(asignacion);
        EstadoAsignacion anterior = asignacion.getEstado();
        // Patron State: EN_PROCESO_VINCULACION -> VINCULADA.
        new AsignacionContext(asignacion).completarVinculacion();
        Asignacion guardada = asignacionRepository.save(asignacion);
        registrarHistorial(guardada, anterior, guardada.getEstado(), responsableId, motivo);
        registrarBitacora(guardada, TipoAccion.MODIFICACION, responsableId, anterior, guardada.getEstado());
        notificar(TipoEventoSistema.ASIGNACION_VINCULADA, guardada, responsableId);
        return asignacionResponseMapper.toResponse(guardada);
    }

    @Transactional
    public AsignacionResponse cancelar(Long id, Long responsableId, String motivo) {
        Asignacion asignacion = obtenerEntidad(id);
        validarAccesoAsignacion(asignacion);
        EstadoAsignacion anterior = asignacion.getEstado();
        // Patron State: cancela solo si el estado actual permite la transicion.
        new AsignacionContext(asignacion).cancelar(motivo);

        Vacante vacante = vacanteRepository.findById(asignacion.getVacanteId())
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la vacante asociada a la asignación"));
        // Al cancelar se libera el cupo para que otra asignacion pueda usar la vacante.
        new VacanteContext(vacante).liberarCupo();
        vacanteRepository.save(vacante);

        liberarInstanciaTrasCancelacion(asignacion);

        Asignacion guardada = asignacionRepository.save(asignacion);
        registrarHistorial(guardada, anterior, guardada.getEstado(), responsableId, motivo);
        registrarBitacora(guardada, TipoAccion.CANCELACION, responsableId, anterior, guardada.getEstado());
        notificar(TipoEventoSistema.ASIGNACION_CANCELADA, guardada, responsableId);
        return asignacionResponseMapper.toResponse(guardada);
    }

    @Transactional(readOnly = true)
    public AsignacionDetalleResponse obtener(Long id) {
        Asignacion asignacion = obtenerEntidad(id);
        validarAccesoAsignacion(asignacion);
        List<HistorialAsignacionResponse> historial = historialRepository.findByAsignacionIdOrderByFechaAsc(id)
                .stream()
                .map(HistorialAsignacionResponse::desdeEntidad)
                .toList();
        return new AsignacionDetalleResponse(asignacionResponseMapper.toResponse(asignacion), historial);
    }

    @Transactional(readOnly = true)
    public Page<AsignacionResponse> listar(Long estudianteId,
                                           Long vacanteId,
                                           Long coordinadorId,
                                           EstadoAsignacion estado,
                                           Pageable pageable) {

        Usuario usuario = obtenerUsuarioActual();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ESTUDIANTE"))) {
            String correo = auth.getName();
            Estudiante estudianteLogueado = estudianteRepository.findByCorreo(correo).orElse(null);
            if (estudianteLogueado != null) {
                estudianteId = estudianteLogueado.getId();
            }
        }
        List<Long> vacanteIdsPermitidas = null;
        List<Long> estudianteIdsPermitidos = null;
        if (usuario != null && usuario.getRol() == Rol.EMPRESA) {
            Long empresaId = empresaRepository.findByUsuarioId(usuario.getId())
                    .map(e -> e.getId())
                    .orElseThrow(() -> new AccesoNoAutorizadoException("Empresa asociada no encontrada."));
            vacanteIdsPermitidas = vacanteRepository.findByEmpresaId(empresaId).stream()
                    .map(Vacante::getId)
                    .toList();
        }
        if (usuario != null && usuario.getRol() == Rol.TUTOR_EMPRESARIAL) {
            Long tutorId = tutorEmpresarialRepository.findByUsuarioId(usuario.getId())
                    .or(() -> tutorEmpresarialRepository.findByCorreoIgnoreCase(usuario.getCorreo()))
                    .map(t -> t.getId())
                    .orElseThrow(() -> new AccesoNoAutorizadoException("Tutor empresarial no encontrado."));
            estudianteIdsPermitidos = instanciaPracticaRepository.findEstudianteIdsByTutorId(tutorId);
        }
        if (usuario != null && usuario.getRol() == Rol.DOCENTE_ASESOR) {
            Long docenteId = docenteAsesorRepository.findByUsuario_Id(usuario.getId())
                    .or(() -> docenteAsesorRepository.findByCorreoIgnoreCase(usuario.getCorreo()))
                    .map(d -> d.getId())
                    .orElseThrow(() -> new AccesoNoAutorizadoException("Docente asesor no encontrado."));
            estudianteIdsPermitidos = instanciaPracticaRepository.findEstudianteIdsByDocenteAsesorId(docenteId);
        }

        return asignacionRepository.findAll(conFiltros(estudianteId, vacanteId, coordinadorId, estado,
                        vacanteIdsPermitidas, estudianteIdsPermitidos), pageable)
                .map(asignacionResponseMapper::toResponse);
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
                .map(vacanteResponseMapper::toResponse)
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

        if (vacante.getEmpresaId() != null
                && tutorEmpresarialRepository.findByEmpresaIdAndActivoTrue(vacante.getEmpresaId()).isEmpty()) {
            throw new NegocioException("La empresa de la vacante no tiene tutores empresariales activos.");
        }

        if (tieneAsignacionActiva(estudiante.getId())) {
            throw new NegocioException("El estudiante ya tiene una asignación/postulación activa.");
        }

        if (tienePracticaActiva(estudiante)) {
            throw new NegocioException("El estudiante ya tiene una práctica activa en curso.");
        }
    }

    /**
     * Propaga empresa_id/tutor_id desde la vacante hacia la InstanciaPractica pendiente
     * del estudiante y enlaza la asignacion con ella. Antes este enlace solo ocurria de
     * forma perezosa al cargar el primer documento de vinculacion, lo que dejaba la
     * practica sin empresa/tutor configurados si la vinculacion avanzaba sin documentos.
     */
    private void vincularInstanciaPractica(Asignacion asignacion, Vacante vacante) {
        InstanciaPractica practica = instanciaPracticaRepository
                .findFirstByExpedienteEstudianteIdAndEstadoOrderByNumeroPracticaDesc(
                        asignacion.getEstudianteId(), EstadoPractica.ASIGNADA_PENDIENTE_INICIO)
                .orElseThrow(() -> new NegocioException(
                        "El estudiante no tiene una práctica pendiente de inicio para asignar. "
                                + "Coordinación Académica debe marcarlo APTO para la práctica correspondiente."));

        if (vacante.getCatalogoPracticaId() != null) {
            catalogoPracticaRepository.findById(vacante.getCatalogoPracticaId()).ifPresent(catalogo -> {
                if (!catalogo.getNumeroPractica().equals(practica.getNumeroPractica())) {
                    throw new NegocioException(
                            "La vacante corresponde a la práctica " + catalogo.getNumeroPractica()
                                    + ", pero el estudiante tiene pendiente la práctica "
                                    + practica.getNumeroPractica() + ".");
                }
            });
        }

        practica.setEmpresaId(vacante.getEmpresaId());
        tutorEmpresarialRepository.findByEmpresaIdAndActivoTrue(vacante.getEmpresaId()).stream()
                .findFirst()
                .or(() -> tutorEmpresarialRepository.findByEmpresaId(vacante.getEmpresaId()).stream().findFirst())
                .ifPresent(tutor -> practica.setTutorId(tutor.getId()));
        instanciaPracticaRepository.save(practica);

        asignacion.setInstanciaPracticaId(practica.getId());
        asignacionRepository.save(asignacion);
    }

    /**
     * Tras cancelar una asignación, libera la instancia pendiente para que el estudiante
     * pueda ser asignado a otra vacante conservando el mismo número de práctica.
     */
    private void liberarInstanciaTrasCancelacion(Asignacion asignacion) {
        if (asignacion.getInstanciaPracticaId() == null) {
            return;
        }
        InstanciaPractica practica = instanciaPracticaRepository.findById(asignacion.getInstanciaPracticaId())
                .orElse(null);
        if (practica == null || practica.getEstado() != EstadoPractica.ASIGNADA_PENDIENTE_INICIO) {
            return;
        }
        practica.setEmpresaId(null);
        practica.setTutorId(null);
        practica.setDocenteAsesorId(null);
        instanciaPracticaRepository.save(practica);
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

    private void validarAccesoAsignacion(Asignacion asignacion) {
        Usuario usuario = obtenerUsuarioActual();
        if (usuario == null || usuario.getRol() == Rol.ADMIN || usuario.getRol() == Rol.COORD_PRACTICA) {
            return;
        }
        if (usuario.getRol() == Rol.ESTUDIANTE) {
            Estudiante estudiante = estudianteRepository.findByCorreoIgnoreCase(usuario.getCorreo())
                    .orElseThrow(() -> new AccesoNoAutorizadoException("Perfil de estudiante no encontrado."));
            if (!estudiante.getId().equals(asignacion.getEstudianteId())) {
                throw new AccesoNoAutorizadoException("Acceso denegado: asignación de otro estudiante.");
            }
            return;
        }
        if (usuario.getRol() == Rol.EMPRESA) {
            Long empresaId = empresaRepository.findByUsuarioId(usuario.getId())
                    .map(e -> e.getId())
                    .orElseThrow(() -> new AccesoNoAutorizadoException("Empresa asociada no encontrada."));
            Vacante vacante = vacanteRepository.findById(asignacion.getVacanteId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Vacante no encontrada"));
            if (!empresaId.equals(vacante.getEmpresaId())) {
                throw new AccesoNoAutorizadoException("Acceso denegado: asignación fuera de su empresa.");
            }
            return;
        }
        if (usuario.getRol() == Rol.TUTOR_EMPRESARIAL) {
            TutorEmpresarial tutor = tutorEmpresarialRepository.findByUsuarioId(usuario.getId())
                    .or(() -> tutorEmpresarialRepository.findByCorreoIgnoreCase(usuario.getCorreo()))
                    .orElseThrow(() -> new AccesoNoAutorizadoException("Tutor empresarial no encontrado."));
            Vacante vacante = vacanteRepository.findById(asignacion.getVacanteId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Vacante no encontrada"));
            if (tutor.getEmpresa() == null || !tutor.getEmpresa().getId().equals(vacante.getEmpresaId())) {
                throw new AccesoNoAutorizadoException("Acceso denegado: asignación fuera de su empresa.");
            }
            boolean permitido = asignacion.getInstanciaPracticaId() == null
                    || instanciaPracticaRepository.existsByIdAndTutorId(asignacion.getInstanciaPracticaId(), tutor.getId());
            if (!permitido) {
                throw new AccesoNoAutorizadoException("Acceso denegado: asignación fuera de su empresa.");
            }
            return;
        }
        if (usuario.getRol() == Rol.DOCENTE_ASESOR) {
            Long docenteId = docenteAsesorRepository.findByUsuario_Id(usuario.getId())
                    .or(() -> docenteAsesorRepository.findByCorreoIgnoreCase(usuario.getCorreo()))
                    .map(d -> d.getId())
                    .orElseThrow(() -> new AccesoNoAutorizadoException("Docente asesor no encontrado."));
            boolean permitido = asignacion.getInstanciaPracticaId() != null
                    ? instanciaPracticaRepository.existsByIdAndDocenteAsesorId(asignacion.getInstanciaPracticaId(), docenteId)
                    : instanciaPracticaRepository.existsByExpedienteEstudianteIdAndDocenteAsesorId(asignacion.getEstudianteId(), docenteId);
            if (!permitido) {
                throw new AccesoNoAutorizadoException("Acceso denegado: asignación fuera del docente.");
            }
        }
    }

    private Usuario obtenerUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        return usuarioRepository.findByCorreoIgnoreCase(auth.getName()).orElse(null);
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
        Estudiante est = estudianteRepository.findById(asignacion.getEstudianteId()).orElse(null);
        Vacante vac = vacanteRepository.findById(asignacion.getVacanteId()).orElse(null);

        Map<String, Object> datos = new java.util.HashMap<>();
        datos.put("estudianteId", asignacion.getEstudianteId());
        datos.put("vacanteId", asignacion.getVacanteId());
        datos.put("estado", asignacion.getEstado().name());
        
        if (est != null) {
            datos.put("correo_estudiante", est.getCorreo());
            datos.put("nombre_estudiante", est.getNombre());
        }
        if (vac != null) {
            String emp = vacanteResponseMapper.toResponse(vac).empresaNombre();
            datos.put("empresa", emp != null ? emp : "la empresa");
        }

        notificadorEventos.notificar(EventoSistema.crear(
                tipo,
                usuarioId,
                "ASIGNACIONES",
                asignacion.getId(),
                datos
        ));
    }

    private Specification<Asignacion> conFiltros(Long estudianteId,
                                                 Long vacanteId,
                                                 Long coordinadorId,
                                                 EstadoAsignacion estado,
                                                 List<Long> vacanteIdsPermitidas,
                                                 List<Long> estudianteIdsPermitidos) {
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
            if (vacanteIdsPermitidas != null) {
                if (vacanteIdsPermitidas.isEmpty()) {
                    predicates.add(cb.disjunction());
                } else {
                    predicates.add(root.get("vacanteId").in(vacanteIdsPermitidas));
                }
            }
            if (estudianteIdsPermitidos != null) {
                if (estudianteIdsPermitidos.isEmpty()) {
                    predicates.add(cb.disjunction());
                } else {
                    predicates.add(root.get("estudianteId").in(estudianteIdsPermitidos));
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
