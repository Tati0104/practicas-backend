package com.avh.practicas.shared.exception;

/**
 * Solicitud inválida o regla de negocio incumplida (HTTP 400).
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
