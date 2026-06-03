package com.avh.practicas.asignacion.state;

import com.avh.practicas.asignacion.entity.Asignacion;
import com.avh.practicas.asignacion.entity.EstadoAsignacion;
import lombok.Getter;

@Getter
/**
 * Contexto del patron State.
 * Recibe la entidad Asignacion y delega las transiciones al estado concreto correspondiente.
 */
public class AsignacionContext {

    private final Asignacion asignacion;
    private EstadoAsignacionState estado;

    public AsignacionContext(Asignacion asignacion) {
        this.asignacion = asignacion;
        this.estado = crearEstado(asignacion.getEstado());
    }

    public void iniciarVinculacion() {
        estado.iniciarVinculacion(this);
    }

    public void completarVinculacion() {
        estado.completarVinculacion(this);
    }

    public void cancelar(String motivo) {
        estado.cancelar(this, motivo);
    }

    public void transicionar(EstadoAsignacion nuevoEstado) {
        asignacion.setEstado(nuevoEstado);
        this.estado = crearEstado(nuevoEstado);
    }

    // Selecciona la clase State que corresponde al enum guardado en base de datos.
    private EstadoAsignacionState crearEstado(EstadoAsignacion estadoActual) {
        return switch (estadoActual) {
            case ASIGNADA -> new AsignadaState();
            case EN_PROCESO_VINCULACION -> new EnProcesoVinculacionState();
            case VINCULADA -> new VinculadaState();
            case CANCELADA -> new CanceladaState();
        };
    }
}
