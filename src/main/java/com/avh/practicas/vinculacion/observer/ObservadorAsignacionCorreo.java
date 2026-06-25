package com.avh.practicas.vinculacion.observer;

import com.avh.practicas.shared.pattern.observer.EventoSistema;
import com.avh.practicas.shared.pattern.observer.Observador;
import com.avh.practicas.vinculacion.support.AsignacionCorreoDispatcher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Observador de correo: delega envío asíncrono (&lt; 5 min) vía {@link AsignacionCorreoDispatcher}.
 */
@Component
@RequiredArgsConstructor
public class ObservadorAsignacionCorreo implements Observador {

    private final AsignacionCorreoDispatcher correoDispatcher;

    @Override
    public void actualizar(EventoSistema evento) {
        correoDispatcher.enviar(evento.getTipo(), evento.getDatos());
    }

    @Override
    @SuppressWarnings("unchecked")
    public void actualizar(String evento, Object datos) {
        Map<String, Object> payload = datos instanceof Map ? (Map<String, Object>) datos : Map.of();
        correoDispatcher.enviar(evento, payload);
    }
}
