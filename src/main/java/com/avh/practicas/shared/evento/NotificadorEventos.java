package com.avh.practicas.shared.evento;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class NotificadorEventos {

    private final List<Observador> observadores;

    public void notificar(EventoSistema evento) {
        observadores.forEach(observador -> observador.actualizar(evento));
    }
}
