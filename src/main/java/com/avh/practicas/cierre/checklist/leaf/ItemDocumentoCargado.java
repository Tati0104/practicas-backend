package com.avh.practicas.cierre.checklist.leaf;

import com.avh.practicas.cierre.checklist.EstadoItem;
import com.avh.practicas.cierre.checklist.ItemChecklist;

/**
 * Hoja del Composite: documento cargado para la práctica (por categoría o nombre).
 */
public class ItemDocumentoCargado implements ItemChecklist {

    private final String nombre;
    private final boolean obligatorio;
    private final boolean documentoCargado;

    public ItemDocumentoCargado(String nombre, boolean obligatorio, boolean documentoCargado) {
        this.nombre = nombre;
        this.obligatorio = obligatorio;
        this.documentoCargado = documentoCargado;
    }

    @Override
    public boolean verificar() {
        return documentoCargado;
    }

    @Override
    public String getNombre() {
        return nombre;
    }

    @Override
    public boolean isObligatorio() {
        return obligatorio;
    }

    @Override
    public EstadoItem getEstado() {
        return documentoCargado ? EstadoItem.COMPLETADO : EstadoItem.PENDIENTE;
    }
}
