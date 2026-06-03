package com.avh.practicas.cierre.checklist.leaf;

import com.avh.practicas.cierre.checklist.EstadoItem;
import com.avh.practicas.cierre.checklist.ItemChecklist;

/**
 * Hoja del Composite: nota final de coordinación registrada.
 */
public class ItemNotaFinal implements ItemChecklist {

    private final boolean notaFinalRegistrada;

    public ItemNotaFinal(boolean notaFinalRegistrada) {
        this.notaFinalRegistrada = notaFinalRegistrada;
    }

    @Override
    public boolean verificar() {
        return notaFinalRegistrada;
    }

    @Override
    public String getNombre() {
        return "Nota final de cierre";
    }

    @Override
    public boolean isObligatorio() {
        return true;
    }

    @Override
    public EstadoItem getEstado() {
        return notaFinalRegistrada ? EstadoItem.COMPLETADO : EstadoItem.PENDIENTE;
    }
}
