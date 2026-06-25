package com.avh.practicas.estudiante.state;

import com.avh.practicas.estudiante.entity.EstadoPractica;

import java.time.LocalDate;

/**
 * Estado de practica activa; desde aqui puede cerrarse o cancelarse.
 * Hace parte del patron State solicitado para KBM en S5.
 */
public class EnPracticaState extends EstadoPracticaBase {
    @Override
    public void cerrar(PracticaContext context, EstadoPractica resultado) {
        if (resultado != EstadoPractica.COMPLETADA && resultado != EstadoPractica.REPROBADA) {
            throw new IllegalArgumentException("El resultado del cierre debe ser COMPLETADA o REPROBADA");
        }
        context.getPractica().setFechaFin(LocalDate.now());
        context.getPractica().setInmutable(true);
        context.transicionar(resultado);
    }

    @Override
    public void cancelar(PracticaContext context) {
        context.transicionar(EstadoPractica.CANCELADA);
    }

    @Override
    public EstadoPractica getNombre() {
        return EstadoPractica.EN_CURSO;
    }
}
