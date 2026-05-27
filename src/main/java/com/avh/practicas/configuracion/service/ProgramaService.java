package com.avh.practicas.configuracion.service;

import com.avh.practicas.configuracion.entity.Programa;

import java.util.List;
import java.util.Optional;

public interface ProgramaService {
    Programa crear(Programa programa);
    Programa editar(Long id, Programa programa);
    void desactivar(Long id);
    List<Programa> obtenerTodos();
    Optional<Programa> obtenerPorId(Long id);
    List<Programa> obtenerPorFacultad(Long facultadId);
    List<Programa> obtenerActivosPorFacultad(Long facultadId);
    void activar(Long id);
}
