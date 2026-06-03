package com.avh.practicas.vacante.state;

import com.avh.practicas.vacante.entity.EstadoVacanteEnum;

public abstract class EstadoVacanteBase implements EstadoVacante {

    @Override
    public void aprobar(VacanteContext context) {
        transicionInvalida("aprobar");
    }

    @Override
    public void rechazar(VacanteContext context, String motivo) {
        transicionInvalida("rechazar");
    }

    @Override
    public void pausar(VacanteContext context) {
        transicionInvalida("pausar");
    }

    @Override
    public void reactivar(VacanteContext context) {
        transicionInvalida("reactivar");
    }

    @Override
    public void cerrar(VacanteContext context) {
        transicionInvalida("cerrar");
    }

    @Override
    public void descontarCupo(VacanteContext context) {
        transicionInvalida("descontar cupo");
    }

    @Override
    public void liberarCupo(VacanteContext context) {
        transicionInvalida("liberar cupo");
    }

    protected void validarMotivo(String motivo) {
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("El motivo es obligatorio");
        }
    }

    protected void transicionInvalida(String accion) {
        throw new IllegalStateException("No se puede " + accion + " una vacante en estado " + getNombre());
    }

    public abstract EstadoVacanteEnum getNombre();
}
