package com.avh.practicas.vinculacion.controller;

import com.avh.practicas.shared.api.ApiResponse;
import com.avh.practicas.vinculacion.dto.ConfirmarVinculacionRequest;
import com.avh.practicas.vinculacion.dto.DocumentoCargadoResponse;
import com.avh.practicas.vinculacion.dto.DocumentosPorCategoriaResponse;
import com.avh.practicas.vinculacion.entity.RolFirmaConvenio;
import com.avh.practicas.vinculacion.service.VinculacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class VinculacionController {

    private final VinculacionService vinculacionService;

    @PostMapping(value = "/vinculaciones/asignaciones/{asignacionId}/carta", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<DocumentoCargadoResponse>> cargarCarta(
            @PathVariable Long asignacionId,
            @RequestParam("archivo") MultipartFile archivo
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Carta de vinculación cargada correctamente",
                vinculacionService.cargarCarta(asignacionId, archivo)
        ));
    }

    @PostMapping(value = "/vinculaciones/asignaciones/{asignacionId}/convenio", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<DocumentoCargadoResponse>> cargarConvenio(
            @PathVariable Long asignacionId,
            @RequestParam("archivo") MultipartFile archivo
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Convenio cargado correctamente",
                vinculacionService.cargarConvenio(asignacionId, archivo)
        ));
    }

    @PostMapping("/vinculaciones/convenios/{convenioId}/firmas/{rol}")
    public ResponseEntity<ApiResponse<Void>> confirmarFirma(
            @PathVariable Long convenioId,
            @PathVariable RolFirmaConvenio rol
    ) {
        vinculacionService.confirmarFirma(convenioId, rol);
        return ResponseEntity.ok(ApiResponse.ok("Firma registrada correctamente"));
    }

    @PostMapping("/vinculaciones/practicas/{practicaId}/confirmar")
    public ResponseEntity<ApiResponse<Void>> confirmarVinculacion(
            @PathVariable Long practicaId,
            @Valid @RequestBody ConfirmarVinculacionRequest request
    ) {
        vinculacionService.confirmarVinculacion(practicaId, request);
        return ResponseEntity.ok(ApiResponse.ok("Vinculación confirmada correctamente"));
    }

    @GetMapping("/practicas/{practicaId}/documentos")
    public ResponseEntity<ApiResponse<DocumentosPorCategoriaResponse>> listarDocumentos(
            @PathVariable Long practicaId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(vinculacionService.listarDocumentosPorPractica(practicaId)));
    }

    @GetMapping("/vinculaciones/documentos/{documentoId}/contenido")
    public ResponseEntity<byte[]> obtenerContenidoDocumento(@PathVariable Long documentoId) {
        byte[] contenido = vinculacionService.obtenerContenidoDocumento(documentoId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
                .body(contenido);
    }

    @DeleteMapping("/vinculaciones/documentos/{documentoId}")
    public ResponseEntity<ApiResponse<Void>> eliminarDocumento(@PathVariable Long documentoId) {
        vinculacionService.eliminarDocumento(documentoId);
        return ResponseEntity.ok(ApiResponse.ok("Documento eliminado correctamente"));
    }

    @PutMapping(value = "/vinculaciones/documentos/{documentoId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<DocumentoCargadoResponse>> reemplazarDocumento(
            @PathVariable Long documentoId,
            @RequestParam("archivo") MultipartFile archivo
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Documento reemplazado correctamente",
                vinculacionService.reemplazarDocumento(documentoId, archivo)
        ));
    }
}
