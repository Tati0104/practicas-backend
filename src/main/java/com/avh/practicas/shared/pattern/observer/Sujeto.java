package com.avh.practicas.shared.pattern.observer;

/**
 * Contrato base para clases observables.
 * Incluye métodos por defecto para compatibilidad entre ramas.
 */
public interface Sujeto {

    default void suscribir(Observador observador) {
        agregarObservador(observador);
    }

    default void desuscribir(Observador observador) {
        eliminarObservador(observador);
    }

    default void notificar(EventoSistema evento) {
    }

    default void agregarObservador(Observador observador) {
    }

    default void eliminarObservador(Observador observador) {
    }

    default void notificar(String evento, Object datos) {
    }
}