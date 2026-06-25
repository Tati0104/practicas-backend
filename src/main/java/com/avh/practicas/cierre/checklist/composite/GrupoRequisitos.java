package com.avh.practicas.cierre.checklist.composite;

import com.avh.practicas.cierre.checklist.EstadoItem;
import com.avh.practicas.cierre.checklist.ItemChecklist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Nodo compuesto del checklist: agrupa ítems y evalúa de forma recursiva (PE-nuevo).
 */
public class GrupoRequisitos implements ItemChecklist {

    private final String nombre;
    private final boolean obligatorio;
    private final List<ItemChecklist> items;

    public GrupoRequisitos(String nombre, boolean obligatorio, List<ItemChecklist> items) {
        this.nombre = nombre;
        this.obligatorio = obligatorio;
        this.items = items == null ? List.of() : List.copyOf(items);
    }

    public List<ItemChecklist> getItems() {
        return Collections.unmodifiableList(items);
    }

    @Override
    public boolean verificar() {
        if (items.isEmpty()) {
            return true;
        }
        return items.stream()
                .filter(this::debeEvaluarse)
                .allMatch(ItemChecklist::verificar);
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
        if (items.isEmpty()) {
            return EstadoItem.COMPLETADO;
        }
        List<ItemChecklist> evaluables = items.stream().filter(this::debeEvaluarse).toList();
        if (evaluables.isEmpty()) {
            return EstadoItem.COMPLETADO;
        }
        boolean todosCompletos = evaluables.stream()
                .allMatch(item -> item.getEstado() == EstadoItem.COMPLETADO);
        if (todosCompletos) {
            return EstadoItem.COMPLETADO;
        }
        boolean algunoEnBorrador = evaluables.stream()
                .anyMatch(item -> item.getEstado() == EstadoItem.EN_BORRADOR);
        if (algunoEnBorrador) {
            return EstadoItem.EN_BORRADOR;
        }
        return EstadoItem.PENDIENTE;
    }

    public double getProgreso() {
        List<ItemChecklist> evaluables = items.stream().filter(this::debeEvaluarse).toList();
        if (evaluables.isEmpty()) {
            return 1.0;
        }
        long completados = evaluables.stream()
                .filter(item -> item.getEstado() == EstadoItem.COMPLETADO)
                .count();
        return (double) completados / evaluables.size();
    }

    public List<ItemChecklist> getItemsHoja() {
        List<ItemChecklist> hojas = new ArrayList<>();
        for (ItemChecklist item : items) {
            if (item instanceof GrupoRequisitos grupo) {
                hojas.addAll(grupo.getItemsHoja());
            } else {
                hojas.add(item);
            }
        }
        return hojas;
    }

    private boolean debeEvaluarse(ItemChecklist item) {
        return item.isObligatorio() || item.getEstado() != EstadoItem.PENDIENTE;
    }
}
