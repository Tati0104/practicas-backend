package com.avh.practicas.cierre.state.practica;

import com.avh.practicas.estudiante.entity.EstadoPractica;
import com.avh.practicas.estudiante.entity.InstanciaPractica;

public class PracticaEnCursoState implements PracticaState {

    @Override
    public EstadoPractica getEstado() {
        return EstadoPractica.EN_CURSO;
    }

    @Override
    public void aplicarResultadoCierre(InstanciaPractica practica, boolean aprobada) {
        practica.setEstado(aprobada ? EstadoPractica.COMPLETADA : EstadoPractica.REPROBADA);
    }
}
