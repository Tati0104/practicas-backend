package com.avh.practicas.vacante.state;

import com.avh.practicas.vacante.entity.EstadoVacanteEnum;

public class RechazadaState extends EstadoVacanteBase {
    @Override
    public EstadoVacanteEnum getNombre() {
        return EstadoVacanteEnum.RECHAZADA;
    }
}
