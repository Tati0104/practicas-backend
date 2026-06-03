package com.avh.practicas.vinculacion.alerta;

import com.avh.practicas.vinculacion.alerta.dto.AlertaVista;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Componente concreto del patrón Decorator (PE-37).
 */
@Getter
public class AlertaBase implements Alerta {

    private final Long id;
    private final String mensaje;
    private final LocalDateTime fecha;
    private boolean resuelta;

    public AlertaBase(Long id, String mensaje, LocalDateTime fecha, boolean resuelta) {
        this.id = id;
        this.mensaje = mensaje;
        this.fecha = fecha != null ? fecha : LocalDateTime.now();
        this.resuelta = resuelta;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public AlertaVista mostrar() {
        return new AlertaVista(id, mensaje, resuelta, false, null, null, null, fecha, null);
    }

    @Override
    public void resolver() {
        this.resuelta = true;
    }

    @Override
    public boolean isResuelta() {
        return resuelta;
    }
}
