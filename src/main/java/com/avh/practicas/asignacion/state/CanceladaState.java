package com.avh.practicas.asignacion.state;

import com.avh.practicas.asignacion.entity.EstadoAsignacion;

/**
 * Estado terminal negativo.
 * Se usa cuando la asignacion se cancela y no puede continuar vinculacion.
 */
public class CanceladaState extends EstadoAsignacionBase {
    @Override
    public EstadoAsignacion getNombre() {
        return EstadoAsignacion.CANCELADA;
    }
}
