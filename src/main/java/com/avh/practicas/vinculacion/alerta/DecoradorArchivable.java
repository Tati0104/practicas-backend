package com.avh.practicas.vinculacion.alerta;

import com.avh.practicas.vinculacion.alerta.dto.AlertaVista;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Registra {@code fechaArchivado} al resolver la alerta.
 */
@Getter
public class DecoradorArchivable extends AlertaDecorator {

    private LocalDateTime fechaArchivado;

    public DecoradorArchivable(Alerta envuelta) {
        super(envuelta);
    }

    public static DecoradorArchivable conFechaArchivada(Alerta envuelta, LocalDateTime fechaArchivado) {
        DecoradorArchivable decorador = new DecoradorArchivable(envuelta);
        decorador.fechaArchivado = fechaArchivado;
        return decorador;
    }

    @Override
    public void resolver() {
        this.fechaArchivado = LocalDateTime.now();
        envuelta.resolver();
    }

    @Override
    public AlertaVista mostrar() {
        AlertaVista vista = envuelta.mostrar();
        return vista.conFechaArchivado(fechaArchivado).conResuelta(isResuelta());
    }
}
