package com.avh.practicas.cierre.notificacion;

import com.avh.practicas.correo.entity.TipoEventoCorreo;
import com.avh.practicas.correo.factory.Notificacion;
import com.avh.practicas.correo.factory.NotificacionBase;
import com.avh.practicas.correo.factory.NotificacionFactory;
import com.avh.practicas.correo.service.IMailService;
import com.avh.practicas.correo.service.PlantillaCorreoService;
import com.avh.practicas.shared.evento.EventoSistema;
import com.avh.practicas.shared.evento.TipoEventoSistema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Factory Method para notificaciones de cierre de práctica (PE-43).
 */
@Slf4j
@Component
public class NotificacionCierreFactory extends NotificacionFactory {

    private final PlantillaCorreoService plantillaCorreoService;

    public NotificacionCierreFactory(IMailService mailService, PlantillaCorreoService plantillaCorreoService) {
        super(mailService);
        this.plantillaCorreoService = plantillaCorreoService;
    }

    @Override
    public Notificacion crearNotificacion(EventoSistema evento) {
        Object correoObj = evento.getDatos().get("correo");
        String correo = correoObj != null ? String.valueOf(correoObj).trim() : null;
        if (correo == null || correo.isBlank()) {
            log.warn("NotificacionCierreFactory: correo destinatario ausente o vacío para evento {} (id={}), notificación omitida",
                    evento.getTipo(), evento.getIdRecurso());
            return new NotificacionBase(evento.getTipo().name(), "", "", List.of(), LocalDateTime.now());
        }
        String nombre = String.valueOf(evento.getDatos().getOrDefault("nombre_estudiante", "participante"));
        String resultado = String.valueOf(evento.getDatos().getOrDefault("resultado", "CERRADA"));

        String cuerpo = plantillaCorreoService.procesarTemplate(
                TipoEventoCorreo.RESULTADO_CIERRE,
                Map.of(
                        "nombre_estudiante", nombre,
                        "resultado", resultado
                )
        );
        String asunto = plantillaCorreoService.obtener(TipoEventoCorreo.RESULTADO_CIERRE).getAsunto();

        if (evento.getTipo() == TipoEventoSistema.PRACTICA_COMPLETADA) {
            asunto = "Práctica completada — revisión académica";
        }

        return new NotificacionBase(
                evento.getTipo().name(),
                cuerpo,
                asunto,
                List.of(correo),
                LocalDateTime.now()
        );
    }
}
