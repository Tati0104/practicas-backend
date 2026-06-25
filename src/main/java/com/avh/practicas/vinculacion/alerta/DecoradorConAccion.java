package com.avh.practicas.vinculacion.alerta;

import com.avh.practicas.vinculacion.alerta.dto.AlertaVista;
import lombok.Getter;

/**
 * Agrega enlace directo (urlAccion) y módulo de destino (nombreModulo).
 */
@Getter
public class DecoradorConAccion extends AlertaDecorator {

    private final String urlAccion;
    private final String nombreModulo;

    public DecoradorConAccion(Alerta envuelta, String urlAccion, String nombreModulo) {
        super(envuelta);
        this.urlAccion = urlAccion;
        this.nombreModulo = nombreModulo;
    }

    @Override
    public AlertaVista mostrar() {
        return envuelta.mostrar().conAccion(urlAccion, nombreModulo);
    }
}
