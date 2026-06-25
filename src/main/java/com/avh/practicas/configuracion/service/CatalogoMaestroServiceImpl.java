package com.avh.practicas.configuracion.service;

import com.avh.practicas.configuracion.entity.CatalogoItem;
import com.avh.practicas.configuracion.entity.TipoCatalogo;
import com.avh.practicas.configuracion.repository.CatalogoItemRepository;
import com.avh.practicas.empresa.repository.EmpresaRepository;
import com.avh.practicas.shared.exception.NegocioException;
import com.avh.practicas.shared.exception.RecursoNoEncontradoException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CatalogoMaestroServiceImpl implements CatalogoMaestroService {

    private final CatalogoItemRepository catalogoItemRepository;
    private final EmpresaRepository empresaRepository;

    @Override
    @Transactional
    public CatalogoItem crear(CatalogoItem item) {
        if (catalogoItemRepository.existsByTipoAndNombreIgnoreCase(item.getTipo(), item.getNombre())) {
            throw new NegocioException("Ya existe un ítem con el nombre '" + item.getNombre() + "' para el tipo de catálogo " + item.getTipo());
        }
        item.setActivo(true);
        return catalogoItemRepository.save(item);
    }

    @Override
    @Transactional
    public CatalogoItem editar(Long id, CatalogoItem itemActualizado) {
        CatalogoItem itemExistente = catalogoItemRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el ítem del catálogo con id: " + id));

        if (catalogoItemRepository.existsByTipoAndNombreIgnoreCaseAndIdNot(itemActualizado.getTipo(), itemActualizado.getNombre(), id)) {
            throw new NegocioException("Ya existe otro ítem con el nombre '" + itemActualizado.getNombre() + "' para el tipo de catálogo " + itemActualizado.getTipo());
        }

        itemExistente.setNombre(itemActualizado.getNombre());
        itemExistente.setTipo(itemActualizado.getTipo());

        if (itemActualizado.getActivo() != null) {
            if (!itemActualizado.getActivo()) {
                validarDesactivacion(id);
            }
            itemExistente.setActivo(itemActualizado.getActivo());
        }

        return catalogoItemRepository.save(itemExistente);
    }

    @Override
    @Transactional
    public void desactivar(Long id) {
        CatalogoItem item = catalogoItemRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el ítem del catálogo con id: " + id));

        validarDesactivacion(id);
        item.setActivo(false);
        catalogoItemRepository.save(item);
    }

    @Override
    @Transactional
    public void activar(Long id) {
        CatalogoItem item = catalogoItemRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el ítem del catálogo con id: " + id));
        item.setActivo(true);
        catalogoItemRepository.save(item);
    }

    @Override
    public List<CatalogoItem> obtenerTodos() {
        return catalogoItemRepository.findAll();
    }

    @Override
    public Optional<CatalogoItem> obtenerPorId(Long id) {
        return catalogoItemRepository.findById(id);
    }

    @Override
    public List<CatalogoItem> obtenerPorTipo(TipoCatalogo tipo) {
        return catalogoItemRepository.findByTipo(tipo);
    }

    @Override
    public List<CatalogoItem> obtenerActivosPorTipo(TipoCatalogo tipo) {
        return catalogoItemRepository.findByTipoAndActivoTrue(tipo);
    }

    private void validarDesactivacion(Long id) {
        if (empresaRepository.existsBySectorIdAndActivoTrue(id)) {
            throw new NegocioException("No se puede desactivar este ítem de catálogo porque está siendo referenciado por empresas activas.");
        }
    }
}
