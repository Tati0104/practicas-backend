package com.avh.practicas.vinculacion.alerta;

import com.avh.practicas.vinculacion.alerta.dto.AlertaVista;
import lombok.Getter;

/**
 * Decorador abstracto (PE-37): delega en la alerta envuelta y permite enriquecer comportamiento.
 */
@Getter
public abstract class AlertaDecorator implements Alerta {

    protected final Alerta envuelta;

    protected AlertaDecorator(Alerta envuelta) {
        if (envuelta == null) {
            throw new IllegalArgumentException("La alerta envuelta no puede ser nula");
        }
        this.envuelta = envuelta;
    }

    @Override
    public Long getId() {
        return envuelta.getId();
    }

    @Override
    public AlertaVista mostrar() {
        return envuelta.mostrar();
    }

    @Override
    public void resolver() {
        envuelta.resolver();
    }

    @Override
    public boolean isResuelta() {
        return envuelta.isResuelta();
    }
}
