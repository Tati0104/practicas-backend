package com.avh.practicas.cierre.checklist.leaf;

import com.avh.practicas.cierre.checklist.EstadoItem;
import com.avh.practicas.cierre.checklist.ItemChecklist;

/**
 * Hoja del Composite: notas del docente registradas en todos los cortes.
 */
public class ItemNotaDocente implements ItemChecklist {

    private final int numCortes;
    private final int notasRegistradas;

    public ItemNotaDocente(int numCortes, int notasRegistradas) {
        this.numCortes = Math.max(numCortes, 0);
        this.notasRegistradas = Math.max(notasRegistradas, 0);
    }

    @Override
    public boolean verificar() {
        return numCortes > 0 && notasRegistradas >= numCortes;
    }

    @Override
    public String getNombre() {
        return "Notas del docente por corte";
    }

    @Override
    public boolean isObligatorio() {
        return true;
    }

    @Override
    public EstadoItem getEstado() {
        if (notasRegistradas <= 0) {
            return EstadoItem.PENDIENTE;
        }
        if (notasRegistradas < numCortes) {
            return EstadoItem.EN_BORRADOR;
        }
        return EstadoItem.COMPLETADO;
    }
}
