package com.avh.practicas.vinculacion.dto;

import com.avh.practicas.vinculacion.entity.CategoriaDocumento;

import java.util.List;

public record DocumentoVinculacionDto(
        Long id,
        String tipo,
        CategoriaDocumento categoria,
        String nombre,
        String estado,
        List<FirmaConvenioDto> firmas
) {
}
