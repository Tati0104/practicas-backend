package com.avh.practicas.vacante.state;

import com.avh.practicas.vacante.entity.Vacante;

public interface EstadoVacante {
    void publicar(Vacante vacante);
    void asignar(Vacante vacante);
    void cerrar(Vacante vacante);
    String getNombre();
}
