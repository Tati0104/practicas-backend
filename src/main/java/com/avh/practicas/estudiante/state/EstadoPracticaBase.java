package com.avh.practicas.estudiante.state;

import com.avh.practicas.estudiante.entity.EstadoPractica;

/**
 * Clase base del State de practica.
 * Lanza excepciones cuando se intenta una transicion no permitida.
 */
public abstract class EstadoPracticaBase implements EstadoPracticaState {
    @Override
    public void iniciar(PracticaContext context) {
        transicionInvalida("iniciar");
    }

    @Override
    public void cerrar(PracticaContext context, EstadoPractica resultado) {
        transicionInvalida("cerrar");
    }

    @Override
    public void cancelar(PracticaContext context) {
        transicionInvalida("cancelar");
    }

    protected void transicionInvalida(String accion) {
        throw new IllegalStateException("No se puede " + accion + " una práctica en estado " + getNombre());
    }
}
