package com.avh.practicas.documento.dto;

import com.avh.practicas.documento.entity.EstadoDocumento;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class DocumentoPracticaDto {

    private Long id;
    private String nombre;
    private LocalDateTime fechaCarga;
    private String cargadoPor;
    private EstadoDocumento estado;
}
