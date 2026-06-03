package com.avh.practicas.vinculacion.support;

import com.avh.practicas.shared.pattern.observer.Observador;
import com.avh.practicas.vinculacion.entity.Asignacion;
import com.avh.practicas.vinculacion.observer.ObservadorAsignacionBitacora;
import com.avh.practicas.vinculacion.observer.ObservadorAsignacionCorreo;
import org.springframework.stereotype.Component;

@Component
public class AsignacionObservadorRegistry {

    private final ObservadorAsignacionBitacora observadorBitacora;
    private final ObservadorAsignacionCorreo observadorCorreo;

    public AsignacionObservadorRegistry(
            ObservadorAsignacionBitacora observadorBitacora,
            ObservadorAsignacionCorreo observadorCorreo
    ) {
        this.observadorBitacora = observadorBitacora;
        this.observadorCorreo = observadorCorreo;
    }

    public void registrarObservadores(Asignacion asignacion) {
        registrarSiFalta(asignacion, observadorBitacora);
        registrarSiFalta(asignacion, observadorCorreo);
    }

    private void registrarSiFalta(Asignacion asignacion, Observador observador) {
        asignacion.registrarObservador(observador);
    }
}
