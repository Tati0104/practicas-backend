package com.avh.practicas.vinculacion.notificacion;

import com.avh.practicas.correo.entity.TipoEventoCorreo;
import com.avh.practicas.correo.service.PlantillaCorreoService;
import com.avh.practicas.vinculacion.event.EventoAsignacion;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Factory Method (PE-30): crea la notificación de correo según el tipo de evento de asignación.
 */
@Component
@RequiredArgsConstructor
public class NotificacionAsignacionFactory {

    private final PlantillaCorreoService plantillaCorreoService;

    public NotificacionAsignacion crear(String tipoEvento, Map<String, Object> datos) {
        return switch (tipoEvento) {
            case EventoAsignacion.NUEVA_ASIGNACION -> crearNuevaAsignacion(datos);
            case EventoAsignacion.CAMBIO_ESTADO_ASIGNACION -> crearCambioEstado(datos);
            case EventoAsignacion.ASIGNACION_CANCELADA -> crearAsignacionCancelada(datos);
            default -> crearGenerica(tipoEvento, datos);
        };
    }

    private NotificacionAsignacion crearNuevaAsignacion(Map<String, Object> datos) {
        String correo = stringDato(datos, "correo_estudiante");
        String cuerpo = plantillaCorreoService.procesarTemplate(
                TipoEventoCorreo.NUEVA_ASIGNACION,
                Map.of(
                        "nombre_estudiante", stringDato(datos, "nombre_estudiante"),
                        "empresa", stringDato(datos, "empresa")
                )
        );
        String asunto = plantillaCorreoService.obtener(TipoEventoCorreo.NUEVA_ASIGNACION).getAsunto();

        return new NotificacionAsignacion(
                EventoAsignacion.NUEVA_ASIGNACION,
                asunto,
                cuerpo,
                destinatarios(correo, stringDato(datos, "correo_tutor")),
                LocalDateTime.now()
        );
    }

    private NotificacionAsignacion crearCambioEstado(Map<String, Object> datos) {
        String correo = stringDato(datos, "correo_estudiante");
        String cuerpo = plantillaCorreoService.procesarTemplate(
                TipoEventoCorreo.CAMBIO_ESTADO,
                Map.of(
                        "nombre", stringDato(datos, "nombre_estudiante"),
                        "estado", stringDato(datos, "estado")
                )
        );
        String asunto = plantillaCorreoService.obtener(TipoEventoCorreo.CAMBIO_ESTADO).getAsunto();

        return new NotificacionAsignacion(
                EventoAsignacion.CAMBIO_ESTADO_ASIGNACION,
                asunto,
                cuerpo,
                destinatarios(correo, stringDato(datos, "correo_tutor")),
                LocalDateTime.now()
        );
    }

    private NotificacionAsignacion crearAsignacionCancelada(Map<String, Object> datos) {
        String correo = stringDato(datos, "correo_estudiante");
        String motivo = stringDato(datos, "motivo");
        String cuerpo = "<p>Hola " + stringDato(datos, "nombre_estudiante") + ",</p>"
                + "<p>Tu asignación con <b>" + stringDato(datos, "empresa") + "</b> fue cancelada.</p>"
                + "<p><b>Motivo:</b> " + motivo + "</p>";

        return new NotificacionAsignacion(
                EventoAsignacion.ASIGNACION_CANCELADA,
                "Asignación cancelada",
                cuerpo,
                destinatarios(correo),
                LocalDateTime.now()
        );
    }

    private NotificacionAsignacion crearGenerica(String tipoEvento, Map<String, Object> datos) {
        return new NotificacionAsignacion(
                tipoEvento,
                "Notificación de asignación",
                "<p>Evento: " + tipoEvento + "</p>",
                destinatarios(stringDato(datos, "correo_estudiante")),
                LocalDateTime.now()
        );
    }

    private List<String> destinatarios(String... correos) {
        List<String> lista = new ArrayList<>();
        for (String correo : correos) {
            if (correo != null && !correo.isBlank()) {
                lista.add(correo);
            }
        }
        if (lista.isEmpty()) {
            lista.add("notificaciones@demo.com");
        }
        return lista;
    }

    private String stringDato(Map<String, Object> datos, String clave) {
        if (datos == null) {
            return "";
        }
        Object valor = datos.get(clave);
        return valor == null ? "" : String.valueOf(valor);
    }
}
