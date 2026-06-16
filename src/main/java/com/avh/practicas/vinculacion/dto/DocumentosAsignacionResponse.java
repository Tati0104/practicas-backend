package com.avh.practicas.vinculacion.dto;

import com.avh.practicas.asignacion.entity.EstadoAsignacion;
import java.time.LocalDate;
import java.util.List;

public record DocumentosAsignacionResponse(
        Long asignacionId,
        Long practicaId,
        Long convenioId,
        EstudianteVinculacionDto estudiante,
        VacanteVinculacionDto vacante,
        String tutorEmpresarial,
        Long docenteAsesorId,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        EstadoAsignacion estadoVinculacion,
        List<DocumentoVinculacionDto> documentos
) {
}
