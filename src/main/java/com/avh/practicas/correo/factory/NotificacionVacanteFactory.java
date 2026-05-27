package com.avh.practicas.correo.factory;

import com.avh.practicas.correo.service.IMailService;
import com.avh.practicas.shared.evento.EventoSistema;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class NotificacionVacanteFactory extends NotificacionFactory {

    public NotificacionVacanteFactory(IMailService mailService) {
        super(mailService);
    }

    @Override
    public Notificacion crearNotificacion(EventoSistema evento) {
        String correoEmpresa = String.valueOf(evento.getDatos().getOrDefault("correoEmpresa", "empresa@demo.com"));
        String cargo = String.valueOf(evento.getDatos().getOrDefault("cargo", "vacante"));
        String estado = String.valueOf(evento.getDatos().getOrDefault("estado", evento.getTipo().name()));

        return new NotificacionBase(
                evento.getTipo().name(),
                "<p>La vacante <b>" + cargo + "</b> cambió al estado <b>" + estado + "</b>.</p>",
                "Actualización de vacante",
                List.of(correoEmpresa),
                LocalDateTime.now()
        );
    }
}
