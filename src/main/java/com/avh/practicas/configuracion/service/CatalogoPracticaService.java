package com.avh.practicas.configuracion.service;

import com.avh.practicas.configuracion.entity.CatalogoPractica;

import java.util.List;
import java.util.Optional;

public interface CatalogoPracticaService {
    CatalogoPractica crear(CatalogoPractica catalogoPractica);
    CatalogoPractica editar(Long id, CatalogoPractica catalogoPractica);
    void desactivar(Long id);
    List<CatalogoPractica> obtenerTodos();
    Optional<CatalogoPractica> obtenerPorId(Long id);
    List<CatalogoPractica> obtenerPorPrograma(Long programaId);
    List<CatalogoPractica> obtenerActivosPorPrograma(Long programaId);
    void activar(Long id);
}
