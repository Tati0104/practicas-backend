package com.avh.practicas.vacante.state;

import com.avh.practicas.vacante.entity.EstadoVacanteEnum;

public class PausadaState extends EstadoVacanteBase {

    @Override
    public void reactivar(VacanteContext context) {
        context.transicionar(EstadoVacanteEnum.ACTIVA);
    }

    @Override
    public void cerrar(VacanteContext context) {
        context.transicionar(EstadoVacanteEnum.CERRADA);
    }

    @Override
    public EstadoVacanteEnum getNombre() {
        return EstadoVacanteEnum.PAUSADA;
    }
}
