package com.avh.practicas.vinculacion.alerta;

import com.avh.practicas.vinculacion.alerta.dto.AlertaVista;

/**
 * Componente del patrón Decorator (PE-37).
 */
public interface Alerta {

    Long getId();

    AlertaVista mostrar();

    void resolver();

    boolean isResuelta();
}
