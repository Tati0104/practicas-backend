package com.avh.practicas.correo.factory;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class NotificacionBase implements Notificacion {
    private final String tipo;
    private final String mensaje;
    private final String asunto;
    private final List<String> destinatarios;
    private final LocalDateTime fecha;
}
