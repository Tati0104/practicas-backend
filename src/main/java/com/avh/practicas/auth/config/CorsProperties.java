package com.avh.practicas.auth.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Orígenes CORS permitidos para el frontend.
 * Configurable por variable de entorno o application.yml.
 *
 * Ejemplo (PowerShell):
 *   $env:CORS_ALLOWED_ORIGINS="http://localhost:5173,https://app.midominio.com"
 *
 * Ejemplo (Linux / producción):
 *   CORS_ALLOWED_ORIGINS=https://app.midominio.com,https://www.midominio.com
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.cors")
public class CorsProperties {

    /**
     * Lista separada por comas. Por defecto incluye puertos locales de Vite/React.
     */
    private String allowedOrigins =
            "http://localhost:5173,http://localhost:3000,http://localhost:4200";

    public List<String> getAllowedOriginsList() {
        return Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toList();
    }
}
