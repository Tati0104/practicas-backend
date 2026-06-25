package com.avh.practicas.asignacion.state;

import com.avh.practicas.asignacion.entity.EstadoAsignacion;

/**
 * Clase base del patron State.
 * Centraliza el error de transicion invalida para no repetir logica en cada estado.
 */
public abstract class EstadoAsignacionBase implements EstadoAsignacionState {

    @Override
    public void iniciarVinculacion(AsignacionContext context) {
        transicionInvalida("iniciar vinculación");
    }

    @Override
    public void completarVinculacion(AsignacionContext context) {
        transicionInvalida("completar vinculación");
    }

    @Override
    public void cancelar(AsignacionContext context, String motivo) {
        transicionInvalida("cancelar");
    }

    protected void validarMotivo(String motivo) {
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("El motivo de cancelación es obligatorio");
        }
    }

    protected void transicionInvalida(String accion) {
        throw new IllegalStateException("No se puede " + accion + " una asignación en estado " + getNombre());
    }

    public abstract EstadoAsignacion getNombre();
}
