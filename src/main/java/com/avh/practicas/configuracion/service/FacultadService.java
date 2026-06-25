package com.avh.practicas.configuracion.service;

import com.avh.practicas.configuracion.entity.Facultad;

import java.util.List;
import java.util.Optional;

public interface FacultadService {
    Facultad crear(Facultad facultad);
    Facultad editar(Long id, Facultad facultad);
    void desactivar(Long id);
    List<Facultad> obtenerTodas();
    Optional<Facultad> obtenerPorId(Long id);
    void activar(Long id);
}
