package com.avh.practicas.cierre.checklist.leaf;

import com.avh.practicas.cierre.checklist.EstadoItem;
import com.avh.practicas.cierre.checklist.ItemChecklist;

/**
 * Hoja del Composite: al menos una nota registrada por el tutor empresarial.
 */
public class ItemNotaTutor implements ItemChecklist {

    private final int notasRegistradas;

    public ItemNotaTutor(int notasRegistradas) {
        this.notasRegistradas = Math.max(notasRegistradas, 0);
    }

    @Override
    public boolean verificar() {
        return notasRegistradas >= 1;
    }

    @Override
    public String getNombre() {
        return "Nota del tutor empresarial";
    }

    @Override
    public boolean isObligatorio() {
        return true;
    }

    @Override
    public EstadoItem getEstado() {
        return notasRegistradas >= 1 ? EstadoItem.COMPLETADO : EstadoItem.PENDIENTE;
    }
}
