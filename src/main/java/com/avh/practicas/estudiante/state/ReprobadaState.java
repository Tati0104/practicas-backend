package com.avh.practicas.estudiante.state;

import com.avh.practicas.estudiante.entity.EstadoPractica;

/**
 * Estado terminal negativo cuando la practica no cumple el resultado esperado.
 * Hace parte del patron State solicitado para KBM en S5.
 */
public class ReprobadaState extends EstadoPracticaBase {
    @Override
    public EstadoPractica getNombre() {
        return EstadoPractica.REPROBADA;
    }
}
