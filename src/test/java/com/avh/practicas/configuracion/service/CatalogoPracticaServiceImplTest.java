package com.avh.practicas.configuracion.service;

import com.avh.practicas.configuracion.entity.CatalogoPractica;
import com.avh.practicas.configuracion.entity.Programa;
import com.avh.practicas.configuracion.repository.CatalogoPracticaRepository;
import com.avh.practicas.configuracion.repository.ProgramaRepository;
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
class CatalogoPracticaServiceImplTest {

    @Mock
    private CatalogoPracticaRepository catalogoPracticaRepository;
    @Mock
    private ProgramaRepository programaRepository;
    @Mock
    private InstanciaPracticaRepository instanciaPracticaRepository;

    @InjectMocks
    private CatalogoPracticaServiceImpl service;

    @Test
    void crear_programaActivoYNumeroDisponible_guardaActivo() {
        Programa programa = programa(true);
        CatalogoPractica catalogo = catalogo(1L, 1, false);
        when(programaRepository.findById(1L)).thenReturn(Optional.of(programa));
        when(catalogoPracticaRepository.existsByProgramaIdAndNumeroPractica(1L, 1)).thenReturn(false);
        when(catalogoPracticaRepository.save(catalogo)).thenReturn(catalogo);

        CatalogoPractica response = service.crear(catalogo);

        assertSame(programa, response.getPrograma());
        assertTrue(response.getActivo());
        verify(catalogoPracticaRepository).save(catalogo);
    }

    @Test
    void crear_programaNoExiste_lanzaRecursoNoEncontrado() {
        CatalogoPractica catalogo = catalogo(99L, 1, true);
        when(programaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.crear(catalogo));
        verify(catalogoPracticaRepository, never()).save(any());
    }

    @Test
    void crear_programaInactivo_lanzaNegocioException() {
        CatalogoPractica catalogo = catalogo(1L, 1, true);
        when(programaRepository.findById(1L)).thenReturn(Optional.of(programa(false)));

        assertThrows(NegocioException.class, () -> service.crear(catalogo));
        verify(catalogoPracticaRepository, never()).save(any());
    }

    @Test
    void crear_numeroDuplicadoEnPrograma_lanzaNegocioException() {
        CatalogoPractica catalogo = catalogo(1L, 2, true);
        when(programaRepository.findById(1L)).thenReturn(Optional.of(programa(true)));
        when(catalogoPracticaRepository.existsByProgramaIdAndNumeroPractica(1L, 2)).thenReturn(true);

        assertThrows(NegocioException.class, () -> service.crear(catalogo));
        verify(catalogoPracticaRepository, never()).save(any());
    }

    @Test
    void editar_catalogoExistente_actualizaCampos() {
        CatalogoPractica existente = catalogo(1L, 1, true);
        existente.setId(5L);
        CatalogoPractica actualizado = catalogo(2L, 2, true);
        actualizado.setNombre("Practica II");
        Programa nuevoPrograma = Programa.builder().id(2L).nombre("Industrial").activo(true).build();
        when(catalogoPracticaRepository.findById(5L)).thenReturn(Optional.of(existente));
        when(programaRepository.findById(2L)).thenReturn(Optional.of(nuevoPrograma));
        when(catalogoPracticaRepository.existsByProgramaIdAndNumeroPracticaAndIdNot(2L, 2, 5L)).thenReturn(false);
        when(catalogoPracticaRepository.save(existente)).thenReturn(existente);

        CatalogoPractica response = service.editar(5L, actualizado);

        assertEquals("Practica II", response.getNombre());
        assertEquals(2, response.getNumeroPractica());
        assertSame(nuevoPrograma, response.getPrograma());
    }

    @Test
    void editar_catalogoNoExiste_lanzaRecursoNoEncontrado() {
        when(catalogoPracticaRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.editar(5L, catalogo(1L, 1, true)));
    }

    @Test
    void editar_numeroDuplicado_lanzaNegocioException() {
        CatalogoPractica existente = catalogo(1L, 1, true);
        existente.setId(5L);
        CatalogoPractica actualizado = catalogo(1L, 2, true);
        when(catalogoPracticaRepository.findById(5L)).thenReturn(Optional.of(existente));
        when(programaRepository.findById(1L)).thenReturn(Optional.of(programa(true)));
        when(catalogoPracticaRepository.existsByProgramaIdAndNumeroPracticaAndIdNot(1L, 2, 5L)).thenReturn(true);

        assertThrows(NegocioException.class, () -> service.editar(5L, actualizado));
        verify(catalogoPracticaRepository, never()).save(any());
    }

    @Test
    void desactivar_sinInstanciasActivas_marcaInactivo() {
        CatalogoPractica catalogo = catalogo(1L, 1, true);
        when(catalogoPracticaRepository.findById(5L)).thenReturn(Optional.of(catalogo));
        when(instanciaPracticaRepository.existsByExpedienteEstudianteProgramaIdAndNumeroPracticaAndEstadoIn(eq(1L), eq(1), anyList()))
                .thenReturn(false);

        service.desactivar(5L);

        assertFalse(catalogo.getActivo());
        verify(catalogoPracticaRepository).save(catalogo);
    }

    @Test
    void activar_programaInactivo_lanzaNegocioException() {
        CatalogoPractica catalogo = catalogo(1L, 1, false);
        catalogo.getPrograma().setActivo(false);
        when(catalogoPracticaRepository.findById(5L)).thenReturn(Optional.of(catalogo));

        assertThrows(NegocioException.class, () -> service.activar(5L));
        verify(catalogoPracticaRepository, never()).save(any());
    }

    @Test
    void obtenerActivosPorPrograma_delegaEnRepositorio() {
        List<CatalogoPractica> catalogos = List.of(catalogo(1L, 1, true));
        when(catalogoPracticaRepository.findByProgramaIdAndActivoTrue(1L)).thenReturn(catalogos);

        assertSame(catalogos, service.obtenerActivosPorPrograma(1L));
    }

    private CatalogoPractica catalogo(Long programaId, Integer numero, Boolean activo) {
        return CatalogoPractica.builder()
                .programa(Programa.builder().id(programaId).nombre("Sistemas").activo(true).build())
                .numeroPractica(numero)
                .nombre("Practica I")
                .materiaNucleo("Nucleo")
                .codigoMateria("PR" + numero)
                .numCortes(3)
                .duracionSemanas(16)
                .activo(activo)
                .build();
    }

    private Programa programa(Boolean activo) {
        return Programa.builder().id(1L).nombre("Sistemas").activo(activo).build();
    }
}
