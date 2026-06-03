package com.avh.practicas.vinculacion.mediator;

import java.time.LocalDate;
import java.util.Map;

public interface MediadorVinculacion {

    void notificar(String evento, Map<String, Object> datos);

    void confirmarVinculacion(Long practicaId);

    void confirmarVinculacion(Long practicaId, LocalDate fechaInicio, LocalDate fechaFin);
}
