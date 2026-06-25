package com.avh.practicas.asignacion.state;

import com.avh.practicas.asignacion.entity.EstadoAsignacion;

/**
 * Estado terminal positivo.
 * Una vez vinculada, la asignacion no debe modificarse a otros estados.
 */
public class VinculadaState extends EstadoAsignacionBase {
    @Override
    public EstadoAsignacion getNombre() {
        return EstadoAsignacion.VINCULADA;
    }
}
