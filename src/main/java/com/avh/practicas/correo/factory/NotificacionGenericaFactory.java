package com.avh.practicas.correo.factory;

import com.avh.practicas.correo.service.IMailService;
import com.avh.practicas.shared.evento.EventoSistema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class NotificacionGenericaFactory extends NotificacionFactory {

    public NotificacionGenericaFactory(IMailService mailService) {
        super(mailService);
    }

    @Override
    public Notificacion crearNotificacion(EventoSistema evento) {
        Object correoObj = evento.getDatos().get("correo");
        String correo = correoObj != null ? String.valueOf(correoObj).trim() : null;
        if (correo == null || correo.isBlank()) {
            log.warn("NotificacionGenericaFactory: correo destinatario ausente o vacío para evento {} (id={}), notificación omitida",
                    evento.getTipo(), evento.getIdRecurso());
            return new NotificacionBase(evento.getTipo().name(), "", "", List.of(), LocalDateTime.now());
        }
        return new NotificacionBase(
                evento.getTipo().name(),
                "<p>Evento del sistema: " + evento.getTipo().name() + "</p>",
                "Notificación del sistema",
                List.of(correo),
                LocalDateTime.now()
        );
    }
}
