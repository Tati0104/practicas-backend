package com.avh.practicas.documento.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class DocumentosPracticaResponse {

    private List<DocumentoPracticaDto> vinculacion;
    private List<DocumentoPracticaDto> seguimiento;
    private List<DocumentoPracticaDto> evaluacionesEncuestas;
    private List<DocumentoPracticaDto> actaCierre;
}
