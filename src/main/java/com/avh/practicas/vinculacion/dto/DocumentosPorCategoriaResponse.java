package com.avh.practicas.vinculacion.dto;

import java.util.List;
import java.util.Map;

public record DocumentosPorCategoriaResponse(
        Long practicaId,
        Map<String, List<DocumentoPracticaDto>> documentosPorCategoria
) {
}
