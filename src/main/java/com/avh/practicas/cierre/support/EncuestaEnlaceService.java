package com.avh.practicas.cierre.support;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Construye enlaces directos al módulo de evaluaciones/encuestas en el frontend.
 */
@Component
public class EncuestaEnlaceService {

    private final String frontendBaseUrl;

    public EncuestaEnlaceService(
            @Value("${app.frontend.base-url:http://localhost:5173}") String frontendBaseUrl) {
        this.frontendBaseUrl = frontendBaseUrl;
    }

    public String buildEnlaceEncuesta(Long practicaId) {
        if (practicaId == null) {
            return frontendBaseUrl;
        }
        String base = frontendBaseUrl.endsWith("/")
                ? frontendBaseUrl.substring(0, frontendBaseUrl.length() - 1)
                : frontendBaseUrl;
        return base + "/evaluaciones/" + practicaId;
    }
}
