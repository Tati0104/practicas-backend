package com.avh.practicas.shared.exception;

/**
 * Recurso solicitado no existe (HTTP 404).
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String recurso, Object identificador) {
        super(String.format("%s no encontrado con identificador: %s", recurso, identificador));
    }
}
