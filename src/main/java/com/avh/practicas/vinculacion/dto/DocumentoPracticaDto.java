package com.avh.practicas.vinculacion.dto;

import com.avh.practicas.vinculacion.entity.CategoriaDocumento;
import com.avh.practicas.vinculacion.entity.DocumentoPractica;

import java.time.LocalDateTime;

public record DocumentoPracticaDto(
        Long id,
        String nombre,
        String url,
        String tipo,
        CategoriaDocumento categoria,
        LocalDateTime fecha
) {
    public static DocumentoPracticaDto desde(DocumentoPractica documento) {
        return new DocumentoPracticaDto(
                documento.getId(),
                documento.getNombre(),
                documento.getUrl(),
                documento.getTipo(),
                documento.getCategoria(),
                documento.getFecha()
        );
    }
}
