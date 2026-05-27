package com.avh.practicas.vacante.state;

import com.avh.practicas.vacante.entity.Vacante;
import com.avh.practicas.shared.exception.NegocioException;

public class EstadoAsignada implements EstadoVacante {

    @Override
    public void publicar(Vacante vacante) {
        throw new NegocioException("No se puede publicar una vacante que ya está en estado ASIGNADA.");
    }

    @Override
    public void asignar(Vacante vacante) {
        if (vacante.getCuposDisponibles() <= 0) {
            throw new NegocioException("No quedan cupos disponibles para esta vacante.");
        }
        vacante.setCuposDisponibles(vacante.getCuposDisponibles() - 1);
    }

    @Override
    public void cerrar(Vacante vacante) {
        vacante.setEstadoActual(new EstadoCerrada());
    }

    @Override
    public String getNombre() {
        return "ASIGNADA";
    }
}
