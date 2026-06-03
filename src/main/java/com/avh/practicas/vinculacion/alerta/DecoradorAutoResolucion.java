package com.avh.practicas.vinculacion.alerta;

import com.avh.practicas.vinculacion.alerta.dto.AlertaVista;
import com.avh.practicas.vinculacion.alerta.support.EvaluadorCondicionAlerta;
import lombok.Getter;

import java.util.Map;

/**
 * Evalúa {@code condicionResolucion} para cerrar la alerta automáticamente.
 */
@Getter
public class DecoradorAutoResolucion extends AlertaDecorator {

    private final String condicionResolucion;
    private final EvaluadorCondicionAlerta evaluador;

    public DecoradorAutoResolucion(Alerta envuelta, String condicionResolucion) {
        this(envuelta, condicionResolucion, new EvaluadorCondicionAlerta());
    }

    public DecoradorAutoResolucion(
            Alerta envuelta,
            String condicionResolucion,
            EvaluadorCondicionAlerta evaluador
    ) {
        super(envuelta);
        this.condicionResolucion = condicionResolucion;
        this.evaluador = evaluador;
    }

    @Override
    public AlertaVista mostrar() {
        return envuelta.mostrar().conCondicionResolucion(condicionResolucion);
    }

    /**
     * Evalúa la condición y resuelve la alerta si aplica.
     *
     * @return true si se resolvió automáticamente en esta invocación
     */
    public boolean intentarAutoResolucion(Map<String, String> contexto) {
        if (isResuelta()) {
            return false;
        }
        AlertaVista vista = mostrar();
        if (evaluador.evaluar(condicionResolucion, vista, contexto)) {
            resolver();
            return true;
        }
        return false;
    }
}
