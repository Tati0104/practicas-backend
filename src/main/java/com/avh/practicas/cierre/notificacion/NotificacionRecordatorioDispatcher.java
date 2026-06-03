package com.avh.practicas.cierre.notificacion;

import com.avh.practicas.shared.evento.NotificadorEventos;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Despacha la notificación de recordatorio al bus de eventos del sistema.
 */
@Component
@RequiredArgsConstructor
public class NotificacionRecordatorioDispatcher {

    private final NotificadorEventos notificadorEventos;

    public void enviar(NotificacionRecordatorio notificacion) {
        notificadorEventos.notificar(notificacion.toEventoSistema());
    }
}
