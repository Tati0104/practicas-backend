package com.avh.practicas.cierre.state.practica;

import com.avh.practicas.estudiante.entity.EstadoPractica;
import com.avh.practicas.estudiante.entity.InstanciaPractica;

/**
 * Estado de la práctica (PE-43 — State).
 */
public interface PracticaState {

    EstadoPractica getEstado();

    void aplicarResultadoCierre(InstanciaPractica practica, boolean aprobada);
}
