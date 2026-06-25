package com.avh.practicas.configuracion.service;

import com.avh.practicas.configuracion.entity.CatalogoItem;
import com.avh.practicas.configuracion.entity.TipoCatalogo;
import com.avh.practicas.configuracion.repository.CatalogoItemRepository;
import com.avh.practicas.empresa.repository.EmpresaRepository;
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
class CatalogoMaestroServiceImplTest {

    @Mock
    private CatalogoItemRepository catalogoItemRepository;
    @Mock
    private EmpresaRepository empresaRepository;

    @InjectMocks
    private CatalogoMaestroServiceImpl service;

    @Test
    void crear_itemNuevo_loActivaYGuarda() {
        CatalogoItem item = item(TipoCatalogo.SECTOR_ECONOMICO, "Tecnologia", false);
        when(catalogoItemRepository.existsByTipoAndNombreIgnoreCase(TipoCatalogo.SECTOR_ECONOMICO, "Tecnologia"))
                .thenReturn(false);
        when(catalogoItemRepository.save(item)).thenReturn(item);

        CatalogoItem response = service.crear(item);

        assertTrue(response.getActivo());
        verify(catalogoItemRepository).save(item);
    }

    @Test
    void crear_itemDuplicado_lanzaNegocioException() {
        CatalogoItem item = item(TipoCatalogo.SECTOR_ECONOMICO, "Tecnologia", true);
        when(catalogoItemRepository.existsByTipoAndNombreIgnoreCase(TipoCatalogo.SECTOR_ECONOMICO, "Tecnologia"))
                .thenReturn(true);

        assertThrows(NegocioException.class, () -> service.crear(item));
        verify(catalogoItemRepository, never()).save(any());
    }

    @Test
    void editar_itemExistente_actualizaNombreYTipo() {
        CatalogoItem existente = item(TipoCatalogo.SECTOR_ECONOMICO, "Viejo", true);
        CatalogoItem actualizado = item(TipoCatalogo.MODALIDAD, "Remoto", true);
        when(catalogoItemRepository.findById(4L)).thenReturn(Optional.of(existente));
        when(catalogoItemRepository.existsByTipoAndNombreIgnoreCaseAndIdNot(TipoCatalogo.MODALIDAD, "Remoto", 4L))
                .thenReturn(false);
        when(catalogoItemRepository.save(existente)).thenReturn(existente);

        CatalogoItem response = service.editar(4L, actualizado);

        assertEquals("Remoto", response.getNombre());
        assertEquals(TipoCatalogo.MODALIDAD, response.getTipo());
    }

    @Test
    void editar_itemNoExiste_lanzaRecursoNoEncontrado() {
        when(catalogoItemRepository.findById(9L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.editar(9L, item(TipoCatalogo.MODALIDAD, "Remoto", true)));
    }

    @Test
    void editar_nombreDuplicado_lanzaNegocioException() {
        CatalogoItem existente = item(TipoCatalogo.MODALIDAD, "Presencial", true);
        CatalogoItem actualizado = item(TipoCatalogo.MODALIDAD, "Remoto", true);
        when(catalogoItemRepository.findById(4L)).thenReturn(Optional.of(existente));
        when(catalogoItemRepository.existsByTipoAndNombreIgnoreCaseAndIdNot(TipoCatalogo.MODALIDAD, "Remoto", 4L))
                .thenReturn(true);

        assertThrows(NegocioException.class, () -> service.editar(4L, actualizado));
        verify(catalogoItemRepository, never()).save(any());
    }

    @Test
    void desactivar_itemSinEmpresasActivas_marcaInactivo() {
        CatalogoItem item = item(TipoCatalogo.SECTOR_ECONOMICO, "Tecnologia", true);
        when(catalogoItemRepository.findById(4L)).thenReturn(Optional.of(item));
        when(empresaRepository.existsBySectorIdAndActivoTrue(4L)).thenReturn(false);

        service.desactivar(4L);

        assertFalse(item.getActivo());
        verify(catalogoItemRepository).save(item);
    }

    @Test
    void desactivar_itemReferenciadoPorEmpresasActivas_lanzaNegocioException() {
        CatalogoItem item = item(TipoCatalogo.SECTOR_ECONOMICO, "Tecnologia", true);
        when(catalogoItemRepository.findById(4L)).thenReturn(Optional.of(item));
        when(empresaRepository.existsBySectorIdAndActivoTrue(4L)).thenReturn(true);

        assertThrows(NegocioException.class, () -> service.desactivar(4L));
        verify(catalogoItemRepository, never()).save(any());
    }

    @Test
    void activar_itemExistente_marcaActivo() {
        CatalogoItem item = item(TipoCatalogo.MODALIDAD, "Remoto", false);
        when(catalogoItemRepository.findById(4L)).thenReturn(Optional.of(item));

        service.activar(4L);

        assertTrue(item.getActivo());
        verify(catalogoItemRepository).save(item);
    }

    @Test
    void obtenerPorTipo_delegaEnRepositorio() {
        List<CatalogoItem> items = List.of(item(TipoCatalogo.MODALIDAD, "Remoto", true));
        when(catalogoItemRepository.findByTipo(TipoCatalogo.MODALIDAD)).thenReturn(items);

        assertSame(items, service.obtenerPorTipo(TipoCatalogo.MODALIDAD));
    }

    @Test
    void obtenerActivosPorTipo_delegaEnRepositorio() {
        List<CatalogoItem> items = List.of(item(TipoCatalogo.AREA_PRACTICA, "Desarrollo", true));
        when(catalogoItemRepository.findByTipoAndActivoTrue(TipoCatalogo.AREA_PRACTICA)).thenReturn(items);

        assertSame(items, service.obtenerActivosPorTipo(TipoCatalogo.AREA_PRACTICA));
    }

    private CatalogoItem item(TipoCatalogo tipo, String nombre, Boolean activo) {
        return CatalogoItem.builder()
                .tipo(tipo)
                .nombre(nombre)
                .activo(activo)
                .build();
    }
}
