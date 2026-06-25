package com.avh.practicas.asignacion.state;

import com.avh.practicas.asignacion.entity.EstadoAsignacion;

import java.time.LocalDateTime;

/**
 * Estado intermedio.
 * Representa una asignacion con documentos o vinculacion en proceso.
 */
public class EnProcesoVinculacionState extends EstadoAsignacionBase {

    @Override
    public void completarVinculacion(AsignacionContext context) {
        context.getAsignacion().setFechaVinculacion(LocalDateTime.now());
        context.transicionar(EstadoAsignacion.VINCULADA);
    }

    @Override
    public void cancelar(AsignacionContext context, String motivo) {
        validarMotivo(motivo);
        context.getAsignacion().setMotivoCancelacion(motivo);
        context.transicionar(EstadoAsignacion.CANCELADA);
    }

    @Override
    public EstadoAsignacion getNombre() {
        return EstadoAsignacion.EN_PROCESO_VINCULACION;
    }
}
