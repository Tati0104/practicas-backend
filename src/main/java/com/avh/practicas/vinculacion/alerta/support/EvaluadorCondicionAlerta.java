package com.avh.practicas.vinculacion.alerta.support;

import com.avh.practicas.vinculacion.alerta.dto.AlertaVista;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * Evalúa expresiones simples de auto-resolución (PE-37).
 * <p>
 * Formatos soportados: {@code true}, {@code contains:texto}, {@code equals:valor}, {@code context:clave=valor}
 */
@Component
public class EvaluadorCondicionAlerta {

    public boolean evaluar(String condicion, AlertaVista vista, Map<String, String> contexto) {
        if (!StringUtils.hasText(condicion)) {
            return false;
        }

        String expresion = condicion.trim();

        if ("true".equalsIgnoreCase(expresion)) {
            return true;
        }
        if ("false".equalsIgnoreCase(expresion)) {
            return false;
        }
        if (expresion.regionMatches(true, 0, "contains:", 0, 9)) {
            String fragmento = expresion.substring(9);
            return vista.mensaje() != null && vista.mensaje().toLowerCase().contains(fragmento.toLowerCase());
        }
        if (expresion.regionMatches(true, 0, "equals:", 0, 7)) {
            String valor = expresion.substring(7);
            return vista.mensaje() != null && vista.mensaje().equalsIgnoreCase(valor);
        }
        if (expresion.regionMatches(true, 0, "context:", 0, 8) && contexto != null) {
            return evaluarContexto(expresion.substring(8), contexto);
        }
        if (expresion.regionMatches(true, 0, "resuelta:", 0, 9)) {
            return Boolean.parseBoolean(expresion.substring(9));
        }
        if ("nueva actividad".equalsIgnoreCase(expresion)) {
            return contexto != null
                    && "true".equalsIgnoreCase(String.valueOf(contexto.getOrDefault("nueva_actividad", "false")));
        }

        return false;
    }

    private boolean evaluarContexto(String regla, Map<String, String> contexto) {
        String[] partes = regla.split("=", 2);
        if (partes.length != 2) {
            return false;
        }
        String clave = partes[0].trim();
        String valorEsperado = partes[1].trim();
        String valorActual = contexto.get(clave);
        return valorActual != null && valorActual.equalsIgnoreCase(valorEsperado);
    }
}
