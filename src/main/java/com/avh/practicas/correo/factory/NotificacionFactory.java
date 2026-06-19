package com.avh.practicas.correo.factory;

import com.avh.practicas.correo.service.IMailService;
import com.avh.practicas.shared.evento.EventoSistema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public abstract class NotificacionFactory {

    protected final IMailService mailService;

    public abstract Notificacion crearNotificacion(EventoSistema evento);

    public void enviar(EventoSistema evento) {
        Notificacion notificacion = crearNotificacion(evento);
        for (String destinatario : notificacion.getDestinatarios()) {
            if (destinatario == null || destinatario.isBlank()) {
                continue;
            }
            try {
                boolean enviado = mailService.enviar(destinatario, notificacion.getAsunto(), notificacion.getMensaje());
                if (!enviado) {
                    log.warn("No se pudo enviar el correo de notificación a {}: el servicio de correo no lo confirmó.", destinatario);
                }
            } catch (Exception ex) {
                log.warn("No se pudo enviar el correo de notificación a {}: {}", destinatario, ex.getMessage());
            }
        }
    }
}
