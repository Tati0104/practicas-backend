package com.avh.practicas.vinculacion.dto;

import java.util.List;

public record DocumentosAsignacionResponse(
        Long asignacionId,
        Long practicaId,
        Long convenioId,
        EstudianteVinculacionDto estudiante,
        VacanteVinculacionDto vacante,
        String tutorEmpresarial,
        List<DocumentoVinculacionDto> documentos
) {
}
