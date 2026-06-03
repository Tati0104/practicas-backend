package com.avh.practicas.cierre.checklist.dto;

import java.util.List;

/**
 * Resumen agregado del checklist de cierre.
 */
public record ResumenChecklist(
        Long practicaId,
        double progresoGlobal,
        boolean habilitarBotonCierre,
        int totalItems,
        int itemsCompletados,
        int itemsPendientes,
        List<GrupoResumenDto> grupos
) {
}
