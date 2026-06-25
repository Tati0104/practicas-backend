package com.avh.practicas.cierre.state.practica;

import com.avh.practicas.estudiante.entity.EstadoPractica;
import com.avh.practicas.estudiante.entity.InstanciaPractica;

/**
 * Contexto State para transicionar la práctica al cierre (PE-43).
 */
public class PracticaContext {

    private PracticaState estado;

    public PracticaContext(InstanciaPractica practica) {
        this.estado = resolverEstado(practica.getEstado());
    }

    public void actualizarPorCierre(InstanciaPractica practica, boolean aprobada) {
        estado.aplicarResultadoCierre(practica, aprobada);
        this.estado = resolverEstado(practica.getEstado());
    }

    public EstadoPractica getEstadoActual() {
        return estado.getEstado();
    }

    private PracticaState resolverEstado(EstadoPractica estadoPractica) {
        if (estadoPractica == null) {
            return new PracticaEnCursoState();
        }
        return switch (estadoPractica) {
            case COMPLETADA -> new PracticaCompletadaState();
            case REPROBADA -> new PracticaReprobadaState();
            case EN_CURSO, ASIGNADA_PENDIENTE_INICIO -> new PracticaEnCursoState();
            case CANCELADA -> new PracticaReprobadaState();
        };
    }
}
