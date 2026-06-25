package com.avh.practicas.vinculacion.dto;

import com.avh.practicas.asignacion.entity.EstadoAsignacion;
import com.avh.practicas.estudiante.entity.EstadoPractica;

import java.util.List;

public record VinculacionListadoResponse(
        Long asignacionId,
        Long practicaId,
        EstadoAsignacion estado,
        EstudianteVinculacionDto estudiante,
        VacanteVinculacionDto vacante,
        List<DocumentoVinculacionDto> documentos,
        Integer numeroPractica,
        EstadoPractica estadoPractica,
        Boolean convenioFirmadoEstudiante
) {
}
