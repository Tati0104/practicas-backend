package com.avh.practicas.cierre.service;

import com.avh.practicas.cierre.entity.Encuesta;
import com.avh.practicas.cierre.entity.TipoEncuesta;
import java.util.Optional;

/**
 * Interfaz que define las operaciones del módulo de encuestas.
 */
public interface EncuestaService {

    /**
     * Crea una encuesta en estado PENDIENTE para un tipo de participante (Factory Method).
     * Dispara una invitación de correo a través del sistema de eventos.
     */
    Encuesta crearEncuestaPendiente(Long practicaId, TipoEncuesta tipo);

    /**
     * Guarda las respuestas parciales de la encuesta en estado EN_BORRADOR.
     */
    Encuesta guardarBorrador(Long encuestaId, String respuestasJson);

    /**
     * Valida y finaliza la encuesta (cambiando a COMPLETADA).
     */
    Encuesta enviar(Long encuestaId);

    /**
     * Envía un recordatorio por correo para completar la encuesta (máximo 1 al día).
     */
    void enviarRecordatorio(Long practicaId, TipoEncuesta tipo);

    /**
     * Valida que se pueda enviar un recordatorio (encuesta activa y límite diario).
     */
    void validarRecordatorioDiario(Long practicaId, TipoEncuesta tipo);

    /**
     * Registra en BD y bitácora un recordatorio ya despachado (usado por {@code ItemEncuesta}).
     */
    void registrarRecordatorioEnviado(Long practicaId, TipoEncuesta tipo);

    /**
     * Verifica si la encuesta está completada.
     */
    boolean isCompleta(Long practicaId, TipoEncuesta tipo);

    /**
     * Obtiene la encuesta asociada a una práctica y tipo específico.
     */
    Optional<Encuesta> obtenerPorPracticaYTipo(Long practicaId, TipoEncuesta tipo);

    /**
     * Crea la encuesta si no existe y envía (o reenvía) la invitación por correo al destinatario.
     */
    Encuesta enviarInvitacion(Long practicaId, TipoEncuesta tipo);
}
