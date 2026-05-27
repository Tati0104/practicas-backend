package com.avh.practicas.vacante.state;

import com.avh.practicas.vacante.entity.EstadoVacanteEnum;
import com.avh.practicas.vacante.entity.Vacante;
import lombok.Getter;

@Getter
public class VacanteContext {

    private final Vacante vacante;
    private EstadoVacante estado;

    public VacanteContext(Vacante vacante) {
        this.vacante = vacante;
        this.estado = crearEstado(vacante.getEstado());
    }

    public void aprobar() {
        estado.aprobar(this);
    }

    public void rechazar(String motivo) {
        estado.rechazar(this, motivo);
    }

    public void pausar() {
        estado.pausar(this);
    }

    public void reactivar() {
        estado.reactivar(this);
    }

    public void cerrar() {
        estado.cerrar(this);
    }

    public void descontarCupo() {
        estado.descontarCupo(this);
    }

    public void liberarCupo() {
        estado.liberarCupo(this);
    }

    public void transicionar(EstadoVacanteEnum nuevoEstado) {
        vacante.setEstado(nuevoEstado);
        this.estado = crearEstado(nuevoEstado);
    }

    private EstadoVacante crearEstado(EstadoVacanteEnum estadoActual) {
        return switch (estadoActual) {
            case PENDIENTE_APROBACION -> new PendienteAprobacionState();
            case ACTIVA -> new ActivaState();
            case PAUSADA -> new PausadaState();
            case CUPOS_COMPLETOS -> new CuposCompletosState();
            case CERRADA -> new CerradaState();
            case RECHAZADA -> new RechazadaState();
        };
    }
}
