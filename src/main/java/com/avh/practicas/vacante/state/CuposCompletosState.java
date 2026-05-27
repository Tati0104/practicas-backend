package com.avh.practicas.vacante.state;

import com.avh.practicas.vacante.entity.EstadoVacanteEnum;

public class CuposCompletosState extends EstadoVacanteBase {

    @Override
    public void liberarCupo(VacanteContext context) {
        int ocupados = context.getVacante().getCuposOcupados() == null ? 0 : context.getVacante().getCuposOcupados();
        if (ocupados > 0) {
            context.getVacante().setCuposOcupados(ocupados - 1);
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
