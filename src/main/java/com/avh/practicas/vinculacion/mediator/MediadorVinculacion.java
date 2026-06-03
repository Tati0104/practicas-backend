package com.avh.practicas.vinculacion.mediator;

import java.util.Map;

/**
 * Mediador del proceso de vinculación (PE-34).
 * Centraliza la comunicación entre servicios colaboradores sin acoplamiento directo entre ellos.
 */
public interface MediadorVinculacion {

    void notificar(String evento, Map<String, Object> datos);

    void confirmarVinculacion(Long practicaId);
}
