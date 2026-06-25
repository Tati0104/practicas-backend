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
        Object passwordObj = evento.getDatos().get("passwordTemporal");
        String passwordTemporal = passwordObj != null ? String.valueOf(passwordObj).trim() : "";

        String mensaje;
        String asunto;
        if (!passwordTemporal.isBlank()) {
            asunto = "Acceso al Sistema de Prácticas — AVH";
            mensaje = "<p>Bienvenido/a <b>" + nombre + "</b>.</p>"
                    + "<p>Tu correo de acceso es: <b>" + correo + "</b></p>"
                    + "<p>Tu contraseña temporal es: <b>" + passwordTemporal + "</b></p>"
                    + "<p>Debes cambiarla en tu primer inicio de sesión.</p>";
        } else {
            asunto = "Usuario docente asesor";
            mensaje = "<p>Hola " + nombre + ", tu usuario como docente asesor fue creado o actualizado.</p>";
        }

        return new NotificacionBase(
                evento.getTipo().name(),
                mensaje,
                asunto,
                List.of(correo),
                LocalDateTime.now()
        );
    }
}
