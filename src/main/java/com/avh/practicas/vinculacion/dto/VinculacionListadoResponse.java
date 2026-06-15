package com.avh.practicas.vinculacion.dto;

import com.avh.practicas.asignacion.entity.EstadoAsignacion;

import java.util.List;

public record VinculacionListadoResponse(
        Long asignacionId,
        Long practicaId,
        EstadoAsignacion estado,
        EstudianteVinculacionDto estudiante,
        VacanteVinculacionDto vacante,
        List<DocumentoVinculacionDto> documentos
) {
}
