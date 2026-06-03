package com.avh.practicas.estudiante.state;

import com.avh.practicas.estudiante.entity.EstadoPractica;

/**
 * Estado terminal positivo de una practica aprobada/completada.
 * Hace parte del patron State solicitado para KBM en S5.
 */
public class CompletadaState extends EstadoPracticaBase {
    @Override
    public EstadoPractica getNombre() {
        return EstadoPractica.COMPLETADA;
    }
}
