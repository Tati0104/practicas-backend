package com.avh.practicas.vacante.state;

import com.avh.practicas.vacante.entity.EstadoVacanteEnum;

public class CerradaState extends EstadoVacanteBase {
    @Override
    public EstadoVacanteEnum getNombre() {
        return EstadoVacanteEnum.CERRADA;
    }
}
