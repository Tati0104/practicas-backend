package com.avh.practicas.correo.factory;

import com.avh.practicas.correo.entity.TipoEventoCorreo;
import com.avh.practicas.correo.service.IMailService;
import com.avh.practicas.correo.service.PlantillaCorreoService;
import com.avh.practicas.shared.evento.EventoSistema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class NotificacionVinculacionFactory extends NotificacionFactory {

    private final PlantillaCorreoService plantillaCorreoService;

    public NotificacionVinculacionFactory(IMailService mailService, PlantillaCorreoService plantillaCorreoService) {
        super(mailService);
        this.plantillaCorreoService = plantillaCorreoService;
    }

    @Override
    public Notificacion crearNotificacion(EventoSistema evento) {
        Object correoObj = evento.getDatos().get("correo");
        String correo = correoObj != null ? String.valueOf(correoObj).trim() : null;
        if (correo == null || correo.isBlank()) {
            log.warn("NotificacionVinculacionFactory: correo destinatario ausente o vacío para evento {} (id={}), notificación omitida",
                    evento.getTipo(), evento.getIdRecurso());
            return new NotificacionBase(evento.getTipo().name(), "", "", List.of(), LocalDateTime.now());
        }
        String nombreEstudiante = String.valueOf(evento.getDatos().getOrDefault("nombre_estudiante", "Estudiante"));
        String empresa = String.valueOf(evento.getDatos().getOrDefault("empresa", "Empresa"));

        String cuerpo = plantillaCorreoService.procesarTemplate(
                TipoEventoCorreo.CONFIRMACION_VINCULACION,
                Map.of(
                        "nombre_estudiante", nombreEstudiante,
                        "empresa", empresa
                )
        );

        String asunto = plantillaCorreoService.obtener(TipoEventoCorreo.CONFIRMACION_VINCULACION).getAsunto();

        return new NotificacionBase(
                evento.getTipo().name(),
                cuerpo,
                asunto,
                List.of(correo),
                LocalDateTime.now()
        );
    }
}
