package com.avh.practicas.vinculacion.alerta;

import com.avh.practicas.vinculacion.alerta.entity.TipoAlerta;
import com.avh.practicas.vinculacion.alerta.support.EvaluadorCondicionAlerta;
import org.springframework.stereotype.Component;

/**
 * Aplica la cadena de decoradores según el tipo de alerta (PE-37).
 */
@Component
public class AlertaDecoradorPorTipo {

    private static final String CONDICION_NUEVA_ACTIVIDAD = "nueva actividad";
    private static final String MODULO_TABLERO = "TABLERO_SEGUIMIENTO";

    private final EvaluadorCondicionAlerta evaluadorCondicion;

    public AlertaDecoradorPorTipo(EvaluadorCondicionAlerta evaluadorCondicion) {
        this.evaluadorCondicion = evaluadorCondicion;
    }

    public ConfiguracionDecoradores configuracion(TipoAlerta tipo, Long practicaId) {
        return switch (tipo) {
            case INACTIVIDAD -> new ConfiguracionDecoradores(
                    true,
                    urlTablero(practicaId),
                    MODULO_TABLERO,
                    CONDICION_NUEVA_ACTIVIDAD
            );
        };
    }

    public Alerta aplicarDecoradores(Alerta base, ConfiguracionDecoradores config) {
        Alerta decorada = base;
        if (config.prioritaria()) {
            decorada = new DecoradorPrioritario(decorada);
        }
        if (config.urlAccion() != null || config.nombreModulo() != null) {
            decorada = new DecoradorConAccion(decorada, config.urlAccion(), config.nombreModulo());
        }
        if (config.condicionResolucion() != null) {
            decorada = new DecoradorAutoResolucion(decorada, config.condicionResolucion(), evaluadorCondicion);
        }
        return new DecoradorArchivable(decorada);
    }

    private String urlTablero(Long practicaId) {
        return "/practicas/" + practicaId + "/tablero";
    }

    public record ConfiguracionDecoradores(
            boolean prioritaria,
            String urlAccion,
            String nombreModulo,
            String condicionResolucion
    ) {
    }
}
