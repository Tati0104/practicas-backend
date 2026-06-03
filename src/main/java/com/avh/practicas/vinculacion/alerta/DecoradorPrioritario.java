package com.avh.practicas.vinculacion.alerta;

import com.avh.practicas.vinculacion.alerta.dto.AlertaVista;

/**
 * Marca la alerta como prioritaria para que aparezca al inicio del listado.
 */
public class DecoradorPrioritario extends AlertaDecorator {

    public DecoradorPrioritario(Alerta envuelta) {
        super(envuelta);
    }

    public boolean isPrioritaria() {
        return true;
    }

    @Override
    public AlertaVista mostrar() {
        return envuelta.mostrar().conPrioritaria(true);
    }
}
