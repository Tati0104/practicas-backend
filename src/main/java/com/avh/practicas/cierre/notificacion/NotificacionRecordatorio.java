package com.avh.practicas.cierre.notificacion;

import com.avh.practicas.cierre.entity.TipoEncuesta;
import com.avh.practicas.shared.evento.EventoSistema;
import com.avh.practicas.shared.evento.TipoEventoSistema;

import java.util.Map;

/**
 * Notificación de recordatorio de encuesta (producto del Factory Method).
 */
public record NotificacionRecordatorio(
        TipoEventoSistema tipoEvento,
        Long practicaId,
        TipoEncuesta tipoEncuesta,
        Long usuarioId,
        String correo,
        String nombreDestinatario
) {

    public EventoSistema toEventoSistema() {
        return EventoSistema.crear(
                tipoEvento,
                usuarioId,
                "cierre",
                practicaId,
                Map.of(
                        "correo", correo,
                        "nombre", nombreDestinatario,
                        "tipo_encuesta", tipoEncuesta.name()
                )
        );
    }
}
