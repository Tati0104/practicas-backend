package com.avh.practicas.vinculacion.alerta;

import com.avh.practicas.vinculacion.alerta.entity.AlertaSistema;
import com.avh.practicas.vinculacion.alerta.support.EvaluadorCondicionAlerta;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * Construye la cadena de decoradores a partir de la entidad persistida (PE-37).
 */
@Component
public class AlertaFabricaDecorator {

    private final EvaluadorCondicionAlerta evaluadorCondicion;

    public AlertaFabricaDecorator(EvaluadorCondicionAlerta evaluadorCondicion) {
        this.evaluadorCondicion = evaluadorCondicion;
    }

    public Alerta crearDesdeEntidad(AlertaSistema entidad) {
        Alerta alerta = new AlertaBase(
                entidad.getId(),
                entidad.getMensaje(),
                entidad.getFecha(),
                Boolean.TRUE.equals(entidad.getResuelta())
        );

        if (StringUtils.hasText(entidad.getCondicionResolucion())) {
            alerta = new DecoradorAutoResolucion(alerta, entidad.getCondicionResolucion(), evaluadorCondicion);
        }

        if (StringUtils.hasText(entidad.getUrlAccion()) || StringUtils.hasText(entidad.getNombreModulo())) {
            alerta = new DecoradorConAccion(
                    alerta,
                    entidad.getUrlAccion(),
                    entidad.getNombreModulo()
            );
        }

        if (Boolean.TRUE.equals(entidad.getPrioritaria())) {
            alerta = new DecoradorPrioritario(alerta);
        }

        if (entidad.getFechaArchivado() != null) {
            return DecoradorArchivable.conFechaArchivada(alerta, entidad.getFechaArchivado());
        }
        return new DecoradorArchivable(alerta);
    }

    public Alerta crearNueva(
            String mensaje,
            boolean prioritaria,
            String urlAccion,
            String nombreModulo,
            String condicionResolucion
    ) {
        Alerta alerta = new AlertaBase(null, mensaje, LocalDateTime.now(), false);

        if (StringUtils.hasText(condicionResolucion)) {
            alerta = new DecoradorAutoResolucion(alerta, condicionResolucion, evaluadorCondicion);
        }
        if (StringUtils.hasText(urlAccion) || StringUtils.hasText(nombreModulo)) {
            alerta = new DecoradorConAccion(alerta, urlAccion, nombreModulo);
        }
        if (prioritaria) {
            alerta = new DecoradorPrioritario(alerta);
        }
        return new DecoradorArchivable(alerta);
    }
}
