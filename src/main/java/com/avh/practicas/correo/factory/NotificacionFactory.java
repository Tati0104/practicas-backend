package com.avh.practicas.correo.factory;

import com.avh.practicas.correo.service.IMailService;
import com.avh.practicas.shared.evento.EventoSistema;
import com.avh.practicas.shared.exception.NegocioException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class NotificacionFactory {

    protected final IMailService mailService;

    public abstract Notificacion crearNotificacion(EventoSistema evento);

    public void enviar(EventoSistema evento) {
        Notificacion notificacion = crearNotificacion(evento);
        for (String destinatario : notificacion.getDestinatarios()) {
            boolean enviado = mailService.enviar(destinatario, notificacion.getAsunto(), notificacion.getMensaje());
            if (!enviado) {
                throw new NegocioException("No se pudo enviar el correo a: " + destinatario);
            }
        }
    }
}
