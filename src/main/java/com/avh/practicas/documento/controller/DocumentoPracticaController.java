package com.avh.practicas.documento.controller;

import com.avh.practicas.documento.dto.DocumentoDescargaDto;
import com.avh.practicas.documento.dto.DocumentosPracticaResponse;
import com.avh.practicas.documento.service.DocumentoPracticaService;
import com.avh.practicas.shared.pattern.proxy.ScopeGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/practicas")
@RequiredArgsConstructor
public class DocumentoPracticaController {

    private final DocumentoPracticaService documentoPracticaService;

    @GetMapping("/{practicaId}/documentos")
    @ScopeGuard("DOCUMENTO_LISTAR")
    public ResponseEntity<DocumentosPracticaResponse> listarDocumentos(@PathVariable Long practicaId) {
        return ResponseEntity.ok(documentoPracticaService.listarPorPractica(practicaId));
    }

    @GetMapping("/{practicaId}/documentos/{id}/descargar")
    @ScopeGuard("DOCUMENTO_DESCARGAR")
    public ResponseEntity<Resource> descargarDocumento(
            @PathVariable Long practicaId,
            @PathVariable Long id) {

        DocumentoDescargaDto descarga = documentoPracticaService.descargar(practicaId, id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + descarga.getNombre() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(descarga.getRecurso());
    }
}
