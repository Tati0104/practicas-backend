package com.avh.practicas.estudiante.state;

import com.avh.practicas.estudiante.entity.EstadoPractica;
import com.avh.practicas.estudiante.entity.InstanciaPractica;
import lombok.Getter;

@Getter
/**
 * Contexto del patron State para InstanciaPractica.
 * Delegar en estados concretos evita condicionales largos en los servicios.
 */
public class PracticaContext {

    private final InstanciaPractica practica;
    private EstadoPracticaState estado;

    public PracticaContext(InstanciaPractica practica) {
        this.practica = practica;
        this.estado = crearEstado(practica.getEstado());
    }

    public void iniciar() {
        estado.iniciar(this);
    }

    public void cerrar(EstadoPractica resultado) {
        estado.cerrar(this, resultado);
    }

    public void cancelar() {
        estado.cancelar(this);
    }

    public void transicionar(EstadoPractica nuevoEstado) {
        practica.setEstado(nuevoEstado);
        this.estado = crearEstado(nuevoEstado);
    }

    private EstadoPracticaState crearEstado(EstadoPractica estadoActual) {
        return switch (estadoActual) {
            case ASIGNADA_PENDIENTE_INICIO -> new AsignadaPendienteInicioState();
            case EN_CURSO -> new EnPracticaState();
            case COMPLETADA -> new CompletadaState();
            case REPROBADA -> new ReprobadaState();
            case CANCELADA -> new CanceladaPracticaState();
        };
    }
}
