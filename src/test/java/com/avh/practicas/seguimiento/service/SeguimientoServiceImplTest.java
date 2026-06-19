package com.avh.practicas.seguimiento.service;

import com.avh.practicas.empresa.entity.TutorEmpresarial;
import com.avh.practicas.empresa.repository.TutorEmpresarialRepository;
import com.avh.practicas.estudiante.entity.DocenteAsesor;
import com.avh.practicas.estudiante.entity.Estudiante;
import com.avh.practicas.estudiante.entity.Expediente;
import com.avh.practicas.estudiante.entity.InstanciaPractica;
import com.avh.practicas.estudiante.entity.EstadoPractica;
import com.avh.practicas.estudiante.repository.DocenteAsesorRepository;
import com.avh.practicas.estudiante.repository.EstudianteRepository;
import com.avh.practicas.estudiante.repository.InstanciaPracticaRepository;
import com.avh.practicas.seguimiento.dto.AvanceRequest;
import com.avh.practicas.seguimiento.dto.BitacoraRequest;
import com.avh.practicas.seguimiento.dto.ObservacionRequest;
import com.avh.practicas.seguimiento.entity.AvanceTutor;
import com.avh.practicas.seguimiento.entity.BitacoraEstudiante;
import com.avh.practicas.seguimiento.entity.ObservacionDocente;
import com.avh.practicas.notificacion.service.NotificacionService;
import com.avh.practicas.shared.scope.ScopePracticaResolver;
import com.avh.practicas.seguimiento.repository.AlertaSistemaRepository;
import com.avh.practicas.seguimiento.repository.AvanceTutorRepository;
import com.avh.practicas.seguimiento.repository.BitacoraEstudianteRepository;
import com.avh.practicas.seguimiento.repository.ObservacionDocenteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para validar las reglas de negocio de SeguimientoServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class SeguimientoServiceImplTest {

    @Mock
    private InstanciaPracticaRepository practicaRepository;
    @Mock
    private DocenteAsesorRepository docenteRepository;
    @Mock
    private TutorEmpresarialRepository tutorRepository;
    @Mock
    private EstudianteRepository estudianteRepository;
    @Mock
    private ObservacionDocenteRepository observacionDocenteRepository;
    @Mock
    private AvanceTutorRepository avanceTutorRepository;
    @Mock
    private BitacoraEstudianteRepository bitacoraEstudianteRepository;
    @Mock
    private AlertaSistemaRepository alertaSistemaRepository;
    @Mock
    private NotificacionService notificacionService;
    @Mock
    private JdbcTemplate jdbcTemplate;
    @Mock
    private ScopePracticaResolver scopePracticaResolver;

    private SeguimientoServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new SeguimientoServiceImpl(
                practicaRepository,
                docenteRepository,
                tutorRepository,
                estudianteRepository,
                observacionDocenteRepository,
                avanceTutorRepository,
                bitacoraEstudianteRepository,
                alertaSistemaRepository,
                notificacionService,
                jdbcTemplate,
                scopePracticaResolver
        );
    }

    @Test
    void registrarObservacion_Exitoso() {
        // Arrange
        Long practicaId = 1L;
        Long docenteId = 2L;
        Integer corte = 1;
        ObservacionRequest request = new ObservacionRequest("Todo excelente", true);

        InstanciaPractica practica = InstanciaPractica.builder()
                .id(practicaId)
                .docenteAsesorId(docenteId)
                .numCortes(3)
                .estado(EstadoPractica.EN_CURSO)
                .build();

        DocenteAsesor docente = DocenteAsesor.builder()
                .id(docenteId)
                .nombre("Juan Perez")
                .build();

        when(practicaRepository.findById(practicaId)).thenReturn(Optional.of(practica));
        when(docenteRepository.findById(docenteId)).thenReturn(Optional.of(docente));
        when(jdbcTemplate.queryForObject(any(String.class), eq(Boolean.class), eq(practicaId))).thenReturn(false);
        when(jdbcTemplate.queryForObject(any(String.class), eq(Integer.class), eq(practicaId))).thenReturn(0);
        when(jdbcTemplate.queryForObject(any(String.class), eq(Integer.class), eq(practicaId), eq(corte))).thenReturn(0);
        when(observacionDocenteRepository.save(any(ObservacionDocente.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ObservacionDocente result = service.registrarObservacion(practicaId, docenteId, corte, request);

        // Assert
        assertNotNull(result);
        assertEquals("Todo excelente", result.getObservacion());
        assertTrue(result.getVisibleParaEstudiante());
        assertEquals(corte, result.getCorte());
        verify(observacionDocenteRepository).save(any(ObservacionDocente.class));
    }

    @Test
    void registrarObservacion_FallaDocenteNoAsignado() {
        // Arrange
        Long practicaId = 1L;
        Long docenteId = 2L;
        Integer corte = 1;
        ObservacionRequest request = new ObservacionRequest("Todo excelente", true);

        InstanciaPractica practica = InstanciaPractica.builder()
                .id(practicaId)
                .docenteAsesorId(99L) // Diferente de docenteId
                .numCortes(3)
                .build();

        when(practicaRepository.findById(practicaId)).thenReturn(Optional.of(practica));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                service.registrarObservacion(practicaId, docenteId, corte, request)
        );
        assertEquals("El docente no está asignado a esta práctica.", exception.getMessage());
        verifyNoInteractions(observacionDocenteRepository);
    }

    @Test
    void registrarBitacoraEstudiante_FallaCortesAnteriores() {
        // Arrange
        Long practicaId = 1L;
        Long estudianteId = 3L;
        Integer corte = 1; // Intentando registrar para corte 1
        BitacoraRequest request = new BitacoraRequest("Mi reporte");

        Estudiante estudiante = Estudiante.builder().id(estudianteId).nombre("Ana").build();
        Expediente expediente = Expediente.builder().estudiante(estudiante).build();

        InstanciaPractica practica = InstanciaPractica.builder()
                .id(practicaId)
                .expediente(expediente)
                .numCortes(3)
                .build();

        when(practicaRepository.findById(practicaId)).thenReturn(Optional.of(practica));
        when(jdbcTemplate.queryForObject(any(String.class), eq(Boolean.class), eq(practicaId))).thenReturn(false);
        when(jdbcTemplate.queryForObject(any(String.class), eq(Integer.class), eq(practicaId))).thenReturn(0);
        when(jdbcTemplate.queryForObject(any(String.class), eq(Integer.class), eq(practicaId), eq(corte))).thenReturn(0);
        
        // Simular que el estudiante ya tiene bitácora en corte 2 (max es 2)
        when(jdbcTemplate.queryForObject(contains("MAX(corte)"), eq(Integer.class), eq(practicaId))).thenReturn(2);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                service.registrarBitacoraEstudiante(practicaId, estudianteId, corte, request)
        );
        assertEquals("No se permite registrar o modificar bitácoras para cortes anteriores.", exception.getMessage());
    }

    @Test
    void registrarObservacion_FallaPracticaNoExiste() {
        Long practicaId = 404L;
        when(practicaRepository.findById(practicaId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                service.registrarObservacion(practicaId, 2L, 1, new ObservacionRequest("Seguimiento", true))
        );

        assertTrue(exception.getMessage().contains("ID: 404"));
        verifyNoInteractions(docenteRepository, observacionDocenteRepository);
    }

    @Test
    void registrarObservacion_FallaCorteCero() {
        Long practicaId = 1L;
        Long docenteId = 2L;
        when(practicaRepository.findById(practicaId)).thenReturn(Optional.of(practicaDocente(practicaId, docenteId)));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                service.registrarObservacion(practicaId, docenteId, 0, new ObservacionRequest("Seguimiento", true))
        );

        assertTrue(exception.getMessage().contains("Corte"));
        verifyNoInteractions(jdbcTemplate, docenteRepository, observacionDocenteRepository);
    }

    @Test
    void registrarObservacion_FallaCorteMayorANumeroDeCortes() {
        Long practicaId = 1L;
        Long docenteId = 2L;
        when(practicaRepository.findById(practicaId)).thenReturn(Optional.of(practicaDocente(practicaId, docenteId)));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                service.registrarObservacion(practicaId, docenteId, 4, new ObservacionRequest("Seguimiento", true))
        );

        assertTrue(exception.getMessage().contains("Corte"));
        verifyNoInteractions(jdbcTemplate, docenteRepository, observacionDocenteRepository);
    }

    @Test
    void registrarObservacion_FallaCorteCerradoPorPracticaInmutable() {
        Long practicaId = 1L;
        Long docenteId = 2L;
        when(practicaRepository.findById(practicaId)).thenReturn(Optional.of(practicaDocente(practicaId, docenteId)));
        when(jdbcTemplate.queryForObject(contains("SELECT inmutable"), eq(Boolean.class), eq(practicaId))).thenReturn(true);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                service.registrarObservacion(practicaId, docenteId, 1, new ObservacionRequest("Seguimiento", true))
        );

        assertEquals("El corte ya se encuentra cerrado.", exception.getMessage());
        verifyNoInteractions(docenteRepository, observacionDocenteRepository);
    }

    @Test
    void registrarObservacion_FallaDocenteNoExiste() {
        Long practicaId = 1L;
        Long docenteId = 2L;
        Integer corte = 1;
        when(practicaRepository.findById(practicaId)).thenReturn(Optional.of(practicaDocente(practicaId, docenteId)));
        corteAbierto(practicaId, corte);
        when(docenteRepository.findById(docenteId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                service.registrarObservacion(practicaId, docenteId, corte, new ObservacionRequest("Seguimiento", true))
        );

        assertTrue(exception.getMessage().contains("docente con ID: 2"));
        verifyNoInteractions(observacionDocenteRepository);
    }

    @Test
    void editarObservacion_Exitoso() {
        Long observacionId = 5L;
        ObservacionDocente observacion = ObservacionDocente.builder()
                .id(observacionId)
                .instanciaPractica(practicaDocente(1L, 2L))
                .corte(1)
                .observacion("Inicial")
                .visibleParaEstudiante(true)
                .build();
        when(observacionDocenteRepository.findById(observacionId)).thenReturn(Optional.of(observacion));
        corteAbierto(1L, 1);
        when(observacionDocenteRepository.save(any(ObservacionDocente.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ObservacionDocente result = service.editarObservacion(observacionId, new ObservacionRequest("Actualizada", false));

        assertEquals("Actualizada", result.getObservacion());
        assertFalse(result.getVisibleParaEstudiante());
        verify(observacionDocenteRepository).save(observacion);
    }

    @Test
    void editarObservacion_FallaNoExiste() {
        Long observacionId = 5L;
        when(observacionDocenteRepository.findById(observacionId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                service.editarObservacion(observacionId, new ObservacionRequest("Actualizada", false))
        );

        assertTrue(exception.getMessage().contains("ID: 5"));
        verifyNoInteractions(jdbcTemplate);
    }

    @Test
    void editarObservacion_FallaCorteCerradoPorNotaFinal() {
        Long observacionId = 5L;
        ObservacionDocente observacion = ObservacionDocente.builder()
                .id(observacionId)
                .instanciaPractica(practicaDocente(1L, 2L))
                .corte(1)
                .build();
        when(observacionDocenteRepository.findById(observacionId)).thenReturn(Optional.of(observacion));
        when(jdbcTemplate.queryForObject(contains("SELECT inmutable"), eq(Boolean.class), eq(1L))).thenReturn(false);
        when(jdbcTemplate.queryForObject(contains("notas_finales"), eq(Integer.class), eq(1L))).thenReturn(1);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                service.editarObservacion(observacionId, new ObservacionRequest("Actualizada", false))
        );

        assertTrue(exception.getMessage().contains("corte cerrado"));
        verify(observacionDocenteRepository, never()).save(any());
    }

    @Test
    void registrarAvanceTutor_Exitoso() {
        Long practicaId = 1L;
        Long tutorId = 7L;
        Integer corte = 2;
        when(practicaRepository.findById(practicaId)).thenReturn(Optional.of(practicaTutor(practicaId, tutorId)));
        corteAbierto(practicaId, corte);
        when(tutorRepository.findById(tutorId)).thenReturn(Optional.of(TutorEmpresarial.builder().id(tutorId).nombre("Tutor").build()));
        when(avanceTutorRepository.save(any(AvanceTutor.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AvanceTutor result = service.registrarAvanceTutor(
                practicaId,
                tutorId,
                corte,
                new AvanceRequest("60%", "Entrega parcial", "Ninguna")
        );

        assertEquals("60%", result.getAvance());
        assertEquals("Entrega parcial", result.getLogros());
        assertEquals("Ninguna", result.getDificultades());
        assertEquals(corte, result.getCorte());
    }

    @Test
    void registrarAvanceTutor_FallaTutorNoAsignado() {
        Long practicaId = 1L;
        when(practicaRepository.findById(practicaId)).thenReturn(Optional.of(practicaTutor(practicaId, 7L)));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                service.registrarAvanceTutor(practicaId, 8L, 1, new AvanceRequest("20%", "Inicio", null))
        );

        assertTrue(exception.getMessage().contains("tutor"));
        assertTrue(exception.getMessage().contains("asignado"));
        verifyNoInteractions(jdbcTemplate, tutorRepository, avanceTutorRepository);
    }

    @Test
    void registrarAvanceTutor_FallaCorteInvalido() {
        Long practicaId = 1L;
        Long tutorId = 7L;
        when(practicaRepository.findById(practicaId)).thenReturn(Optional.of(practicaTutor(practicaId, tutorId)));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                service.registrarAvanceTutor(practicaId, tutorId, 9, new AvanceRequest("20%", "Inicio", null))
        );

        assertTrue(exception.getMessage().contains("Corte"));
        verifyNoInteractions(jdbcTemplate, tutorRepository, avanceTutorRepository);
    }

    @Test
    void registrarAvanceTutor_FallaTutorNoExiste() {
        Long practicaId = 1L;
        Long tutorId = 7L;
        Integer corte = 1;
        when(practicaRepository.findById(practicaId)).thenReturn(Optional.of(practicaTutor(practicaId, tutorId)));
        corteAbierto(practicaId, corte);
        when(tutorRepository.findById(tutorId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                service.registrarAvanceTutor(practicaId, tutorId, corte, new AvanceRequest("20%", "Inicio", null))
        );

        assertTrue(exception.getMessage().contains("tutor con ID: 7"));
        verifyNoInteractions(avanceTutorRepository);
    }

    @Test
    void registrarBitacoraEstudiante_Exitoso() {
        Long practicaId = 1L;
        Long estudianteId = 3L;
        Integer corte = 2;
        when(practicaRepository.findById(practicaId)).thenReturn(Optional.of(practicaEstudiante(practicaId, estudianteId)));
        corteAbierto(practicaId, corte);
        when(jdbcTemplate.queryForObject(contains("MAX(corte)"), eq(Integer.class), eq(practicaId))).thenReturn(1);
        when(estudianteRepository.findById(estudianteId)).thenReturn(Optional.of(Estudiante.builder().id(estudianteId).nombre("Ana").build()));
        when(bitacoraEstudianteRepository.save(any(BitacoraEstudiante.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BitacoraEstudiante result = service.registrarBitacoraEstudiante(
                practicaId,
                estudianteId,
                corte,
                new BitacoraRequest("Actividades de la semana")
        );

        assertEquals("Actividades de la semana", result.getDescripcion());
        assertEquals(corte, result.getCorte());
        assertEquals(estudianteId, result.getEstudiante().getId());
    }

    @Test
    void registrarBitacoraEstudiante_FallaNoEsPropietario() {
        Long practicaId = 1L;
        when(practicaRepository.findById(practicaId)).thenReturn(Optional.of(practicaEstudiante(practicaId, 3L)));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                service.registrarBitacoraEstudiante(practicaId, 4L, 1, new BitacoraRequest("Actividad"))
        );

        assertTrue(exception.getMessage().contains("propietario"));
        verifyNoInteractions(jdbcTemplate, estudianteRepository, bitacoraEstudianteRepository);
    }

    @Test
    void registrarBitacoraEstudiante_FallaEstudianteNoExiste() {
        Long practicaId = 1L;
        Long estudianteId = 3L;
        Integer corte = 1;
        when(practicaRepository.findById(practicaId)).thenReturn(Optional.of(practicaEstudiante(practicaId, estudianteId)));
        corteAbierto(practicaId, corte);
        when(jdbcTemplate.queryForObject(contains("MAX(corte)"), eq(Integer.class), eq(practicaId))).thenReturn(null);
        when(estudianteRepository.findById(estudianteId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                service.registrarBitacoraEstudiante(practicaId, estudianteId, corte, new BitacoraRequest("Actividad"))
        );

        assertTrue(exception.getMessage().contains("estudiante con ID: 3"));
        verifyNoInteractions(bitacoraEstudianteRepository);
    }

    @Test
    void obtenerObservacionesPorPractica_DelegaEnRepositorio() {
        Long practicaId = 1L;
        List<ObservacionDocente> observaciones = List.of(ObservacionDocente.builder().id(1L).build());
        when(observacionDocenteRepository.findByInstanciaPracticaId(practicaId)).thenReturn(observaciones);

        assertSame(observaciones, service.obtenerObservacionesPorPractica(practicaId));
    }

    @Test
    void obtenerAvancesPorPractica_DelegaEnRepositorio() {
        Long practicaId = 1L;
        List<AvanceTutor> avances = List.of(AvanceTutor.builder().id(1L).build());
        when(avanceTutorRepository.findByInstanciaPracticaId(practicaId)).thenReturn(avances);

        assertSame(avances, service.obtenerAvancesPorPractica(practicaId));
    }

    @Test
    void obtenerBitacorasPorPractica_DelegaEnRepositorio() {
        Long practicaId = 1L;
        List<BitacoraEstudiante> bitacoras = List.of(BitacoraEstudiante.builder().id(1L).build());
        when(bitacoraEstudianteRepository.findByInstanciaPracticaId(practicaId)).thenReturn(bitacoras);

        assertSame(bitacoras, service.obtenerBitacorasPorPractica(practicaId));
    }

    @Test
    void obtenerAlertasActivas_UsaNotificacionService() {
        when(notificacionService.listarParaUsuarioActual(null)).thenReturn(List.of());

        assertTrue(service.obtenerAlertasActivas().isEmpty());
        verify(notificacionService).listarParaUsuarioActual(null);
    }

    private InstanciaPractica practicaDocente(Long practicaId, Long docenteId) {
        return InstanciaPractica.builder()
                .id(practicaId)
                .docenteAsesorId(docenteId)
                .numCortes(3)
                .build();
    }

    private InstanciaPractica practicaTutor(Long practicaId, Long tutorId) {
        return InstanciaPractica.builder()
                .id(practicaId)
                .tutorId(tutorId)
                .numCortes(3)
                .build();
    }

    private InstanciaPractica practicaEstudiante(Long practicaId, Long estudianteId) {
        Estudiante estudiante = Estudiante.builder().id(estudianteId).nombre("Ana").build();
        Expediente expediente = Expediente.builder().estudiante(estudiante).build();
        return InstanciaPractica.builder()
                .id(practicaId)
                .expediente(expediente)
                .numCortes(3)
                .build();
    }

    private void corteAbierto(Long practicaId, Integer corte) {
        when(jdbcTemplate.queryForObject(contains("SELECT inmutable"), eq(Boolean.class), eq(practicaId))).thenReturn(false);
        when(jdbcTemplate.queryForObject(contains("notas_finales"), eq(Integer.class), eq(practicaId))).thenReturn(0);
        when(jdbcTemplate.queryForObject(contains("notas_docente"), eq(Integer.class), eq(practicaId), eq(corte))).thenReturn(0);
        when(jdbcTemplate.queryForObject(contains("notas_tutor"), eq(Integer.class), eq(practicaId), eq(corte))).thenReturn(0);
    }
}
