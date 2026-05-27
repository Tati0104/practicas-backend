package com.avh.practicas.correo.factory;

import com.avh.practicas.correo.service.IMailService;
import com.avh.practicas.shared.evento.EventoSistema;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class NotificacionFactory {

    protected final IMailService mailService;

    public abstract Notificacion crearNotificacion(EventoSistema evento);

    public void enviar(EventoSistema evento) {
        Notificacion notificacion = crearNotificacion(evento);
        for (String destinatario : notificacion.getDestinatarios()) {
            mailService.enviar(destinatario, notificacion.getAsunto(), notificacion.getMensaje());
        }
    }
}
