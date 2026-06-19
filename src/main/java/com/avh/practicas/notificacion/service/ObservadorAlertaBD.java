package com.avh.practicas.notificacion.service;

import com.avh.practicas.seguimiento.entity.AlertaSistema;
import com.avh.practicas.seguimiento.repository.AlertaSistemaRepository;
import com.avh.practicas.shared.evento.EventoSistema;
import com.avh.practicas.shared.evento.Observador;
import com.avh.practicas.shared.evento.TipoEventoSistema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

/**
 * Observer que persiste una AlertaSistema en BD para cada evento relevante del negocio,
 * de modo que la campanita de notificaciones (GET /notificaciones) tenga datos reales.
 * El envío de correo sigue siendo responsabilidad de ObservadorCorreo; este observer
 * trabaja de forma independiente y nunca lanza excepciones al bus de eventos.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ObservadorAlertaBD implements Observador {

    private static final Set<com.avh.practicas.shared.evento.TipoEventoSistema> EVENTOS_RELEVANTES = Set.of(
            TipoEventoSistema.ASIGNACION_CREADA,
            TipoEventoSistema.ASIGNACION_EN_VINCULACION,
            TipoEventoSistema.ASIGNACION_VINCULADA,
            TipoEventoSistema.ASIGNACION_CANCELADA,
            TipoEventoSistema.VINCULACION_CONFIRMADA,
            TipoEventoSistema.ENCUESTA_DISPONIBLE,
            TipoEventoSistema.RECORDATORIO_ENCUESTA,
            TipoEventoSistema.PRACTICA_CERRADA,
            TipoEventoSistema.PRACTICA_COMPLETADA,
            TipoEventoSistema.DOCENTE_ASESOR_CREADO
    );

    private final AlertaSistemaRepository alertaRepository;

    @Override
    public void actualizar(EventoSistema evento) {
        if (!EVENTOS_RELEVANTES.contains(evento.getTipo())) {
            return;
        }
        try {
            alertaRepository.save(construir(evento));
        } catch (Exception e) {
            log.warn("ObservadorAlertaBD: no se pudo persistir alerta para evento {} (id={}): {}",
                    evento.getTipo(), evento.getIdRecurso(), e.getMessage());
        }
    }

    private AlertaSistema construir(EventoSistema evento) {
        Map<String, Object> d = evento.getDatos();
        String correoDestinatario = extraerCorreo(d);
        boolean prioritaria = esPrioritario(evento);

        return AlertaSistema.builder()
                .mensaje(mensajePara(evento))
                .tipo(null)
                .instanciaPracticaId(evento.getIdRecurso())
                .destinatarioCorreo(correoDestinatario)
                .prioritaria(prioritaria)
                .leida(false)
                .resuelta(false)
                .nombreModulo(evento.getModulo())
                .urlAccion(urlAccion(evento))
                .build();
    }

    private String extraerCorreo(Map<String, Object> datos) {
        if (datos == null) return null;
        Object val = datos.get("correo");
        if (val == null) return null;
        String correo = String.valueOf(val).trim();
        return correo.isBlank() ? null : correo;
    }

    private boolean esPrioritario(EventoSistema evento) {
        return switch (evento.getTipo()) {
            case ASIGNACION_CANCELADA, PRACTICA_CERRADA, PRACTICA_COMPLETADA -> true;
            default -> false;
        };
    }

    private String mensajePara(EventoSistema evento) {
        Map<String, Object> d = evento.getDatos();
        String nombre = d != null ? String.valueOf(d.getOrDefault("nombre_estudiante",
                d.getOrDefault("nombre", ""))) : "";
        String empresa = d != null ? String.valueOf(d.getOrDefault("empresa", "")) : "";

        return switch (evento.getTipo()) {
            case ASIGNACION_CREADA -> "Nueva asignación de práctica creada" +
                    (nombre.isBlank() ? "" : " para " + nombre);
            case ASIGNACION_EN_VINCULACION -> "Asignación en proceso de vinculación" +
                    (nombre.isBlank() ? "" : " — " + nombre);
            case ASIGNACION_VINCULADA -> "Asignación vinculada exitosamente" +
                    (empresa.isBlank() ? "" : " con " + empresa);
            case ASIGNACION_CANCELADA -> "Asignación cancelada" +
                    (nombre.isBlank() ? "" : " — " + nombre);
            case VINCULACION_CONFIRMADA -> "Tu vinculación con la empresa " + empresa + " fue confirmada";
            case ENCUESTA_DISPONIBLE -> "Tienes una encuesta de satisfacción disponible para completar";
            case RECORDATORIO_ENCUESTA -> "Recordatorio: completa tu encuesta de satisfacción pendiente";
            case PRACTICA_CERRADA -> "La práctica ha sido cerrada" +
                    (nombre.isBlank() ? "" : " — " + nombre);
            case PRACTICA_COMPLETADA -> "La práctica fue procesada con aprobación académica" +
                    (nombre.isBlank() ? "" : " — " + nombre);
            case DOCENTE_ASESOR_CREADO -> "Tu cuenta de docente asesor fue creada o actualizada";
            default -> "Evento del sistema: " + evento.getTipo().name();
        };
    }

    private String urlAccion(EventoSistema evento) {
        Long id = evento.getIdRecurso();
        if (id == null) return null;
        return switch (evento.getTipo()) {
            case PRACTICA_CERRADA, PRACTICA_COMPLETADA -> "/cierre/" + id;
            case ENCUESTA_DISPONIBLE, RECORDATORIO_ENCUESTA -> "/cierre/" + id;
            case ASIGNACION_CREADA, ASIGNACION_EN_VINCULACION,
                 ASIGNACION_VINCULADA, ASIGNACION_CANCELADA,
                 VINCULACION_CONFIRMADA -> "/seguimiento/" + id;
            default -> null;
        };
    }
}