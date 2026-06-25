package com.avh.practicas.cierre.checklist.leaf;

import com.avh.practicas.cierre.checklist.EstadoItem;
import com.avh.practicas.cierre.checklist.ItemChecklist;
import com.avh.practicas.cierre.entity.Encuesta;
import com.avh.practicas.cierre.entity.EstadoEncuesta;
import com.avh.practicas.cierre.entity.TipoEncuesta;
import com.avh.practicas.cierre.notificacion.NotificacionRecordatorio;
import com.avh.practicas.cierre.notificacion.NotificacionRecordatorioDispatcher;
import com.avh.practicas.cierre.notificacion.NotificacionRecordatorioFactory;
import com.avh.practicas.cierre.service.EncuestaService;
import com.avh.practicas.estudiante.entity.InstanciaPractica;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Hoja del Composite: encuesta de cierre con recordatorio vía Factory Method.
 */
public class ItemEncuesta implements ItemChecklist {

    private final Long practicaId;
    private final TipoEncuesta tipoEncuesta;
    private final boolean obligatorio;
    private final InstanciaPractica practica;
    private final EncuestaService encuestaService;
    private final NotificacionRecordatorioFactory recordatorioFactory;
    private final NotificacionRecordatorioDispatcher recordatorioDispatcher;

    private EstadoEncuesta estadoEncuesta;
    private LocalDateTime fechaUltimoRecordatorio;

    public ItemEncuesta(
            Long practicaId,
            InstanciaPractica practica,
            TipoEncuesta tipoEncuesta,
            boolean obligatorio,
            Encuesta encuesta,
            EncuestaService encuestaService,
            NotificacionRecordatorioFactory recordatorioFactory,
            NotificacionRecordatorioDispatcher recordatorioDispatcher) {

        this.practicaId = practicaId;
        this.practica = practica;
        this.tipoEncuesta = tipoEncuesta;
        this.obligatorio = obligatorio;
        this.encuestaService = encuestaService;
        this.recordatorioFactory = recordatorioFactory;
        this.recordatorioDispatcher = recordatorioDispatcher;

        if (encuesta != null) {
            this.estadoEncuesta = encuesta.getEstado();
            this.fechaUltimoRecordatorio = encuesta.getFechaUltimoRecordatorio();
        } else {
            this.estadoEncuesta = EstadoEncuesta.PENDIENTE;
            this.fechaUltimoRecordatorio = null;
        }
    }

    @Override
    public boolean verificar() {
        return estadoEncuesta == EstadoEncuesta.COMPLETADA;
    }

    @Override
    public String getNombre() {
        return tipoEncuesta == TipoEncuesta.TUTOR
                ? "Encuesta de satisfacción del tutor"
                : "Encuesta de satisfacción del estudiante";
    }

    @Override
    public boolean isObligatorio() {
        return obligatorio;
    }

    @Override
    public EstadoItem getEstado() {
        return switch (estadoEncuesta) {
            case COMPLETADA -> EstadoItem.COMPLETADO;
            case EN_BORRADOR -> EstadoItem.EN_BORRADOR;
            case PENDIENTE -> EstadoItem.PENDIENTE;
        };
    }

    public TipoEncuesta getTipoEncuesta() {
        return tipoEncuesta;
    }

    public EstadoEncuesta getEstadoEncuesta() {
        return estadoEncuesta;
    }

    public LocalDateTime getFechaUltimoRecordatorio() {
        return fechaUltimoRecordatorio;
    }

    /**
     * Envía recordatorio usando {@link NotificacionRecordatorioFactory} y persiste auditoría vía {@link EncuestaService}.
     * Si la encuesta no existe aún en BD, la crea y envía la invitación inicial en lugar del recordatorio.
     */
    public void enviarRecordatorio() {
        Optional<com.avh.practicas.cierre.entity.Encuesta> encuestaOpt =
                encuestaService.obtenerPorPracticaYTipo(practicaId, tipoEncuesta);
        if (encuestaOpt.isEmpty()) {
            encuestaService.crearEncuestaPendiente(practicaId, tipoEncuesta);
            refrescarEstadoEncuesta();
            return;
        }
        encuestaService.validarRecordatorioDiario(practicaId, tipoEncuesta);
        NotificacionRecordatorio notificacion = recordatorioFactory.crear(practica, tipoEncuesta, estadoEncuesta);
        recordatorioDispatcher.enviar(notificacion);
        encuestaService.registrarRecordatorioEnviado(practicaId, tipoEncuesta);
        refrescarEstadoEncuesta();
    }

    private void refrescarEstadoEncuesta() {
        encuestaService.obtenerPorPracticaYTipo(practicaId, tipoEncuesta)
                .ifPresent(encuesta -> {
                    this.estadoEncuesta = encuesta.getEstado();
                    this.fechaUltimoRecordatorio = encuesta.getFechaUltimoRecordatorio();
                });
    }
}
