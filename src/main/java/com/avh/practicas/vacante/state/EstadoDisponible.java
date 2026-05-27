package com.avh.practicas.vacante.state;

import com.avh.practicas.vacante.entity.Vacante;
import com.avh.practicas.shared.exception.NegocioException;

public class EstadoDisponible implements EstadoVacante {

    @Override
    public void publicar(Vacante vacante) {
        throw new NegocioException("La vacante ya se encuentra disponible.");
    }

    @Override
    public void asignar(Vacante vacante) {
        if (vacante.getCuposDisponibles() <= 0) {
            throw new NegocioException("No hay cupos disponibles para asignar esta vacante.");
        }
        vacante.setCuposDisponibles(vacante.getCuposDisponibles() - 1);
        if (vacante.getCuposDisponibles() == 0) {
            vacante.setEstadoActual(new EstadoAsignada());
        }
    }

    @Override
    public void cerrar(Vacante vacante) {
        vacante.setEstadoActual(new EstadoCerrada());
    }

    @Override
    public String getNombre() {
        return "DISPONIBLE";
    }
}
