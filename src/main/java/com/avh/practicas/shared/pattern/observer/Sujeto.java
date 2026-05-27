package com.avh.practicas.shared.pattern.observer;

/**
 * Contrato del sujeto observable (patrón Observer).
 * Las implementaciones concretas gestionan la lista de observadores (SRP en cada módulo).
 */
public interface Sujeto {

    void suscribir(Observador observador);

    void desuscribir(Observador observador);

    void notificar(EventoSistema evento);
}
