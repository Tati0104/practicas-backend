package com.avh.practicas.vacante.state;

import com.avh.practicas.vacante.entity.EstadoVacanteEnum;

public interface EstadoVacante {
    void aprobar(VacanteContext context);
    void rechazar(VacanteContext context, String motivo);
    void pausar(VacanteContext context);
    void reactivar(VacanteContext context);
    void cerrar(VacanteContext context);
    void descontarCupo(VacanteContext context);
    void liberarCupo(VacanteContext context);
    EstadoVacanteEnum getNombre();
}
