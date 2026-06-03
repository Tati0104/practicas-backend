package com.avh.practicas.vinculacion.dto;

import com.avh.practicas.vinculacion.entity.CategoriaDocumento;

public record DocumentoCargadoResponse(
        Long documentoId,
        Long asignacionId,
        Long practicaId,
        CategoriaDocumento categoria,
        String url
) {
}
