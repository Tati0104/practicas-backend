package com.avh.practicas.correo.factory;

import com.avh.practicas.cierre.support.EncuestaEnlaceService;
import com.avh.practicas.correo.entity.TipoEventoCorreo;
import com.avh.practicas.correo.service.IMailService;
import com.avh.practicas.correo.service.PlantillaCorreoService;
import com.avh.practicas.shared.evento.EventoSistema;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fábrica de notificaciones por correo para eventos relacionados con encuestas.
 * Utiliza PlantillaCorreoService para recuperar y procesar las plantillas registradas en base de datos.
 */
@Component
public class NotificacionEncuestaFactory extends NotificacionFactory {

    private final PlantillaCorreoService plantillaCorreoService;
    private final EncuestaEnlaceService encuestaEnlaceService;

    public NotificacionEncuestaFactory(
            IMailService mailService,
            PlantillaCorreoService plantillaCorreoService,
            EncuestaEnlaceService encuestaEnlaceService) {
        super(mailService);
        this.plantillaCorreoService = plantillaCorreoService;
        this.encuestaEnlaceService = encuestaEnlaceService;
    }

    @Override
    public Notificacion crearNotificacion(EventoSistema evento) {
        String correo = String.valueOf(evento.getDatos().getOrDefault("correo", "usuario@demo.com"));
        String nombre = String.valueOf(evento.getDatos().getOrDefault("nombre", "Usuario"));

        TipoEventoCorreo tipoEventoCorreo = TipoEventoCorreo.valueOf(evento.getTipo().name());
        Map<String, String> variables = new HashMap<>();
        variables.put("nombre", nombre);
        variables.put(
                "enlace_encuesta",
                encuestaEnlaceService.buildEnlaceEncuesta(evento.getIdRecurso())
        );
        String cuerpo = plantillaCorreoService.procesarTemplate(tipoEventoCorreo, variables);
        String asunto = plantillaCorreoService.obtener(tipoEventoCorreo).getAsunto();

        return new NotificacionBase(
                evento.getTipo().name(),
                cuerpo,
                asunto,
                List.of(correo),
                LocalDateTime.now()
        );
    }
}
