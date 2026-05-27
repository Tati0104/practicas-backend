package com.avh.practicas.shared.exception;

public class CatalogoPracticaNoEncontradaException extends NegocioException {
    public CatalogoPracticaNoEncontradaException(String mensaje) {
        super(mensaje);
    }
}
