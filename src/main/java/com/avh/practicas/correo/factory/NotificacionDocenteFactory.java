package com.avh.practicas.correo.factory;

import com.avh.practicas.correo.service.IMailService;
import com.avh.practicas.shared.evento.EventoSistema;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class NotificacionDocenteFactory extends NotificacionFactory {

    public NotificacionDocenteFactory(IMailService mailService) {
        super(mailService);
    }

    @Override
    public Notificacion crearNotificacion(EventoSistema evento) {
        String correo = String.valueOf(evento.getDatos().getOrDefault("correo", "docente@demo.com"));
        String nombre = String.valueOf(evento.getDatos().getOrDefault("nombre", "docente asesor"));

        return new NotificacionBase(
                evento.getTipo().name(),
                "<p>Hola " + nombre + ", tu usuario como docente asesor fue creado o actualizado.</p>",
                "Usuario docente asesor",
                List.of(correo),
                LocalDateTime.now()
        );
    }
}
