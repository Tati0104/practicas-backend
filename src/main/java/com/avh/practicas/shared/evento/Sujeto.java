package com.avh.practicas.shared.evento;

public interface Sujeto {
    void suscribir(Observador observador);
    void desuscribir(Observador observador);
    void notificar(EventoSistema evento);
}
