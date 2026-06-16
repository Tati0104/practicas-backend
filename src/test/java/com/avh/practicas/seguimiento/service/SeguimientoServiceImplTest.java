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
                jdbcTemplate
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
}
