package com.avh.practicas.configuracion.service;

import com.avh.practicas.configuracion.entity.Facultad;
import com.avh.practicas.configuracion.entity.Programa;
import com.avh.practicas.configuracion.repository.FacultadRepository;
import com.avh.practicas.configuracion.repository.ProgramaRepository;
import com.avh.practicas.estudiante.repository.EstudianteRepository;
import com.avh.practicas.estudiante.repository.InstanciaPracticaRepository;
import com.avh.practicas.shared.exception.NegocioException;
import com.avh.practicas.shared.exception.RecursoNoEncontradoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProgramaServiceImplTest {

    @Mock
    private ProgramaRepository programaRepository;
    @Mock
    private FacultadRepository facultadRepository;
    @Mock
    private EstudianteRepository estudianteRepository;
    @Mock
    private InstanciaPracticaRepository instanciaPracticaRepository;

    @InjectMocks
    private ProgramaServiceImpl programaService;

    @Test
    void crear_facultadActivaYNombreDisponible_activaYGuardaPrograma() {
        Facultad facultad = Facultad.builder().id(1L).nombre("Ingenieria").activo(true).build();
        Programa programa = Programa.builder().nombre("Sistemas").facultad(Facultad.builder().id(1L).build()).build();
        when(facultadRepository.findById(1L)).thenReturn(Optional.of(facultad));
        when(programaRepository.existsByNombreIgnoreCaseAndFacultadId("Sistemas", 1L)).thenReturn(false);
        when(programaRepository.save(programa)).thenReturn(programa);

        Programa response = programaService.crear(programa);

        assertSame(facultad, response.getFacultad());
        assertTrue(response.getActivo());
        verify(programaRepository).save(programa);
    }

    @Test
    void crear_facultadNoExiste_lanzaRecursoNoEncontrado() {
        Programa programa = Programa.builder().nombre("Sistemas").facultad(Facultad.builder().id(99L).build()).build();
        when(facultadRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> programaService.crear(programa));
    }

    @Test
    void crear_facultadInactiva_lanzaNegocioException() {
        Facultad facultad = Facultad.builder().id(1L).nombre("Ingenieria").activo(false).build();
        Programa programa = Programa.builder().nombre("Sistemas").facultad(Facultad.builder().id(1L).build()).build();
        when(facultadRepository.findById(1L)).thenReturn(Optional.of(facultad));

        assertThrows(NegocioException.class, () -> programaService.crear(programa));
        verify(programaRepository, never()).save(any());
    }

    @Test
    void crear_nombreDuplicadoEnFacultad_lanzaNegocioException() {
        Facultad facultad = Facultad.builder().id(1L).nombre("Ingenieria").activo(true).build();
        Programa programa = Programa.builder().nombre("Sistemas").facultad(Facultad.builder().id(1L).build()).build();
        when(facultadRepository.findById(1L)).thenReturn(Optional.of(facultad));
        when(programaRepository.existsByNombreIgnoreCaseAndFacultadId("Sistemas", 1L)).thenReturn(true);

        assertThrows(NegocioException.class, () -> programaService.crear(programa));
        verify(programaRepository, never()).save(any());
    }

    @Test
    void editar_programaExiste_actualizaCampos() {
        Programa existente = Programa.builder().id(2L).nombre("Viejo").totalPracticas(1).activo(true).build();
        Facultad facultad = Facultad.builder().id(1L).nombre("Ingenieria").activo(true).build();
        Programa actualizado = Programa.builder()
                .nombre("Nuevo")
                .facultad(Facultad.builder().id(1L).build())
                .totalPracticas(3)
                .build();
        when(programaRepository.findById(2L)).thenReturn(Optional.of(existente));
        when(facultadRepository.findById(1L)).thenReturn(Optional.of(facultad));
        when(programaRepository.existsByNombreIgnoreCaseAndFacultadIdAndIdNot("Nuevo", 1L, 2L)).thenReturn(false);
        when(programaRepository.save(existente)).thenReturn(existente);

        Programa response = programaService.editar(2L, actualizado);

        assertEquals("Nuevo", response.getNombre());
        assertSame(facultad, response.getFacultad());
        assertEquals(3, response.getTotalPracticas());
    }

    @Test
    void editar_nombreDuplicado_lanzaNegocioException() {
        Programa existente = Programa.builder().id(2L).nombre("Viejo").activo(true).build();
        Facultad facultad = Facultad.builder().id(1L).nombre("Ingenieria").activo(true).build();
        Programa actualizado = Programa.builder().nombre("Nuevo").facultad(Facultad.builder().id(1L).build()).build();
        when(programaRepository.findById(2L)).thenReturn(Optional.of(existente));
        when(facultadRepository.findById(1L)).thenReturn(Optional.of(facultad));
        when(programaRepository.existsByNombreIgnoreCaseAndFacultadIdAndIdNot("Nuevo", 1L, 2L)).thenReturn(true);

        assertThrows(NegocioException.class, () -> programaService.editar(2L, actualizado));
        verify(programaRepository, never()).save(any());
    }

    @Test
    void desactivar_conEstudiantesAsociados_lanzaNegocioException() {
        Programa programa = Programa.builder().id(2L).nombre("Sistemas").activo(true).build();
        when(programaRepository.findById(2L)).thenReturn(Optional.of(programa));
        when(estudianteRepository.existsByProgramaId(2L)).thenReturn(true);

        assertThrows(NegocioException.class, () -> programaService.desactivar(2L));
        verify(programaRepository, never()).save(any());
    }

    @Test
    void desactivar_sinDependencias_marcaInactivo() {
        Programa programa = Programa.builder().id(2L).nombre("Sistemas").activo(true).build();
        when(programaRepository.findById(2L)).thenReturn(Optional.of(programa));
        when(estudianteRepository.existsByProgramaId(2L)).thenReturn(false);
        when(instanciaPracticaRepository.existsByExpedienteEstudianteProgramaIdAndEstadoIn(eq(2L), anyList())).thenReturn(false);

        programaService.desactivar(2L);

        assertFalse(programa.getActivo());
        verify(programaRepository).save(programa);
    }

    @Test
    void activar_facultadActiva_marcaActivo() {
        Facultad facultad = Facultad.builder().id(1L).nombre("Ingenieria").activo(true).build();
        Programa programa = Programa.builder().id(2L).nombre("Sistemas").facultad(facultad).activo(false).build();
        when(programaRepository.findById(2L)).thenReturn(Optional.of(programa));

        programaService.activar(2L);

        assertTrue(programa.getActivo());
        verify(programaRepository).save(programa);
    }
}
