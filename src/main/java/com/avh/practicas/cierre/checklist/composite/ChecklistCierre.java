package com.avh.practicas.cierre.checklist.composite;

import com.avh.practicas.cierre.checklist.EstadoItem;
import com.avh.practicas.cierre.checklist.ItemChecklist;
import com.avh.practicas.cierre.checklist.dto.GrupoResumenDto;
import com.avh.practicas.cierre.checklist.dto.ItemResumenDto;
import com.avh.practicas.cierre.checklist.dto.ResumenChecklist;
import com.avh.practicas.cierre.checklist.leaf.ItemEncuesta;

import java.util.Collections;
import java.util.List;

/**
 * Raíz del Composite: checklist de cierre de una práctica (PE-nuevo).
 */
public class ChecklistCierre {

    private final Long practicaId;
    private final List<GrupoRequisitos> grupos;

    public ChecklistCierre(Long practicaId, List<GrupoRequisitos> grupos) {
        this.practicaId = practicaId;
        this.grupos = grupos == null ? List.of() : List.copyOf(grupos);
    }

    public Long getPracticaId() {
        return practicaId;
    }

    public List<GrupoRequisitos> getGrupos() {
        return Collections.unmodifiableList(grupos);
    }

    public boolean habilitarBotonCierre() {
        if (grupos.isEmpty()) {
            return false;
        }
        return grupos.stream()
                .filter(GrupoRequisitos::isObligatorio)
                .allMatch(GrupoRequisitos::verificar);
    }

    public ResumenChecklist getResumen() {
        List<GrupoResumenDto> gruposResumen = grupos.stream()
                .map(this::mapearGrupo)
                .toList();

        List<ItemChecklist> hojas = grupos.stream()
                .flatMap(grupo -> grupo.getItemsHoja().stream())
                .toList();

        int totalItems = hojas.size();
        int itemsCompletados = (int) hojas.stream()
                .filter(item -> item.getEstado() == EstadoItem.COMPLETADO)
                .count();
        int itemsPendientes = (int) hojas.stream()
                .filter(item -> item.getEstado() == EstadoItem.PENDIENTE)
                .count();

        double progresoGlobal = totalItems == 0
                ? 0.0
                : (double) itemsCompletados / totalItems;

        return new ResumenChecklist(
                practicaId,
                progresoGlobal,
                habilitarBotonCierre(),
                totalItems,
                itemsCompletados,
                itemsPendientes,
                gruposResumen
        );
    }

    public ItemEncuesta buscarItemEncuesta(com.avh.practicas.cierre.entity.TipoEncuesta tipo) {
        return grupos.stream()
                .flatMap(grupo -> grupo.getItems().stream())
                .filter(ItemEncuesta.class::isInstance)
                .map(ItemEncuesta.class::cast)
                .filter(item -> item.getTipoEncuesta() == tipo)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró ítem de encuesta para el tipo: " + tipo));
    }

    private GrupoResumenDto mapearGrupo(GrupoRequisitos grupo) {
        List<ItemResumenDto> items = grupo.getItems().stream()
                .map(this::mapearItem)
                .toList();

        return new GrupoResumenDto(
                grupo.getNombre(),
                grupo.getProgreso(),
                grupo.verificar(),
                grupo.getEstado(),
                items
        );
    }

    private ItemResumenDto mapearItem(ItemChecklist item) {
        if (item instanceof ItemEncuesta encuesta) {
            return new ItemResumenDto(
                    encuesta.getNombre(),
                    encuesta.isObligatorio(),
                    encuesta.verificar(),
                    encuesta.getEstado(),
                    encuesta.getTipoEncuesta(),
                    encuesta.getEstadoEncuesta(),
                    encuesta.getFechaUltimoRecordatorio()
            );
        }
        return ItemResumenDto.simple(
                item.getNombre(),
                item.isObligatorio(),
                item.verificar(),
                item.getEstado()
        );
    }
}
