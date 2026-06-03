package com.avh.practicas.asignacion.state;

import com.avh.practicas.asignacion.entity.EstadoAsignacion;

/**
 * Estado inicial de una asignacion.
 * Desde aqui puede iniciar vinculacion o cancelarse.
 */
public class AsignadaState extends EstadoAsignacionBase {

    @Override
    public void iniciarVinculacion(AsignacionContext context) {
        context.transicionar(EstadoAsignacion.EN_PROCESO_VINCULACION);
    }

    @Override
    public void cancelar(AsignacionContext context, String motivo) {
        validarMotivo(motivo);
        context.getAsignacion().setMotivoCancelacion(motivo);
        context.transicionar(EstadoAsignacion.CANCELADA);
    }

    @Override
    public EstadoAsignacion getNombre() {
        return EstadoAsignacion.ASIGNADA;
    }
}
