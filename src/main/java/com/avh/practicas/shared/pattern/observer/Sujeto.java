package com.avh.practicas.shared.pattern.observer;

public interface Sujeto {
    void registrarObservador(Observador observador);
    void eliminarObservador(Observador observador);
    void notificarObservadores(String evento, Object datos);
}
