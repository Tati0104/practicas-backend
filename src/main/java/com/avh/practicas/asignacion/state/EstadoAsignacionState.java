package com.avh.practicas.asignacion.state;

import com.avh.practicas.asignacion.entity.EstadoAsignacion;

/**
 * Patron State S4.
 * Esta interfaz define las transiciones validas de una asignacion.
 * Cada clase concreta decide si permite o rechaza la transicion segun su estado actual.
 */
public interface EstadoAsignacionState {
    // Transicion cuando se inicia el proceso documental de vinculacion.
    void iniciarVinculacion(AsignacionContext context);
    // Transicion cuando el proceso de vinculacion se completa.
    void completarVinculacion(AsignacionContext context);
    // Transicion de cancelacion; los estados terminales la rechazan.
    void cancelar(AsignacionContext context, String motivo);
    EstadoAsignacion getNombre();
}
