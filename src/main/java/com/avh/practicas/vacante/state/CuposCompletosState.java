package com.avh.practicas.vacante.state;

import com.avh.practicas.vacante.entity.EstadoVacanteEnum;

public class CuposCompletosState extends EstadoVacanteBase {

    @Override
    public void liberarCupo(VacanteContext context) {
        int total = context.getVacante().getCuposTotales() == null ? 0 : context.getVacante().getCuposTotales();
        int disponibles = context.getVacante().getCuposDisponibles() == null ? 0 : context.getVacante().getCuposDisponibles();
        if (disponibles < total) {
            context.getVacante().setCuposDisponibles(disponibles + 1);
        }
        context.transicionar(EstadoVacanteEnum.ACTIVA);
    }

    @Override
    public void cerrar(VacanteContext context) {
        context.transicionar(EstadoVacanteEnum.CERRADA);
    }

    @Override
    public EstadoVacanteEnum getNombre() {
        return EstadoVacanteEnum.CUPOS_COMPLETOS;
    }
}
