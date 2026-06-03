package com.avh.practicas.configuracion.service;

import com.avh.practicas.configuracion.entity.CatalogoItem;
import com.avh.practicas.configuracion.entity.TipoCatalogo;

import java.util.List;
import java.util.Optional;

public interface CatalogoMaestroService {
    CatalogoItem crear(CatalogoItem item);
    CatalogoItem editar(Long id, CatalogoItem item);
    void desactivar(Long id);
    List<CatalogoItem> obtenerTodos();
    Optional<CatalogoItem> obtenerPorId(Long id);
    List<CatalogoItem> obtenerPorTipo(TipoCatalogo tipo);
    List<CatalogoItem> obtenerActivosPorTipo(TipoCatalogo tipo);
    void activar(Long id);
}
