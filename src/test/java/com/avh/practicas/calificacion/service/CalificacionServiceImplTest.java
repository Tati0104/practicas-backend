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
import com.avh.practicas.shared.scope.ScopePracticaResolver;
import com.avh.practicas.shared.scope.ScopePracticas;
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
    @Mock
    private ScopePracticaResolver scopeResolver;
    @Mock
    private ScopePracticas scopePracticas;

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
                jdbcTemplate,
                scopeResolver
        );
        lenient().when(scopeResolver.resolver()).thenReturn(scopePracticas);
        lenient().when(scopePracticas.esVisible(any(InstanciaPractica.class))).thenReturn(true);
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
        Long docenteAsesorId = 2L;
        NotaFinalRequest request = new NotaFinalRequest(4.0);

        com.avh.practicas.configuracion.entity.Programa programa = com.avh.practicas.configuracion.entity.Programa.builder().id(5L).build();
        Estudiante estudiante = Estudiante.builder().programa(programa).build();
        Expediente expediente = Expediente.builder().estudiante(estudiante).build();

        InstanciaPractica practica = InstanciaPractica.builder()
                .id(practicaId)
                .docenteAsesorId(docenteAsesorId)
                .expediente(expediente)
                .numCortes(3)
                .inmutable(false)
                .estado(EstadoPractica.EN_CURSO)
                .build();

        when(practicaRepository.findById(practicaId)).thenReturn(Optional.of(practica));
        when(notaFinalRepository.findByInstanciaPracticaId(practicaId)).thenReturn(Optional.empty());
        // Simular nota mínima de aprobación en BD como 3.5
        when(jdbcTemplate.queryForObject(any(String.class), eq(Double.class), any())).thenReturn(3.5);
        when(notaFinalRepository.save(any(NotaFinal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        NotaFinal result = service.registrarNotaFinal(practicaId, docenteAsesorId, request);

        // Assert
        assertNotNull(result);
        assertEquals(4.0, result.getNotaFinal());
        assertTrue(result.getAprobada());
        assertFalse(practica.getInmutable());
        assertEquals(EstadoPractica.EN_CURSO, practica.getEstado());
        verify(notaFinalRepository).save(any(NotaFinal.class));
        verify(practicaRepository, never()).save(any(InstanciaPractica.class));
    }

    @Test
    void registrarNotaDocente_ActualizaNotaExistente() {
        Long practicaId = 1L;
        Long docenteId = 2L;
        Integer corte = 1;
        NotaDocente existente = NotaDocente.builder()
                .id(9L)
                .corte(corte)
                .nota(3.0)
                .observaciones("Inicial")
                .build();
        when(practicaRepository.findById(practicaId)).thenReturn(Optional.of(practicaDocente(practicaId, docenteId)));
        when(docenteRepository.findById(docenteId)).thenReturn(Optional.of(DocenteAsesor.builder().id(docenteId).build()));
        when(notaDocenteRepository.findByInstanciaPracticaIdAndCorte(practicaId, corte)).thenReturn(Optional.of(existente));
        when(notaDocenteRepository.save(any(NotaDocente.class))).thenAnswer(invocation -> invocation.getArgument(0));

        NotaDocente result = service.registrarNotaDocente(practicaId, docenteId, corte, new NotaRequest(4.2, "Actualizada"));

        assertSame(existente, result);
        assertEquals(4.2, result.getNota());
        assertEquals("Actualizada", result.getObservaciones());
    }

    @Test
    void registrarNotaDocente_FallaPracticaCerrada() {
        Long practicaId = 1L;
        InstanciaPractica practica = practicaDocente(practicaId, 2L);
        practica.setInmutable(true);
        when(practicaRepository.findById(practicaId)).thenReturn(Optional.of(practica));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                service.registrarNotaDocente(practicaId, 2L, 1, new NotaRequest(4.0, "Cierre"))
        );

        assertTrue(exception.getMessage().contains("cerrada"));
        verifyNoInteractions(docenteRepository, notaDocenteRepository);
    }

    @Test
    void registrarNotaDocente_FallaDocenteNoAsignado() {
        Long practicaId = 1L;
        when(practicaRepository.findById(practicaId)).thenReturn(Optional.of(practicaDocente(practicaId, 2L)));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                service.registrarNotaDocente(practicaId, 99L, 1, new NotaRequest(4.0, "No asignado"))
        );

        assertTrue(exception.getMessage().contains("docente"));
        assertTrue(exception.getMessage().contains("asignado"));
        verifyNoInteractions(docenteRepository, notaDocenteRepository);
    }

    @Test
    void registrarNotaDocente_FallaCorteInvalido() {
        Long practicaId = 1L;
        Long docenteId = 2L;
        when(practicaRepository.findById(practicaId)).thenReturn(Optional.of(practicaDocente(practicaId, docenteId)));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                service.registrarNotaDocente(practicaId, docenteId, 0, new NotaRequest(4.0, "Corte"))
        );

        assertTrue(exception.getMessage().contains("corte"));
        verifyNoInteractions(docenteRepository, notaDocenteRepository);
    }

    @Test
    void registrarNotaDocente_FallaDocenteNoExiste() {
        Long practicaId = 1L;
        Long docenteId = 2L;
        when(practicaRepository.findById(practicaId)).thenReturn(Optional.of(practicaDocente(practicaId, docenteId)));
        when(docenteRepository.findById(docenteId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                service.registrarNotaDocente(practicaId, docenteId, 1, new NotaRequest(4.0, "Sin docente"))
        );

        assertTrue(exception.getMessage().contains("docente con ID: 2"));
        verifyNoInteractions(notaDocenteRepository);
    }

    @Test
    void registrarNotaTutor_Exitoso() {
        Long practicaId = 1L;
        Long tutorId = 7L;
        Integer corte = 2;
        when(practicaRepository.findById(practicaId)).thenReturn(Optional.of(practicaTutor(practicaId, tutorId)));
        when(tutorRepository.findById(tutorId)).thenReturn(Optional.of(TutorEmpresarial.builder().id(tutorId).build()));
        when(notaTutorRepository.findByInstanciaPracticaIdAndCorte(practicaId, corte)).thenReturn(Optional.empty());
        when(notaTutorRepository.save(any(NotaTutor.class))).thenAnswer(invocation -> invocation.getArgument(0));

        NotaTutor result = service.registrarNotaTutor(practicaId, tutorId, corte, new NotaRequest(4.7, "Buen avance"));

        assertEquals(4.7, result.getNota());
        assertEquals("Buen avance", result.getObservaciones());
        assertEquals(corte, result.getCorte());
    }

    @Test
    void registrarNotaTutor_ActualizaNotaExistente() {
        Long practicaId = 1L;
        Long tutorId = 7L;
        Integer corte = 1;
        NotaTutor existente = NotaTutor.builder().id(3L).corte(corte).nota(3.5).build();
        when(practicaRepository.findById(practicaId)).thenReturn(Optional.of(practicaTutor(practicaId, tutorId)));
        when(tutorRepository.findById(tutorId)).thenReturn(Optional.of(TutorEmpresarial.builder().id(tutorId).build()));
        when(notaTutorRepository.findByInstanciaPracticaIdAndCorte(practicaId, corte)).thenReturn(Optional.of(existente));
        when(notaTutorRepository.save(any(NotaTutor.class))).thenAnswer(invocation -> invocation.getArgument(0));

        NotaTutor result = service.registrarNotaTutor(practicaId, tutorId, corte, new NotaRequest(4.1, "Ajuste"));

        assertSame(existente, result);
        assertEquals(4.1, result.getNota());
        assertEquals("Ajuste", result.getObservaciones());
    }

    @Test
    void registrarNotaTutor_FallaTutorNoAsignado() {
        Long practicaId = 1L;
        when(practicaRepository.findById(practicaId)).thenReturn(Optional.of(practicaTutor(practicaId, 7L)));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                service.registrarNotaTutor(practicaId, 8L, 1, new NotaRequest(4.0, "Tutor"))
        );

        assertTrue(exception.getMessage().contains("tutor"));
        assertTrue(exception.getMessage().contains("asignado"));
        verifyNoInteractions(tutorRepository, notaTutorRepository);
    }

    @Test
    void registrarNotaTutor_FallaNotaNegativa() {
        Long practicaId = 1L;
        Long tutorId = 7L;
        when(practicaRepository.findById(practicaId)).thenReturn(Optional.of(practicaTutor(practicaId, tutorId)));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                service.registrarNotaTutor(practicaId, tutorId, 1, new NotaRequest(-0.1, "Negativa"))
        );

        assertTrue(exception.getMessage().contains("rango"));
        verifyNoInteractions(tutorRepository, notaTutorRepository);
    }

    @Test
    void registrarNotaTutor_FallaTutorNoExiste() {
        Long practicaId = 1L;
        Long tutorId = 7L;
        when(practicaRepository.findById(practicaId)).thenReturn(Optional.of(practicaTutor(practicaId, tutorId)));
        when(tutorRepository.findById(tutorId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                service.registrarNotaTutor(practicaId, tutorId, 1, new NotaRequest(4.0, "Sin tutor"))
        );

        assertTrue(exception.getMessage().contains("tutor con ID: 7"));
        verifyNoInteractions(notaTutorRepository);
    }

    @Test
    void registrarNotaFinal_FallaNotaFinalDuplicada() {
        Long practicaId = 1L;
        Long docenteId = 2L;
        when(practicaRepository.findById(practicaId)).thenReturn(Optional.of(practicaConPrograma(practicaId, docenteId)));
        when(notaFinalRepository.findByInstanciaPracticaId(practicaId)).thenReturn(Optional.of(NotaFinal.builder().id(1L).build()));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                service.registrarNotaFinal(practicaId, docenteId, new NotaFinalRequest(4.0))
        );

        assertEquals("La nota final ya fue registrada y no puede modificarse.", exception.getMessage());
        verify(notaFinalRepository, never()).save(any());
    }

    @Test
    void registrarNotaFinal_FallaDocenteNoAsignado() {
        Long practicaId = 1L;
        when(practicaRepository.findById(practicaId)).thenReturn(Optional.of(practicaConPrograma(practicaId, 2L)));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                service.registrarNotaFinal(practicaId, 99L, new NotaFinalRequest(4.0))
        );

        assertTrue(exception.getMessage().contains("docente"));
        assertTrue(exception.getMessage().contains("asignado"));
        verifyNoInteractions(notaFinalRepository);
    }

    @Test
    void registrarNotaFinal_UsaPromedioMinimoSingletonSiConsultaFalla() {
        Long practicaId = 1L;
        Long docenteId = 2L;
        GestorConfiguracion.getInstancia().setPromedioMinimo(3.5);
        when(practicaRepository.findById(practicaId)).thenReturn(Optional.of(practicaConPrograma(practicaId, docenteId)));
        when(notaFinalRepository.findByInstanciaPracticaId(practicaId)).thenReturn(Optional.empty());
        when(jdbcTemplate.queryForObject(any(String.class), eq(Double.class), any())).thenThrow(new RuntimeException("sin config"));
        when(notaFinalRepository.save(any(NotaFinal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        NotaFinal result = service.registrarNotaFinal(practicaId, docenteId, new NotaFinalRequest(3.4));

        assertEquals(3.4, result.getNotaFinal());
        assertFalse(result.getAprobada());
    }

    @Test
    void leerNotaFinal_Exitoso() {
        Long practicaId = 1L;
        NotaFinal notaFinal = NotaFinal.builder().id(1L).notaFinal(4.0).build();
        when(notaFinalRepository.findByInstanciaPracticaId(practicaId)).thenReturn(Optional.of(notaFinal));

        assertSame(notaFinal, service.leerNotaFinal(practicaId));
    }

    @Test
    void leerNotaFinal_FallaNoExiste() {
        Long practicaId = 1L;
        when(notaFinalRepository.findByInstanciaPracticaId(practicaId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                service.leerNotaFinal(practicaId)
        );

        assertTrue(exception.getMessage().contains("nota final"));
        assertTrue(exception.getMessage().contains("1"));
    }

    @Test
    void obtenerResumen_IncluyeNotasPorCorteYPromedioEstimado() {
        Long practicaId = 1L;
        when(practicaRepository.findById(practicaId)).thenReturn(Optional.of(practicaDocente(practicaId, 2L)));
        when(notaDocenteRepository.findByInstanciaPracticaId(practicaId)).thenReturn(List.of(
                NotaDocente.builder().corte(1).nota(4.0).observaciones("Docente c1").build()
        ));
        when(notaTutorRepository.findByInstanciaPracticaId(practicaId)).thenReturn(List.of(
                NotaTutor.builder().corte(1).nota(3.0).observaciones("Tutor c1").build(),
                NotaTutor.builder().corte(2).nota(5.0).observaciones("Tutor c2").build()
        ));
        when(notaFinalRepository.findByInstanciaPracticaId(practicaId)).thenReturn(Optional.of(
                NotaFinal.builder().notaFinal(4.2).aprobada(true).build()
        ));

        ResumenCalificacionesResponse result = service.obtenerResumen(practicaId);

        assertEquals(3, result.notasCortes().size());
        assertEquals(4.0, result.notasCortes().get(0).notaDocente());
        assertEquals(3.0, result.notasCortes().get(0).notaTutor());
        assertNull(result.notasCortes().get(1).notaDocente());
        assertEquals(5.0, result.notasCortes().get(1).notaTutor());
        assertEquals(4.2, result.notaFinal());
        assertTrue(result.aprobada());
        assertEquals(4.0, result.promedioEstimado());
    }

    @Test
    void obtenerResumen_SinNotasRetornaPromedioCero() {
        Long practicaId = 1L;
        when(practicaRepository.findById(practicaId)).thenReturn(Optional.of(practicaDocente(practicaId, 2L)));
        when(notaDocenteRepository.findByInstanciaPracticaId(practicaId)).thenReturn(List.of());
        when(notaTutorRepository.findByInstanciaPracticaId(practicaId)).thenReturn(List.of());
        when(notaFinalRepository.findByInstanciaPracticaId(practicaId)).thenReturn(Optional.empty());

        ResumenCalificacionesResponse result = service.obtenerResumen(practicaId);

        assertEquals(0.0, result.promedioEstimado());
        assertNull(result.notaFinal());
        assertNull(result.aprobada());
    }

    private InstanciaPractica practicaDocente(Long practicaId, Long docenteId) {
        return InstanciaPractica.builder()
                .id(practicaId)
                .docenteAsesorId(docenteId)
                .numCortes(3)
                .inmutable(false)
                .build();
    }

    private InstanciaPractica practicaTutor(Long practicaId, Long tutorId) {
        return InstanciaPractica.builder()
                .id(practicaId)
                .tutorId(tutorId)
                .numCortes(3)
                .inmutable(false)
                .build();
    }

    private InstanciaPractica practicaConPrograma(Long practicaId, Long docenteId) {
        com.avh.practicas.configuracion.entity.Programa programa = com.avh.practicas.configuracion.entity.Programa.builder().id(5L).build();
        Estudiante estudiante = Estudiante.builder().programa(programa).build();
        Expediente expediente = Expediente.builder().estudiante(estudiante).build();
        return InstanciaPractica.builder()
                .id(practicaId)
                .docenteAsesorId(docenteId)
                .expediente(expediente)
                .numCortes(3)
                .inmutable(false)
                .build();
    }
}
