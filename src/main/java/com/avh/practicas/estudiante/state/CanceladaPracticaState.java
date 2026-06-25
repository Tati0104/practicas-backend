package com.avh.practicas.estudiante.state;

import com.avh.practicas.estudiante.entity.EstadoPractica;

/**
 * Estado terminal para practicas canceladas.
 * Hace parte del patron State solicitado para KBM en S5.
 */
public class CanceladaPracticaState extends EstadoPracticaBase {
    @Override
    public EstadoPractica getNombre() {
        return EstadoPractica.CANCELADA;
    }
}
