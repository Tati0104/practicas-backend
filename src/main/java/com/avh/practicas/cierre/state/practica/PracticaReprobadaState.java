package com.avh.practicas.cierre.state.practica;

import com.avh.practicas.estudiante.entity.EstadoPractica;
import com.avh.practicas.estudiante.entity.InstanciaPractica;

public class PracticaReprobadaState implements PracticaState {

    @Override
    public EstadoPractica getEstado() {
        return EstadoPractica.REPROBADA;
    }

    @Override
    public void aplicarResultadoCierre(InstanciaPractica practica, boolean aprobada) {
        if (aprobada) {
            practica.setEstado(EstadoPractica.COMPLETADA);
        }
    }
}
