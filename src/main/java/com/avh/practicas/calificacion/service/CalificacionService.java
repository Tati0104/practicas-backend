package com.avh.practicas.calificacion.service;

import com.avh.practicas.calificacion.dto.NotaFinalRequest;
import com.avh.practicas.calificacion.dto.NotaRequest;
import com.avh.practicas.calificacion.dto.ResumenCalificacionesResponse;
import com.avh.practicas.calificacion.entity.NotaDocente;
import com.avh.practicas.calificacion.entity.NotaFinal;
import com.avh.practicas.calificacion.entity.NotaTutor;

/**
 * Interfaz que define las operaciones del módulo de calificaciones de la práctica.
 */
public interface CalificacionService {

    /**
     * Registra o actualiza la calificación de un corte dada por el Docente Asesor.
     */
    NotaDocente registrarNotaDocente(Long practicaId, Long docenteId, Integer corte, NotaRequest request);

    /**
     * Registra o actualiza la calificación de un corte dada por el Tutor Empresarial.
     */
    NotaTutor registrarNotaTutor(Long practicaId, Long tutorId, Integer corte, NotaRequest request);

    /**
     * Registra la calificación final y aprueba/reprueba la práctica (Acción del Coordinador).
     */
    NotaFinal registrarNotaFinal(Long practicaId, Long coordinadorId, NotaFinalRequest request);

    /**
     * Obtiene el resumen consolidado de calificaciones para una práctica.
     */
    ResumenCalificacionesResponse obtenerResumen(Long practicaId);

    /**
     * Lee la nota final registrada (PE-43 — Facade de cierre).
     */
    NotaFinal leerNotaFinal(Long practicaId);
}
