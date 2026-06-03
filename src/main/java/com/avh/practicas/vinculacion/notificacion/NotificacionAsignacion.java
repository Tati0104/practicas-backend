package com.avh.practicas.vinculacion.notificacion;

import java.time.LocalDateTime;
import java.util.List;

public record NotificacionAsignacion(
        String tipoEvento,
        String asunto,
        String mensajeHtml,
        List<String> destinatarios,
        LocalDateTime fechaEnvio
) {
}
