package com.avh.practicas.shared.pattern.observer;

/**
 * Contrato del observador que reacciona ante eventos del sistema.
 * Se dejan dos firmas para compatibilidad entre las ramas integradas:
 * - EventoSistema: versión base de Tati.
 * - String/Object: versión usada por entidades de Estefany.
 */
public interface Observador {

    default void actualizar(EventoSistema evento) {
    }

    default void actualizar(String evento, Object datos) {
    }
}
