package com.avh.practicas.vinculacion.alerta.job;

import com.avh.practicas.vinculacion.alerta.service.AlertaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Job de inactividad (PE-37): lunes a viernes a las 06:00.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AlertaInactividadJob {

    private final AlertaService alertaService;

    @Scheduled(cron = "0 6 * * 1-5")
    public void ejecutarEvaluacionInactividad() {
        log.info("Iniciando evaluación programada de inactividad en prácticas EN_CURSO (EN_PRACTICA)");
        alertaService.evaluarInactividad();
        log.info("Evaluación de inactividad finalizada");
    }
}
