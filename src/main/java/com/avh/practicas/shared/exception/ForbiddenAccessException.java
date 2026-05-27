package com.avh.practicas.shared.exception;

/**
 * El usuario no tiene permisos para la operación (HTTP 403).
 */
public class ForbiddenAccessException extends RuntimeException {

    public ForbiddenAccessException(String message) {
        super(message);
    }
}
