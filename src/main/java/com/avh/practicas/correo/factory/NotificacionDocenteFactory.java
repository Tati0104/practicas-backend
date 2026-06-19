package com.avh.practicas.correo.factory;

import com.avh.practicas.correo.service.IMailService;
import com.avh.practicas.shared.evento.EventoSistema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class NotificacionDocenteFactory extends NotificacionFactory {

    public NotificacionDocenteFactory(IMailService mailService) {
        super(mailService);
    }

    @Override
    public Notificacion crearNotificacion(EventoSistema evento) {
        Object correoObj = evento.getDatos().get("correo");
        String correo = correoObj != null ? String.valueOf(correoObj).trim() : null;
        if (correo == null || correo.isBlank()) {
            log.warn("NotificacionDocenteFactory: correo destinatario ausente o vacío para evento {} (id={}), notificación omitida",
                    evento.getTipo(), evento.getIdRecurso());
            return new NotificacionBase(evento.getTipo().name(), "", "", List.of(), LocalDateTime.now());
        }
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
