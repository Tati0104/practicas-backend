package com.avh.practicas.vacante.state;

import com.avh.practicas.vacante.entity.EstadoVacanteEnum;

public class ActivaState extends EstadoVacanteBase {

    @Override
    public void pausar(VacanteContext context) {
        context.transicionar(EstadoVacanteEnum.PAUSADA);
    }

    @Override
    public void cerrar(VacanteContext context) {
        context.transicionar(EstadoVacanteEnum.CERRADA);
    }

    @Override
    public void descontarCupo(VacanteContext context) {
        int disponibles = context.getVacante().getCuposDisponibles() == null ? 0 : context.getVacante().getCuposDisponibles();
        if (disponibles <= 0) {
            context.transicionar(EstadoVacanteEnum.CUPOS_COMPLETOS);
            throw new IllegalStateException("La vacante no tiene cupos disponibles");
        }

        context.getVacante().setCuposDisponibles(disponibles - 1);

        if (context.getVacante().getCuposDisponibles() == 0) {
            context.transicionar(EstadoVacanteEnum.CUPOS_COMPLETOS);
        }
    }

    @Override
    public void liberarCupo(VacanteContext context) {
        int total = context.getVacante().getCuposTotales() == null ? 0 : context.getVacante().getCuposTotales();
        int disponibles = context.getVacante().getCuposDisponibles() == null ? 0 : context.getVacante().getCuposDisponibles();
        if (disponibles < total) {
            context.getVacante().setCuposDisponibles(disponibles + 1);
        }
    }

    @Override
    public EstadoVacanteEnum getNombre() {
        return EstadoVacanteEnum.ACTIVA;
    }
}
