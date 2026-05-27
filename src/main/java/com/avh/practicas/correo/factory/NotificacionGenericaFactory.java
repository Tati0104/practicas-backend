package com.avh.practicas.correo.factory;

import com.avh.practicas.correo.service.IMailService;
import com.avh.practicas.shared.evento.EventoSistema;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class NotificacionGenericaFactory extends NotificacionFactory {

    public NotificacionGenericaFactory(IMailService mailService) {
        super(mailService);
    }

    @Override
    public Notificacion crearNotificacion(EventoSistema evento) {
        String correo = String.valueOf(evento.getDatos().getOrDefault("correo", "notificaciones@demo.com"));
        return new NotificacionBase(
                evento.getTipo().name(),
                "<p>Evento del sistema: " + evento.getTipo().name() + "</p>",
                "Notificación del sistema",
                List.of(correo),
                LocalDateTime.now()
        );
    }
}
