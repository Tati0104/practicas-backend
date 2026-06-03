package com.avh.practicas.vinculacion.alerta.dto;

import java.time.LocalDateTime;

/**
 * Representación enriquecida para UI tras aplicar la cadena de decoradores.
 */
public record AlertaVista(
        Long id,
        String mensaje,
        boolean resuelta,
        boolean prioritaria,
        String urlAccion,
        String nombreModulo,
        String condicionResolucion,
        LocalDateTime fecha,
        LocalDateTime fechaArchivado
) {
    public AlertaVista conPrioritaria(boolean prioritaria) {
        return new AlertaVista(id, mensaje, resuelta, prioritaria, urlAccion, nombreModulo,
                condicionResolucion, fecha, fechaArchivado);
    }

    public AlertaVista conAccion(String urlAccion, String nombreModulo) {
        return new AlertaVista(id, mensaje, resuelta, prioritaria, urlAccion, nombreModulo,
                condicionResolucion, fecha, fechaArchivado);
    }

    public AlertaVista conCondicionResolucion(String condicionResolucion) {
        return new AlertaVista(id, mensaje, resuelta, prioritaria, urlAccion, nombreModulo,
                condicionResolucion, fecha, fechaArchivado);
    }

    public AlertaVista conFechaArchivado(LocalDateTime fechaArchivado) {
        return new AlertaVista(id, mensaje, resuelta, prioritaria, urlAccion, nombreModulo,
                condicionResolucion, fecha, fechaArchivado);
    }

    public AlertaVista conResuelta(boolean resuelta) {
        return new AlertaVista(id, mensaje, resuelta, prioritaria, urlAccion, nombreModulo,
                condicionResolucion, fecha, fechaArchivado);
    }
}
