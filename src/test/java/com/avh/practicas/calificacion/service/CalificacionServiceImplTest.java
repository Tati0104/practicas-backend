package com.avh.practicas.calificacion.service;

import com.avh.practicas.calificacion.dto.NotaFinalRequest;
import com.avh.practicas.calificacion.dto.NotaRequest;
import com.avh.practicas.calificacion.dto.ResumenCalificacionesResponse;
import com.avh.practicas.calificacion.entity.NotaDocente;
import com.avh.practicas.calificacion.entity.NotaFinal;
import com.avh.practicas.calificacion.entity.NotaTutor;
import com.avh.practicas.calificacion.repository.NotaDocenteRepository;
import com.avh.practicas.calificacion.repository.NotaFinalRepository;
import com.avh.practicas.calificacion.repository.NotaTutorRepository;
import com.avh.practicas.estudiante.entity.DocenteAsesor;
import com.avh.practicas.estudiante.entity.Estudiante;
import com.avh.practicas.estudiante.entity.Expediente;
import com.avh.practicas.estudiante.entity.InstanciaPractica;
import com.avh.practicas.estudiante.entity.EstadoPractica;
import com.avh.practicas.estudiante.repository.DocenteAsesorRepository;
import com.avh.practicas.estudiante.repository.InstanciaPracticaRepository;
import com.avh.practicas.empresa.entity.TutorEmpresarial;
import com.avh.practicas.empresa.repository.TutorEmpresarialRepository;
import com.avh.practicas.shared.pattern.singleton.GestorConfiguracion;
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
 * Pruebas unitarias para validar las reglas de negocio de CalificacionServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class CalificacionServiceImplTest {

    @Mock
    private InstanciaPracticaRepository practicaRepository;
    @Mock
    private DocenteAsesorRepository docenteRepository;
    @Mock
    private TutorEmpresarialRepository tutorRepository;
    @Mock
    private NotaDocenteRepository notaDocenteRepository;
    @Mock
    private NotaTutorRepository notaTutorRepository;
    @Mock
    private NotaFinalRepository notaFinalRepository;
    @Mock
    private JdbcTemplate jdbcTemplate;

    private CalificacionServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new CalificacionServiceImpl(
                practicaRepository,
                docenteRepository,
                tutorRepository,
                notaDocenteRepository,
                notaTutorRepository,
                notaFinalRepository,
                jdbcTemplate
        );
        // Reset singleton configuration if needed
        GestorConfiguracion.getInstancia().setMaxNota(5.0);
    }

    @Test
    void registrarNotaDocente_Exitoso() {
        // Arrange
        Long practicaId = 1L;
        Long docenteId = 2L;
        Integer corte = 2;
        NotaRequest request = new NotaRequest(4.5, "Buen desempeño");

        InstanciaPractica practica = InstanciaPractica.builder()
                .id(practicaId)
                .docenteAsesorId(docenteId)
                .numCortes(3)
                .inmutable(false)
                .build();

        DocenteAsesor docente = DocenteAsesor.builder().id(docenteId).build();

        when(practicaRepository.findById(practicaId)).thenReturn(Optional.of(practica));
        when(docenteRepository.findById(docenteId)).thenReturn(Optional.of(docente));
        when(notaDocenteRepository.findByInstanciaPracticaIdAndCorte(practicaId, corte)).thenReturn(Optional.empty());
        when(notaDocenteRepository.save(any(NotaDocente.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        NotaDocente result = service.registrarNotaDocente(practicaId, docenteId, corte, request);

        // Assert
        assertNotNull(result);
        assertEquals(4.5, result.getNota());
        assertEquals("Buen desempeño", result.getObservaciones());
        assertEquals(corte, result.getCorte());
        verify(notaDocenteRepository).save(any(NotaDocente.class));
    }

    @Test
    void registrarNotaDocente_FallaExcedeNotaMaxima() {
        // Arrange
        Long practicaId = 1L;
        Long docenteId = 2L;
        Integer corte = 2;
        NotaRequest request = new NotaRequest(5.5, "Desempeño superior"); // Excede el maxNota de 5.0

        InstanciaPractica practica = InstanciaPractica.builder()
                .id(practicaId)
                .docenteAsesorId(docenteId)
                .numCortes(3)
                .inmutable(false)
                .build();

        when(practicaRepository.findById(practicaId)).thenReturn(Optional.of(practica));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                service.registrarNotaDocente(practicaId, docenteId, corte, request)
        );
        assertTrue(exception.getMessage().contains("La calificación debe estar en el rango"));
        verifyNoInteractions(notaDocenteRepository);
    }

    @Test
    void registrarNotaFinal_ExitosoYAprobado() {
        // Arrange
        Long practicaId = 1L;
        Long coordinadorId = 4L;
        NotaFinalRequest request = new NotaFinalRequest(4.0);

        com.avh.practicas.configuracion.entity.Programa programa = com.avh.practicas.configuracion.entity.Programa.builder().id(5L).build();
        Estudiante estudiante = Estudiante.builder().programa(programa).build();
        Expediente expediente = Expediente.builder().estudiante(estudiante).build();

        InstanciaPractica practica = InstanciaPractica.builder()
                .id(practicaId)
                .expediente(expediente)
                .numCortes(3)
                .inmutable(false)
                .estado(EstadoPractica.EN_CURSO)
                .build();

        when(practicaRepository.findById(practicaId)).thenReturn(Optional.of(practica));
        // Simular nota mínima de aprobación en BD como 3.5
        when(jdbcTemplate.queryForObject(any(String.class), eq(Double.class), any())).thenReturn(3.5);
        when(notaFinalRepository.save(any(NotaFinal.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(practicaRepository.save(any(InstanciaPractica.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        NotaFinal result = service.registrarNotaFinal(practicaId, coordinadorId, request);

        // Assert
        assertNotNull(result);
        assertEquals(4.0, result.getNotaFinal());
        assertTrue(result.getAprobada());
        assertTrue(practica.getInmutable());
        assertEquals(EstadoPractica.COMPLETADA, practica.getEstado());
        verify(practicaRepository).save(practica);
        verify(notaFinalRepository).save(any(NotaFinal.class));
    }
}
