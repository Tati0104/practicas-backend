package com.avh.practicas.documento.service;

import com.avh.practicas.documento.dto.DocumentoDescargaDto;
import com.avh.practicas.documento.dto.DocumentosPracticaResponse;

public interface DocumentoPracticaService {

    DocumentosPracticaResponse listarPorPractica(Long practicaId);

    DocumentoDescargaDto descargar(Long practicaId, Long documentoId);
}
