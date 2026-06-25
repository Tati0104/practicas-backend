package com.avh.practicas.cierre.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * El checklist de cierre no está completo o la práctica no puede cerrarse.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class CierreNoPermitidoException extends RuntimeException {

    public CierreNoPermitidoException(String mensaje) {
        super(mensaje);
    }
}
