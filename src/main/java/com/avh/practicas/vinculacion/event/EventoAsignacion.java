package com.avh.practicas.vinculacion.event;

/**
 * Tipos de evento del sujeto {@link com.avh.practicas.vinculacion.entity.Asignacion} (PE-30 Observer).
 */
public final class EventoAsignacion {

    public static final String NUEVA_ASIGNACION = "NUEVA_ASIGNACION";
    public static final String CAMBIO_ESTADO_ASIGNACION = "CAMBIO_ESTADO_ASIGNACION";
    public static final String ASIGNACION_CANCELADA = "ASIGNACION_CANCELADA";

    private EventoAsignacion() {
    }
}
