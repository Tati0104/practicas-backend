package com.avh.practicas.vinculacion.support;

import com.avh.practicas.asignacion.entity.Asignacion;
import com.avh.practicas.shared.pattern.observer.EventoSistema;
import com.avh.practicas.shared.pattern.observer.Observador;
import com.avh.practicas.shared.pattern.observer.Sujeto;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Envoltorio Observer sobre {@link Asignacion} sin duplicar la entidad JPA del módulo asignacion.
 */
@Getter
public class AsignacionSubject implements Sujeto {

    private final Asignacion asignacion;
    private final List<Observador> observadores = new ArrayList<>();

    public AsignacionSubject(Asignacion asignacion) {
        this.asignacion = asignacion;
    }

    @Override
    public void agregarObservador(Observador observador) {
        registrarObservador(observador);
    }

    @Override
    public void eliminarObservador(Observador observador) {
        observadores.remove(observador);
    }

    public void registrarObservador(Observador observador) {
        if (observador != null && !observadores.contains(observador)) {
            observadores.add(observador);
        }
    }

    @Override
    public void notificar(EventoSistema evento) {
        notificarObservadores(evento.getTipo(), evento.getDatos());
    }

    public void notificarObservadores(String evento, Object datos) {
        if (observadores.isEmpty()) {
            return;
        }
        for (Observador observador : observadores) {
            if (datos instanceof Map<?, ?> mapa) {
                @SuppressWarnings("unchecked")
                Map<String, Object> datosEvento = (Map<String, Object>) mapa;
                observador.actualizar(evento, datosEvento);
            } else {
                observador.actualizar(evento, datos);
            }
        }
    }
}
