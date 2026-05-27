package com.avh.practicas.shared.pattern.observer;

/**
 * Contrato del observador que reacciona ante eventos del sistema (patrón Observer).
 */
public interface Observador {

    void actualizar(EventoSistema evento);
}
