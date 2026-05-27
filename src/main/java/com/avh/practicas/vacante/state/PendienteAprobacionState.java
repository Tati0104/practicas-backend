package com.avh.practicas.vacante.state;

import com.avh.practicas.vacante.entity.EstadoVacanteEnum;

public class PendienteAprobacionState extends EstadoVacanteBase {

    @Override
    public void aprobar(VacanteContext context) {
        context.transicionar(EstadoVacanteEnum.ACTIVA);
    }

    @Override
    public void rechazar(VacanteContext context, String motivo) {
        validarMotivo(motivo);
        context.getVacante().setMotivoRechazo(motivo);
        context.transicionar(EstadoVacanteEnum.RECHAZADA);
    }

    @Override
    public void cerrar(VacanteContext context) {
        context.transicionar(EstadoVacanteEnum.CERRADA);
    }

    @Override
    public EstadoVacanteEnum getNombre() {
        return EstadoVacanteEnum.PENDIENTE_APROBACION;
    }
}
