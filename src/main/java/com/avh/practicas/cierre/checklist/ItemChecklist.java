package com.avh.practicas.cierre.checklist;

/**
 * Componente del patrón Composite para el checklist de cierre (PE-nuevo).
 */
public interface ItemChecklist {

    boolean verificar();

    String getNombre();

    boolean isObligatorio();

    EstadoItem getEstado();
}
