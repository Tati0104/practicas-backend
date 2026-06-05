package com.avh.practicas.vinculacion.service;

import com.avh.practicas.vinculacion.dto.AsignacionResponse;
import com.avh.practicas.vinculacion.dto.CancelarAsignacionRequest;
import com.avh.practicas.vinculacion.dto.CrearAsignacionRequest;
import com.avh.practicas.asignacion.entity.EstadoAsignacion;

public interface AsignacionService {

    AsignacionResponse crear(CrearAsignacionRequest request);

    AsignacionResponse cambiarEstado(Long asignacionId, EstadoAsignacion nuevoEstado);

    AsignacionResponse cancelar(Long asignacionId, CancelarAsignacionRequest request);
}
