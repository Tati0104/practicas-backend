package com.avh.practicas.vinculacion.support;

import com.avh.practicas.correo.service.IMailService;
import com.avh.practicas.shared.exception.NegocioException;
import com.avh.practicas.vinculacion.notificacion.NotificacionAsignacion;
import com.avh.practicas.vinculacion.notificacion.NotificacionAsignacionFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class AsignacionCorreoDispatcher {

    private static final Duration SLA_ENVIO = Duration.ofMinutes(5);

    private final NotificacionAsignacionFactory notificacionFactory;
    private final IMailService mailService;

    @Async
    public void enviar(String tipoEvento, Map<String, Object> datos) {
        LocalDateTime inicio = LocalDateTime.now();
        NotificacionAsignacion notificacion = notificacionFactory.crear(tipoEvento, datos);

        for (String destinatario : notificacion.destinatarios()) {
            boolean enviado = mailService.enviar(destinatario, notificacion.asunto(), notificacion.mensajeHtml());
            if (!enviado) {
                throw new NegocioException("No se pudo enviar el correo de asignación a: " + destinatario);
            }
        }

        Duration duracion = Duration.between(inicio, LocalDateTime.now());
        if (duracion.compareTo(SLA_ENVIO) > 0) {
            log.warn("Correo {} enviado fuera del SLA de 5 minutos ({} ms)", tipoEvento, duracion.toMillis());
        } else {
            log.debug("Correo {} enviado en {} ms (SLA 5 min)", tipoEvento, duracion.toMillis());
        }
    }
}
