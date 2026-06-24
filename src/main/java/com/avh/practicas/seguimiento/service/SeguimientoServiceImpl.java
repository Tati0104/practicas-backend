package com.avh.practicas.seguimiento.service;

import com.avh.practicas.empresa.entity.TutorEmpresarial;
import com.avh.practicas.empresa.repository.TutorEmpresarialRepository;
import com.avh.practicas.estudiante.entity.DocenteAsesor;
import com.avh.practicas.estudiante.entity.EstadoPractica;
import com.avh.practicas.estudiante.entity.Estudiante;
import com.avh.practicas.estudiante.entity.InstanciaPractica;
import com.avh.practicas.estudiante.repository.DocenteAsesorRepository;
import com.avh.practicas.estudiante.repository.EstudianteRepository;
import com.avh.practicas.estudiante.repository.InstanciaPracticaRepository;
import com.avh.practicas.seguimiento.dto.AvanceRequest;
import com.avh.practicas.seguimiento.dto.BitacoraRequest;
import com.avh.practicas.seguimiento.dto.ObservacionRequest;
import com.avh.practicas.seguimiento.dto.TableroResponse;
import com.avh.practicas.seguimiento.entity.AlertaSistema;
import com.avh.practicas.seguimiento.entity.AvanceTutor;
import com.avh.practicas.seguimiento.entity.BitacoraEstudiante;
import com.avh.practicas.seguimiento.entity.ObservacionDocente;
import com.avh.practicas.seguimiento.repository.AlertaSistemaRepository;
import com.avh.practicas.seguimiento.repository.AvanceTutorRepository;
import com.avh.practicas.seguimiento.repository.BitacoraEstudianteRepository;
import com.avh.practicas.seguimiento.repository.ObservacionDocenteRepository;
import com.avh.practicas.notificacion.service.NotificacionService;
import com.avh.practicas.shared.scope.ScopePracticaResolver;
import com.avh.practicas.shared.scope.ScopePracticas;
import com.avh.practicas.shared.exception.AccesoNoAutorizadoException;
import com.avh.practicas.shared.exception.NegocioException;
import com.avh.practicas.shared.exception.RecursoNoEncontradoException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementación de los servicios de seguimiento del módulo de prácticas.
 */
@Service("seguimientoServiceImpl")
@RequiredArgsConstructor
@Transactional
public class SeguimientoServiceImpl implements SeguimientoService {

    private static final String DIRECTORIO_SEGUIMIENTO = "uploads/seguimiento";
    private static final long TAMANO_MAXIMO_BYTES = 10L * 1024 * 1024;
    private static final List<String> MIMES_PERMITIDOS = List.of(
            "application/pdf",
            "image/jpeg", "image/jpg", "image/png",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    private final InstanciaPracticaRepository practicaRepository;
    private final DocenteAsesorRepository docenteRepository;
    private final TutorEmpresarialRepository tutorRepository;
    private final EstudianteRepository estudianteRepository;
    private final ObservacionDocenteRepository observacionDocenteRepository;
    private final AvanceTutorRepository avanceTutorRepository;
    private final BitacoraEstudianteRepository bitacoraEstudianteRepository;
    private final AlertaSistemaRepository alertaSistemaRepository;
    private final NotificacionService notificacionService;
    private final JdbcTemplate jdbcTemplate;
    private final ScopePracticaResolver scopeResolver;

    /**
     * Valida si un corte específico de una práctica está cerrado.
     * Un corte se considera cerrado si ya tiene una calificación registrada o si la práctica está inmutable (finalizada).
     */
    private boolean esCorteCerrado(Long practicaId, Integer corte) {
        // Verificar si la práctica es inmutable
        Boolean inmutable = jdbcTemplate.queryForObject(
                "SELECT inmutable FROM instancias_practica WHERE id = ?",
                Boolean.class,
                practicaId
        );
        if (Boolean.TRUE.equals(inmutable)) {
            return true;
        }

        // Verificar si ya existe una nota final registrada
        Integer countFinal = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM notas_finales WHERE instancia_practica_id = ?",
                Integer.class,
                practicaId
        );
        if (countFinal != null && countFinal > 0) {
            return true;
        }

        // Verificar si existe nota del docente para este corte
        Integer countDocente = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM notas_docente WHERE instancia_practica_id = ? AND corte = ?",
                Integer.class,
                practicaId,
                corte
        );
        if (countDocente != null && countDocente > 0) {
            return true;
        }

        // Verificar si existe nota del tutor para este corte
        Integer countTutor = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM notas_tutor WHERE instancia_practica_id = ? AND corte = ?",
                Integer.class,
                practicaId,
                corte
        );
        return countTutor != null && countTutor > 0;
    }

    @Override
    public ObservacionDocente registrarObservacion(Long practicaId, Long docenteId, Integer corte, ObservacionRequest request) {
        InstanciaPractica practica = practicaRepository.findById(practicaId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la práctica con ID: " + practicaId));

        // Validar docente asignado
        if (practica.getDocenteAsesorId() == null || !practica.getDocenteAsesorId().equals(docenteId)) {
            throw new IllegalArgumentException("El docente no está asignado a esta práctica.");
        }

        // Validar corte en rango activo
        if (corte <= 0 || corte > practica.getNumCortes()) {
            throw new IllegalArgumentException("Corte inválido para la práctica.");
        }

        // Validar corte no cerrado
        if (esCorteCerrado(practicaId, corte)) {
            throw new IllegalStateException("El corte ya se encuentra cerrado.");
        }

        DocenteAsesor docente = docenteRepository.findById(docenteId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el docente con ID: " + docenteId));

        ObservacionDocente observacion = ObservacionDocente.builder()
                .instanciaPractica(practica)
                .docente(docente)
                .corte(corte)
                .observacion(request.observacion())
                .visibleParaEstudiante(request.visibleParaEstudiante())
                .build();

        return observacionDocenteRepository.save(observacion);
    }

    @Override
    public ObservacionDocente editarObservacion(Long id, ObservacionRequest request) {
        ObservacionDocente observacion = observacionDocenteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la observación con ID: " + id));

        // Validar corte no cerrado
        if (esCorteCerrado(observacion.getInstanciaPractica().getId(), observacion.getCorte())) {
            throw new IllegalStateException("No se puede editar una observación de un corte cerrado.");
        }

        observacion.setObservacion(request.observacion());
        observacion.setVisibleParaEstudiante(request.visibleParaEstudiante());

        return observacionDocenteRepository.save(observacion);
    }

    @Override
    public AvanceTutor registrarAvanceTutor(Long practicaId, Long tutorId, Integer corte, AvanceRequest request) {
        InstanciaPractica practica = practicaRepository.findById(practicaId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la práctica con ID: " + practicaId));

        // Validar tutor asignado
        if (practica.getTutorId() == null || !practica.getTutorId().equals(tutorId)) {
            throw new IllegalArgumentException("El tutor no está asignado a esta práctica.");
        }

        // Validar corte en rango activo
        if (corte <= 0 || corte > practica.getNumCortes()) {
            throw new IllegalArgumentException("Corte inválido para la práctica.");
        }

        // Validar corte no cerrado
        if (esCorteCerrado(practicaId, corte)) {
            throw new IllegalStateException("El corte ya se encuentra cerrado.");
        }

        TutorEmpresarial tutor = tutorRepository.findById(tutorId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el tutor con ID: " + tutorId));

        AvanceTutor avance = AvanceTutor.builder()
                .instanciaPractica(practica)
                .tutor(tutor)
                .corte(corte)
                .avance(request.avance())
                .logros(request.logros())
                .dificultades(request.dificultades())
                .build();

        return avanceTutorRepository.save(avance);
    }

    @Override
    public BitacoraEstudiante registrarBitacoraEstudiante(Long practicaId, Long estudianteId, Integer corte, BitacoraRequest request) {
        InstanciaPractica practica = practicaRepository.findById(practicaId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la práctica con ID: " + practicaId));

        // Validar estudiante dueño de la práctica
        if (practica.getExpediente() == null ||
                practica.getExpediente().getEstudiante() == null ||
                !practica.getExpediente().getEstudiante().getId().equals(estudianteId)) {
            throw new IllegalArgumentException("El estudiante no es el propietario de esta práctica.");
        }

        // Validar corte en rango activo
        if (corte <= 0 || corte > practica.getNumCortes()) {
            throw new IllegalArgumentException("Corte inválido para la práctica.");
        }

        // Validar corte no cerrado
        if (esCorteCerrado(practicaId, corte)) {
            throw new IllegalStateException("El corte ya se encuentra cerrado.");
        }

        // Validar que no edite o registre cortes anteriores
        Integer maxCorteRegistrado = jdbcTemplate.queryForObject(
                "SELECT MAX(corte) FROM bitacora_estudiante WHERE instancia_practica_id = ?",
                Integer.class,
                practicaId
        );
        if (maxCorteRegistrado != null && corte < maxCorteRegistrado) {
            throw new IllegalArgumentException("No se permite registrar o modificar bitácoras para cortes anteriores.");
        }

        Estudiante estudiante = estudianteRepository.findById(estudianteId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el estudiante con ID: " + estudianteId));

        BitacoraEstudiante bitacora = BitacoraEstudiante.builder()
                .instanciaPractica(practica)
                .estudiante(estudiante)
                .corte(corte)
                .descripcion(request.descripcion())
                .build();

        return bitacoraEstudianteRepository.save(bitacora);
    }

    @Override
    public BitacoraEstudiante adjuntarArchivoBitacora(Long bitacoraId, MultipartFile archivo) {
        BitacoraEstudiante bitacora = bitacoraEstudianteRepository.findById(bitacoraId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Bitácora no encontrada: " + bitacoraId));

        if (archivo == null || archivo.isEmpty()) {
            throw new NegocioException("El archivo no puede estar vacío.");
        }
        if (archivo.getSize() > TAMANO_MAXIMO_BYTES) {
            throw new NegocioException("El archivo supera el tamaño máximo permitido de 10 MB.");
        }
        String mime = archivo.getContentType();
        if (mime == null || !MIMES_PERMITIDOS.contains(mime.toLowerCase())) {
            throw new NegocioException("Tipo de archivo no permitido. Se aceptan PDF, Word e imágenes (JPG, PNG).");
        }

        Long practicaId = bitacora.getInstanciaPractica().getId();
        try {
            Path directorio = Paths.get(DIRECTORIO_SEGUIMIENTO, "bitacora", String.valueOf(practicaId));
            Files.createDirectories(directorio);
            String nombreSeguro = UUID.randomUUID() + "_" + archivo.getOriginalFilename();
            Path destino = directorio.resolve(nombreSeguro);
            archivo.transferTo(destino);
            bitacora.setNombreArchivo(archivo.getOriginalFilename());
            bitacora.setUrlArchivo(destino.toString().replace('\\', '/'));
        } catch (IOException ex) {
            throw new NegocioException("No se pudo guardar el archivo: " + ex.getMessage());
        }

        return bitacoraEstudianteRepository.save(bitacora);
    }

    @Override
    @Transactional(readOnly = true)
    public Resource descargarArchivoBitacora(Long bitacoraId) {
        BitacoraEstudiante bitacora = bitacoraEstudianteRepository.findById(bitacoraId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Bitácora no encontrada: " + bitacoraId));
        if (bitacora.getUrlArchivo() == null) {
            throw new RecursoNoEncontradoException("Esta bitácora no tiene archivo adjunto.");
        }
        try {
            Path base = Paths.get(DIRECTORIO_SEGUIMIENTO).toAbsolutePath().normalize();
            Path archivo = Paths.get(bitacora.getUrlArchivo()).toAbsolutePath().normalize();
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
    public String nombreArchivoBitacora(Long bitacoraId) {
        BitacoraEstudiante bitacora = bitacoraEstudianteRepository.findById(bitacoraId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Bitácora no encontrada: " + bitacoraId));
        return bitacora.getNombreArchivo() != null
                ? bitacora.getNombreArchivo()
                : "archivo_bitacora_" + bitacoraId;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TableroResponse> obtenerTableroSeguimiento(Long programaId, String filtroEmpresa, String filtroDocente, Integer filtroCorte, String filtroEstadoSeguimiento) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long estudianteId = null;
        if (auth != null && auth.isAuthenticated() && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ESTUDIANTE"))) {
            String correo = auth.getName();
            Estudiante estLogueado = estudianteRepository.findByCorreo(correo).orElse(null);
            if (estLogueado != null) {
                estudianteId = estLogueado.getId();
            }
        }
        final Long estudianteLogueadoId = estudianteId;

        // Buscar todas las prácticas en curso que correspondan al programa
        List<InstanciaPractica> practicas = practicaRepository.findAll().stream()
                .filter(p -> p.getExpediente() != null
                        && p.getExpediente().getEstudiante() != null
                        && p.getExpediente().getEstudiante().getPrograma() != null
                        && p.getExpediente().getEstudiante().getPrograma().getId().equals(programaId)
                        && p.getEstado() == com.avh.practicas.estudiante.entity.EstadoPractica.EN_CURSO
                        && (estudianteLogueadoId == null || p.getExpediente().getEstudiante().getId().equals(estudianteLogueadoId)))
                .collect(Collectors.toList());

        List<TableroResponse> tablero = new ArrayList<>();

        // Obtener el umbral de inactividad del programa o por defecto (15 días)
        Integer umbralInactividad = 15;
        try {
            umbralInactividad = jdbcTemplate.queryForObject(
                    "SELECT umbral_inactividad_dias FROM config_programas WHERE programa_id = ?",
                    Integer.class,
                    programaId
            );
            if (umbralInactividad == null) {
                umbralInactividad = 15;
            }
        } catch (Exception e) {
            // Ignorar y usar default
        }

        for (InstanciaPractica p : practicas) {
            // Nombre del estudiante
            String nombreEstudiante = p.getExpediente().getEstudiante().getNombre();

            // Nombre de la empresa
            String nombreEmpresa = "No asignada";
            if (p.getEmpresaId() != null) {
                try {
                    nombreEmpresa = jdbcTemplate.queryForObject(
                            "SELECT razon_social FROM empresas WHERE id = ?",
                            String.class,
                            p.getEmpresaId()
                    );
                } catch (Exception e) {
                    // Ignorar
                }
            }

            // Nombre del docente
            String nombreDocente = "No asignado";
            if (p.getDocenteAsesorId() != null) {
                try {
                    nombreDocente = jdbcTemplate.queryForObject(
                            "SELECT nombre FROM docentes_asesores WHERE id = ?",
                            String.class,
                            p.getDocenteAsesorId()
                    );
                } catch (Exception e) {
                    // Ignorar
                }
            }

            // Calcular fecha de la última actividad
            LocalDateTime fechaUltimaActividad = p.getFechaInicio() != null
                    ? p.getFechaInicio().atStartOfDay()
                    : LocalDateTime.now();

            // Consultar última fecha de bitácora
            Optional<BitacoraEstudiante> ultimaBitacora = bitacoraEstudianteRepository.findFirstByInstanciaPracticaIdOrderByFechaDesc(p.getId());
            if (ultimaBitacora.isPresent() && ultimaBitacora.get().getFecha().isAfter(fechaUltimaActividad)) {
                fechaUltimaActividad = ultimaBitacora.get().getFecha();
            }

            // Consultar última fecha de observación docente
            List<ObservacionDocente> observaciones = observacionDocenteRepository.findByInstanciaPracticaId(p.getId());
            if (!observaciones.isEmpty()) {
                LocalDateTime maxFechaObs = observaciones.stream()
                        .map(ObservacionDocente::getFecha)
                        .max(Comparator.naturalOrder())
                        .orElse(fechaUltimaActividad);
                if (maxFechaObs.isAfter(fechaUltimaActividad)) {
                    fechaUltimaActividad = maxFechaObs;
                }
            }

            // Consultar última fecha de avance de tutor
            List<AvanceTutor> avances = avanceTutorRepository.findByInstanciaPracticaId(p.getId());
            if (!avances.isEmpty()) {
                LocalDateTime maxFechaAvance = avances.stream()
                        .map(AvanceTutor::getFecha)
                        .max(Comparator.naturalOrder())
                        .orElse(fechaUltimaActividad);
                if (maxFechaAvance.isAfter(fechaUltimaActividad)) {
                    fechaUltimaActividad = maxFechaAvance;
                }
            }

            // Estimar el corte activo según el tiempo transcurrido
            int corteActivo = 1;
            if (p.getFechaInicio() != null && p.getFechaFin() != null && p.getNumCortes() != null && p.getDuracionSemanas() != null) {
                long semanasTranscurridas = ChronoUnit.WEEKS.between(p.getFechaInicio(), LocalDate.now());
                int semanasPorCorte = p.getDuracionSemanas() / p.getNumCortes();
                if (semanasPorCorte > 0) {
                    corteActivo = (int) (semanasTranscurridas / semanasPorCorte) + 1;
                    corteActivo = Math.max(1, Math.min(corteActivo, p.getNumCortes()));
                }
            }

            // Calcular estadoSeguimiento (AL_DIA/PENDIENTE/EN_ALERTA)
            String estadoSeguimiento = "AL_DIA";
            long diasInactividad = ChronoUnit.DAYS.between(fechaUltimaActividad, LocalDateTime.now());

            if (diasInactividad > umbralInactividad) {
                estadoSeguimiento = "EN_ALERTA";
            } else {
                // Verificar si tiene bitácora en el corte activo estimado
                List<BitacoraEstudiante> bitacorasCorte = bitacoraEstudianteRepository.findByInstanciaPracticaIdAndCorte(p.getId(), corteActivo);
                if (bitacorasCorte.isEmpty()) {
                    estadoSeguimiento = "PENDIENTE";
                }
            }

            TableroResponse response = new TableroResponse(
                    p.getId(),
                    nombreEstudiante,
                    nombreEmpresa,
                    nombreDocente,
                    corteActivo,
                    estadoSeguimiento,
                    fechaUltimaActividad,
                    p.getNumeroPractica(),
                    p.getEstado() != null ? p.getEstado().name() : null,
                    p.getExpediente().getEstudiante().getId(),
                    p.getExpediente().getEstudiante().getIdentificacion(),
                    null
            );

            // Filtrado en memoria
            boolean cumpleEmpresa = filtroEmpresa == null || response.empresa().toLowerCase().contains(filtroEmpresa.toLowerCase());
            boolean cumpleDocente = filtroDocente == null || response.docente().toLowerCase().contains(filtroDocente.toLowerCase());
            boolean cumpleCorte = filtroCorte == null || response.corte().equals(filtroCorte);
            boolean cumpleEstado = filtroEstadoSeguimiento == null || response.estadoSeguimiento().equalsIgnoreCase(filtroEstadoSeguimiento);

            if (cumpleEmpresa && cumpleDocente && cumpleCorte && cumpleEstado) {
                tablero.add(response);
            }
        }

        return tablero;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TableroResponse> obtenerPracticasSeguimiento(
            String busqueda,
            Long programaId,
            String estadoSeguimiento,
            String estadoPractica) {
        ScopePracticas scope = scopeResolver.resolver();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean esEstudiante = auth != null
                && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ESTUDIANTE"));
        boolean esTutor = auth != null
                && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("TUTOR_EMPRESARIAL"));
        boolean expedienteCompleto = esRolExpedienteCompleto(auth);

        List<InstanciaPractica> practicas = practicaRepository.findAll().stream()
                .filter(p -> practicaVisibleEnListado(p, esEstudiante, esTutor, expedienteCompleto))
                .filter(scope::esVisible)
                .filter(p -> programaId == null
                        || (p.getExpediente() != null
                            && p.getExpediente().getEstudiante() != null
                            && p.getExpediente().getEstudiante().getPrograma() != null
                            && programaId.equals(p.getExpediente().getEstudiante().getPrograma().getId())))
                .collect(Collectors.toList());

        List<TableroResponse> resultado = new ArrayList<>();
        for (InstanciaPractica p : practicas) {
            TableroResponse response = construirTableroResponseScoped(p);

            boolean cumpleBusqueda = busqueda == null || busqueda.isBlank()
                    || response.estudiante().toLowerCase().contains(busqueda.toLowerCase())
                    || response.empresa().toLowerCase().contains(busqueda.toLowerCase());
            boolean cumpleEstado = estadoSeguimiento == null || estadoSeguimiento.isBlank()
                    || response.estadoSeguimiento().equalsIgnoreCase(estadoSeguimiento);
            boolean cumpleEstadoPractica = estadoPractica == null || estadoPractica.isBlank()
                    || (response.estadoPractica() != null
                        && response.estadoPractica().equalsIgnoreCase(estadoPractica));

            if (cumpleBusqueda && cumpleEstado && cumpleEstadoPractica) {
                resultado.add(response);
            }
        }
        if (expedienteCompleto) {
            return agruparExpedientePorEstudiante(resultado);
        }
        return resultado;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TableroResponse> obtenerHistorialPracticasEstudiante(Long estudianteId) {
        if (!estudianteRepository.existsById(estudianteId)) {
            throw new RecursoNoEncontradoException("No se encontró el estudiante con id: " + estudianteId);
        }

        ScopePracticas scope = scopeResolver.resolver();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean esEstudiante = auth != null
                && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ESTUDIANTE"));
        boolean esTutor = auth != null
                && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("TUTOR_EMPRESARIAL"));
        boolean expedienteCompleto = esRolExpedienteCompleto(auth);

        return practicaRepository.findByExpedienteEstudianteIdOrderByNumeroPracticaDesc(estudianteId).stream()
                .filter(p -> practicaVisibleEnListado(p, esEstudiante, esTutor, expedienteCompleto))
                .filter(scope::esVisible)
                .map(this::construirTableroResponseScoped)
                .sorted(Comparator.comparing(
                        TableroResponse::numeroPractica,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    private List<TableroResponse> agruparExpedientePorEstudiante(List<TableroResponse> filas) {
        Map<Long, List<TableroResponse>> porEstudiante = filas.stream()
                .filter(r -> r.estudianteId() != null)
                .collect(Collectors.groupingBy(TableroResponse::estudianteId));

        List<TableroResponse> agrupadas = new ArrayList<>();
        for (List<TableroResponse> grupo : porEstudiante.values()) {
            TableroResponse principal = seleccionarPracticaPrincipal(grupo);
            agrupadas.add(new TableroResponse(
                    principal.id(),
                    principal.estudiante(),
                    principal.empresa(),
                    principal.docente(),
                    principal.corte(),
                    principal.estadoSeguimiento(),
                    principal.fechaUltimaActividad(),
                    principal.numeroPractica(),
                    principal.estadoPractica(),
                    principal.estudianteId(),
                    principal.identificacion(),
                    grupo.size()
            ));
        }
        agrupadas.sort(Comparator.comparing(TableroResponse::estudiante, String.CASE_INSENSITIVE_ORDER));
        return agrupadas;
    }

    private TableroResponse seleccionarPracticaPrincipal(List<TableroResponse> grupo) {
        return grupo.stream()
                .min(Comparator
                        .comparingInt((TableroResponse r) -> prioridadEstadoPractica(r.estadoPractica()))
                        .thenComparing(r -> r.numeroPractica() != null ? -r.numeroPractica() : 0))
                .orElse(grupo.get(0));
    }

    private int prioridadEstadoPractica(String estado) {
        if (estado == null) {
            return 99;
        }
        return switch (estado) {
            case "EN_CURSO" -> 0;
            case "ASIGNADA_PENDIENTE_INICIO" -> 1;
            case "REPROBADA" -> 2;
            case "COMPLETADA" -> 3;
            default -> 4;
        };
    }

    private boolean esRolExpedienteCompleto(Authentication auth) {
        if (auth == null) {
            return false;
        }
        return auth.getAuthorities().stream().anyMatch(a -> {
            String rol = a.getAuthority();
            return rol.equals("COORD_PRACTICA")
                    || rol.equals("ROLE_COORD_PRACTICA")
                    || rol.equals("ADMIN")
                    || rol.equals("ROLE_ADMIN")
                    || rol.equals("SECRETARIA")
                    || rol.equals("ROLE_SECRETARIA")
                    || rol.equals("COORD_ACADEMICA")
                    || rol.equals("ROLE_COORD_ACADEMICA");
        });
    }

    private boolean practicaVisibleEnListado(
            InstanciaPractica practica,
            boolean esEstudiante,
            boolean esTutor,
            boolean expedienteCompleto) {
        if (esEstudiante) {
            return true;
        }
        if (esTutor) {
            EstadoPractica estado = practica.getEstado();
            return estado == EstadoPractica.EN_CURSO || estado == EstadoPractica.ASIGNADA_PENDIENTE_INICIO;
        }
        if (expedienteCompleto) {
            EstadoPractica estado = practica.getEstado();
            return estado == EstadoPractica.EN_CURSO
                    || estado == EstadoPractica.COMPLETADA
                    || estado == EstadoPractica.REPROBADA
                    || estado == EstadoPractica.ASIGNADA_PENDIENTE_INICIO;
        }
        return practica.getEstado() == EstadoPractica.EN_CURSO;
    }

    /**
     * Copia deliberada de la lógica de categorización de obtenerTableroSeguimiento (no se
     * extrae como helper común a propósito, para no modificar el método existente ya
     * verificado). Calcula el umbral de inactividad por el programa de CADA práctica, ya
     * que aquí el scope puede abarcar varios programas a la vez.
     */
    private TableroResponse construirTableroResponseScoped(InstanciaPractica p) {
        String nombreEstudiante = p.getExpediente().getEstudiante().getNombre();

        validarVisible(p);

        String nombreEmpresa = "No asignada";
        if (p.getEmpresaId() != null) {
            try {
                nombreEmpresa = jdbcTemplate.queryForObject(
                        "SELECT razon_social FROM empresas WHERE id = ?",
                        String.class,
                        p.getEmpresaId()
                );
            } catch (Exception e) {
                // Ignorar
            }
        }

        String nombreDocente = "No asignado";
        if (p.getDocenteAsesorId() != null) {
            try {
                nombreDocente = jdbcTemplate.queryForObject(
                        "SELECT nombre FROM docentes_asesores WHERE id = ?",
                        String.class,
                        p.getDocenteAsesorId()
                );
            } catch (Exception e) {
                // Ignorar
            }
        }

        LocalDateTime fechaUltimaActividad = p.getFechaInicio() != null
                ? p.getFechaInicio().atStartOfDay()
                : LocalDateTime.now();

        Optional<BitacoraEstudiante> ultimaBitacora = bitacoraEstudianteRepository.findFirstByInstanciaPracticaIdOrderByFechaDesc(p.getId());
        if (ultimaBitacora.isPresent() && ultimaBitacora.get().getFecha().isAfter(fechaUltimaActividad)) {
            fechaUltimaActividad = ultimaBitacora.get().getFecha();
        }

        List<ObservacionDocente> observaciones = observacionDocenteRepository.findByInstanciaPracticaId(p.getId());
        if (!observaciones.isEmpty()) {
            LocalDateTime maxFechaObs = observaciones.stream()
                    .map(ObservacionDocente::getFecha)
                    .max(Comparator.naturalOrder())
                    .orElse(fechaUltimaActividad);
            if (maxFechaObs.isAfter(fechaUltimaActividad)) {
                fechaUltimaActividad = maxFechaObs;
            }
        }

        List<AvanceTutor> avances = avanceTutorRepository.findByInstanciaPracticaId(p.getId());
        if (!avances.isEmpty()) {
            LocalDateTime maxFechaAvance = avances.stream()
                    .map(AvanceTutor::getFecha)
                    .max(Comparator.naturalOrder())
                    .orElse(fechaUltimaActividad);
            if (maxFechaAvance.isAfter(fechaUltimaActividad)) {
                fechaUltimaActividad = maxFechaAvance;
            }
        }

        int corteActivo = 1;
        if (p.getFechaInicio() != null && p.getFechaFin() != null && p.getNumCortes() != null && p.getDuracionSemanas() != null) {
            long semanasTranscurridas = ChronoUnit.WEEKS.between(p.getFechaInicio(), LocalDate.now());
            int semanasPorCorte = p.getDuracionSemanas() / p.getNumCortes();
            if (semanasPorCorte > 0) {
                corteActivo = (int) (semanasTranscurridas / semanasPorCorte) + 1;
                corteActivo = Math.max(1, Math.min(corteActivo, p.getNumCortes()));
            }
        }

        Long programaIdPractica = p.getExpediente().getEstudiante().getPrograma() != null
                ? p.getExpediente().getEstudiante().getPrograma().getId()
                : null;

        Integer umbralInactividad = 15;
        if (programaIdPractica != null) {
            try {
                Integer umbral = jdbcTemplate.queryForObject(
                        "SELECT umbral_inactividad_dias FROM config_programas WHERE programa_id = ?",
                        Integer.class,
                        programaIdPractica
                );
                if (umbral != null) {
                    umbralInactividad = umbral;
                }
            } catch (Exception e) {
                // Ignorar y usar default
            }
        }

        String estadoSeguimiento = "AL_DIA";
        long diasInactividad = ChronoUnit.DAYS.between(fechaUltimaActividad, LocalDateTime.now());
        if (diasInactividad > umbralInactividad) {
            estadoSeguimiento = "EN_ALERTA";
        } else {
            List<BitacoraEstudiante> bitacorasCorte = bitacoraEstudianteRepository.findByInstanciaPracticaIdAndCorte(p.getId(), corteActivo);
            if (bitacorasCorte.isEmpty()) {
                estadoSeguimiento = "PENDIENTE";
            }
        }

        return new TableroResponse(
                p.getId(),
                nombreEstudiante,
                nombreEmpresa,
                nombreDocente,
                corteActivo,
                estadoSeguimiento,
                fechaUltimaActividad,
                p.getNumeroPractica(),
                p.getEstado() != null ? p.getEstado().name() : null,
                p.getExpediente().getEstudiante().getId(),
                p.getExpediente().getEstudiante().getIdentificacion(),
                null
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ObservacionDocente> obtenerObservacionesPorPractica(Long practicaId) {
        validarVisiblePorId(practicaId);
        return observacionDocenteRepository.findByInstanciaPracticaId(practicaId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AvanceTutor> obtenerAvancesPorPractica(Long practicaId) {
        validarVisiblePorId(practicaId);
        return avanceTutorRepository.findByInstanciaPracticaId(practicaId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BitacoraEstudiante> obtenerBitacorasPorPractica(Long practicaId) {
        validarVisiblePorId(practicaId);
        return bitacoraEstudianteRepository.findByInstanciaPracticaId(practicaId);
    }

    @Override
    @Transactional(readOnly = true)
    public com.avh.practicas.seguimiento.dto.PracticaDetalleResponse obtenerDetallePractica(Long practicaId) {
        validarVisiblePorId(practicaId);
        InstanciaPractica p = practicaRepository.findByIdWithExpedienteAndEstudiante(practicaId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la práctica con ID: " + practicaId));

        String nombreEmpresa = "No asignada";
        if (p.getEmpresaId() != null) {
            try {
                nombreEmpresa = jdbcTemplate.queryForObject(
                        "SELECT razon_social FROM empresas WHERE id = ?",
                        String.class,
                        p.getEmpresaId()
                );
            } catch (Exception e) {}
        }

        String nombreDocente = "No asignado";
        if (p.getDocenteAsesorId() != null) {
            try {
                nombreDocente = jdbcTemplate.queryForObject(
                        "SELECT nombre FROM docentes_asesores WHERE id = ?",
                        String.class,
                        p.getDocenteAsesorId()
                );
            } catch (Exception e) {}
        }

        String nombreTutor = "No asignado";
        if (p.getTutorId() != null) {
            try {
                nombreTutor = jdbcTemplate.queryForObject(
                        "SELECT nombre FROM tutores_empresariales WHERE id = ?",
                        String.class,
                        p.getTutorId()
                );
            } catch (Exception e) {}
        }

        // Timeline
        List<com.avh.practicas.seguimiento.dto.TimelineEventDto> timeline = new ArrayList<>();

        observacionDocenteRepository.findByInstanciaPracticaId(practicaId).forEach(obs -> {
            timeline.add(new com.avh.practicas.seguimiento.dto.TimelineEventDto(
                    obs.getId(), "OBSERVACION", obs.getDocente().getNombre(), obs.getFecha(), obs.getObservacion(), null
            ));
        });

        avanceTutorRepository.findByInstanciaPracticaId(practicaId).forEach(av -> {
            timeline.add(new com.avh.practicas.seguimiento.dto.TimelineEventDto(
                    av.getId(), "AVANCE_TUTOR", av.getTutor().getNombre(), av.getFecha(), av.getAvance(), av.getAvance() != null ? Integer.parseInt(av.getAvance().replaceAll("[^0-9]", "")) : 0
            ));
        });

        bitacoraEstudianteRepository.findByInstanciaPracticaId(practicaId).forEach(bit -> {
            timeline.add(new com.avh.practicas.seguimiento.dto.TimelineEventDto(
                    bit.getId(), "BITACORA", bit.getEstudiante().getNombre(), bit.getFecha(), bit.getDescripcion(), null
            ));
        });

        timeline.sort(Comparator.comparing(com.avh.practicas.seguimiento.dto.TimelineEventDto::fecha).reversed());

        int avancePromedio = 0;
        List<AvanceTutor> avances = avanceTutorRepository.findByInstanciaPracticaId(practicaId);
        if (!avances.isEmpty()) {
             avancePromedio = avances.stream()
                .map(a -> {
                    try { return Integer.parseInt(a.getAvance().replaceAll("[^0-9]", "")); }
                    catch (Exception e) { return 0; }
                })
                .max(Integer::compareTo).orElse(0);
        }

        String cargo = "Practicante";
        try {
            String cargoVacante = jdbcTemplate.queryForObject(
                    """
                    SELECT v.cargo FROM asignaciones a
                    JOIN vacantes v ON v.id = a.vacante_id
                    WHERE a.instancia_practica_id = ? AND a.estado <> 'CANCELADA'
                    LIMIT 1
                    """,
                    String.class,
                    practicaId
            );
            if (cargoVacante != null && !cargoVacante.isBlank()) {
                cargo = cargoVacante;
            }
        } catch (Exception e) {
            // Ignorar y usar valor por defecto
        }

        Double notaFinal = null;
        try {
            notaFinal = jdbcTemplate.queryForObject(
                    "SELECT nota_final FROM notas_finales WHERE instancia_practica_id = ?",
                    Double.class,
                    practicaId
            );
        } catch (Exception e) {
            // Sin nota final registrada
        }

        String programaNombre = p.getExpediente().getEstudiante().getPrograma() != null
                ? p.getExpediente().getEstudiante().getPrograma().getNombre()
                : null;

        TableroResponse tablero = construirTableroResponseScoped(p);
        String estadoSeguimiento = tablero.estadoSeguimiento();
        if (p.getEstado() == EstadoPractica.COMPLETADA
                || p.getEstado() == EstadoPractica.REPROBADA
                || p.getEstado() == EstadoPractica.CANCELADA) {
            estadoSeguimiento = p.getEstado().name();
        }

        com.avh.practicas.estudiante.dto.EstudianteDto estudianteDto = com.avh.practicas.estudiante.dto.EstudianteDto.builder()
                .nombre(p.getExpediente().getEstudiante().getNombre())
                .identificacion(p.getExpediente().getEstudiante().getIdentificacion())
                .correo(p.getExpediente().getEstudiante().getCorreo())
                .programaId(p.getExpediente().getEstudiante().getPrograma().getId())
                .build();

        return new com.avh.practicas.seguimiento.dto.PracticaDetalleResponse(
                p.getId(),
                estudianteDto,
                nombreEmpresa,
                cargo,
                nombreDocente,
                nombreTutor,
                estadoSeguimiento,
                p.getFechaInicio(),
                p.getFechaFin(),
                avancePromedio,
                timeline,
                p.getEstado() != null ? p.getEstado().name() : null,
                p.getNumeroPractica(),
                programaNombre,
                notaFinal
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlertaSistema> obtenerAlertasActivas() {
        return notificacionService.listarParaUsuarioActual(null);
    }

    private void validarVisiblePorId(Long practicaId) {
        if (!hayUsuarioAutenticado()) {
            return;
        }
        validarVisible(obtenerPracticaConScope(practicaId));
    }

    private InstanciaPractica obtenerPracticaConScope(Long practicaId) {
        return practicaRepository.findByIdWithExpedienteAndEstudiante(practicaId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontro la practica con ID: " + practicaId));
    }

    private void validarVisible(InstanciaPractica practica) {
        if (!hayUsuarioAutenticado()) {
            return;
        }
        if (!scopeResolver.resolver().esVisible(practica)) {
            throw new AccesoNoAutorizadoException("No tiene acceso a esta practica.");
        }
    }

    private boolean hayUsuarioAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal());
    }
}
