package com.avh.practicas.documento.dto;

import lombok.Builder;
import lombok.Getter;
import org.springframework.core.io.Resource;

@Getter
@Builder
public class DocumentoDescargaDto {

    private String nombre;
    private Resource recurso;
}
