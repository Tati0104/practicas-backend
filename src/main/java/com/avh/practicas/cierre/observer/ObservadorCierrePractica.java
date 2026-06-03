package com.avh.practicas.cierre.observer;

import com.avh.practicas.cierre.notificacion.NotificacionCierreFactory;
import com.avh.practicas.shared.evento.EventoSistema;
import com.avh.practicas.shared.evento.Observador;
import com.avh.practicas.shared.evento.TipoEventoSistema;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Observador de cierre de práctica (PE-43): correo a actores y coordinación académica.
 */
@Component
@RequiredArgsConstructor
public class ObservadorCierrePractica implements Observador {

    private final NotificacionCierreFactory notificacionCierreFactory;

    @Override
    public void actualizar(EventoSistema evento) {
        if (evento.getTipo() != TipoEventoSistema.PRACTICA_CERRADA
                && evento.getTipo() != TipoEventoSistema.PRACTICA_COMPLETADA) {
            return;
        }
        notificacionCierreFactory.enviar(evento);
    }
}
