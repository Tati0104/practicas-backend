package com.avh.practicas.estudiante.state;

import com.avh.practicas.estudiante.entity.EstadoPractica;

/**
 * Patron State S5.
 * Define las transiciones permitidas del ciclo de vida de una practica.
 */
public interface EstadoPracticaState {
    void iniciar(PracticaContext context);
    void cerrar(PracticaContext context, EstadoPractica resultado);
    void cancelar(PracticaContext context);
    EstadoPractica getNombre();
}
