package com.avh.practicas.vacante.state;

import com.avh.practicas.vacante.entity.Vacante;
import com.avh.practicas.shared.exception.NegocioException;

public class EstadoCerrada implements EstadoVacante {

    @Override
    public void publicar(Vacante vacante) {
        if (vacante.getCuposDisponibles() > 0) {
            vacante.setEstadoActual(new EstadoDisponible());
        } else {
            throw new NegocioException("No se puede publicar una vacante cerrada que no tiene cupos disponibles.");
        }
    }

    @Override
    public void asignar(Vacante vacante) {
        throw new NegocioException("No se pueden realizar asignaciones sobre una vacante cerrada.");
    }

    @Override
    public void cerrar(Vacante vacante) {
        throw new NegocioException("La vacante ya se encuentra cerrada.");
    }

    @Override
    public String getNombre() {
        return "CERRADA";
    }
}
