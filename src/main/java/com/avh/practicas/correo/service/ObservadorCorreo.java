package com.avh.practicas.correo.service;

import com.avh.practicas.correo.factory.NotificacionDocenteFactory;
import com.avh.practicas.correo.factory.NotificacionEncuestaFactory;
import com.avh.practicas.correo.factory.NotificacionFactory;
import com.avh.practicas.correo.factory.NotificacionGenericaFactory;
import com.avh.practicas.correo.factory.NotificacionVacanteFactory;
import com.avh.practicas.correo.factory.NotificacionVinculacionFactory;
import com.avh.practicas.shared.evento.EventoSistema;
import com.avh.practicas.shared.evento.Observador;
import com.avh.practicas.shared.evento.TipoEventoSistema;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ObservadorCorreo implements Observador {

    private final NotificacionVacanteFactory vacanteFactory;
    private final NotificacionDocenteFactory docenteFactory;
    private final NotificacionEncuestaFactory encuestaFactory;
    private final NotificacionVinculacionFactory vinculacionFactory;
    private final NotificacionGenericaFactory genericaFactory;

    @Override
    public void actualizar(EventoSistema evento) {
        seleccionarFactory(evento).enviar(evento);
    }

    private NotificacionFactory seleccionarFactory(EventoSistema evento) {
        if (evento.getTipo() == TipoEventoSistema.DOCENTE_ASESOR_CREADO) {
            return docenteFactory;
        }
        if (evento.getTipo() == TipoEventoSistema.ENCUESTA_DISPONIBLE || evento.getTipo() == TipoEventoSistema.RECORDATORIO_ENCUESTA) {
            return encuestaFactory;
        }
        if (evento.getTipo() == TipoEventoSistema.VINCULACION_CONFIRMADA) {
            return vinculacionFactory;
        }
        if (evento.getTipo().name().startsWith("VACANTE_")) {
            return vacanteFactory;
        }
        return genericaFactory;
    }
}
