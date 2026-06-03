package com.avh.practicas.estudiante.state;

import com.avh.practicas.estudiante.entity.EstadoPractica;

import java.time.LocalDate;

/**
 * Estado inicial de la practica antes de iniciar oficialmente.
 * Hace parte del patron State solicitado para KBM en S5.
 */
public class AsignadaPendienteInicioState extends EstadoPracticaBase {
    @Override
    public void iniciar(PracticaContext context) {
        context.getPractica().setFechaInicio(LocalDate.now());
        context.transicionar(EstadoPractica.EN_CURSO);
    }

    @Override
    public void cancelar(PracticaContext context) {
        context.transicionar(EstadoPractica.CANCELADA);
    }

    @Override
    public EstadoPractica getNombre() {
        return EstadoPractica.ASIGNADA_PENDIENTE_INICIO;
    }
}
