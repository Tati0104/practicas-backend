package com.avh.practicas.configuracion.service;

import com.avh.practicas.configuracion.entity.Facultad;
import com.avh.practicas.configuracion.repository.FacultadRepository;
import com.avh.practicas.configuracion.repository.ProgramaRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FacultadServiceImplTest {

    @Mock
    private FacultadRepository facultadRepository;
    @Mock
    private ProgramaRepository programaRepository;

    @InjectMocks
    private FacultadServiceImpl facultadService;

    @Test
    void crear_nombreNuevo_activaYGuardaFacultad() {
        Facultad facultad = Facultad.builder().nombre("Ingenieria").activo(false).build();
        when(facultadRepository.existsByNombreIgnoreCase("Ingenieria")).thenReturn(false);
        when(facultadRepository.save(facultad)).thenReturn(facultad);

        Facultad response = facultadService.crear(facultad);

        assertTrue(response.getActivo());
        verify(facultadRepository).save(facultad);
    }

    @Test
    void crear_nombreDuplicado_lanzaNegocioException() {
        Facultad facultad = Facultad.builder().nombre("Ingenieria").build();
        when(facultadRepository.existsByNombreIgnoreCase("Ingenieria")).thenReturn(true);

        assertThrows(NegocioException.class, () -> facultadService.crear(facultad));
        verify(facultadRepository, never()).save(any());
    }

    @Test
    void editar_nombreDisponible_actualizaNombre() {
        Facultad existente = Facultad.builder().id(1L).nombre("Vieja").activo(true).build();
        Facultad actualizada = Facultad.builder().nombre("Nueva").build();
        when(facultadRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(facultadRepository.existsByNombreIgnoreCaseAndIdNot("Nueva", 1L)).thenReturn(false);
        when(facultadRepository.save(existente)).thenReturn(existente);

        Facultad response = facultadService.editar(1L, actualizada);

        assertEquals("Nueva", response.getNombre());
        assertTrue(response.getActivo());
    }

    @Test
    void editar_facultadNoExiste_lanzaRecursoNoEncontrado() {
        Facultad actualizada = Facultad.builder().nombre("Nueva").build();
        when(facultadRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> facultadService.editar(99L, actualizada));
    }

    @Test
    void editar_desactivarConProgramasActivos_lanzaNegocioException() {
        Facultad existente = Facultad.builder().id(1L).nombre("Ingenieria").activo(true).build();
        Facultad actualizada = Facultad.builder().nombre("Ingenieria").activo(false).build();
        when(facultadRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(facultadRepository.existsByNombreIgnoreCaseAndIdNot("Ingenieria", 1L)).thenReturn(false);
        when(programaRepository.existsByFacultadIdAndActivoTrue(1L)).thenReturn(true);

        assertThrows(NegocioException.class, () -> facultadService.editar(1L, actualizada));
        verify(facultadRepository, never()).save(any());
    }

    @Test
    void desactivar_sinProgramasActivos_marcaInactiva() {
        Facultad facultad = Facultad.builder().id(1L).nombre("Ingenieria").activo(true).build();
        when(facultadRepository.findById(1L)).thenReturn(Optional.of(facultad));
        when(programaRepository.existsByFacultadIdAndActivoTrue(1L)).thenReturn(false);

        facultadService.desactivar(1L);

        assertFalse(facultad.getActivo());
        verify(facultadRepository).save(facultad);
    }

    @Test
    void obtenerTodas_retornaRepositorio() {
        List<Facultad> facultades = List.of(Facultad.builder().id(1L).nombre("Ingenieria").build());
        when(facultadRepository.findAll()).thenReturn(facultades);

        assertSame(facultades, facultadService.obtenerTodas());
    }
}
