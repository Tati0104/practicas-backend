package com.avh.practicas.vinculacion.controller;

import com.avh.practicas.asignacion.entity.EstadoAsignacion;
import com.avh.practicas.shared.api.ApiResponse;
import com.avh.practicas.shared.pattern.proxy.ScopeGuard;
import com.avh.practicas.vinculacion.dto.ConfirmarVinculacionRequest;
import com.avh.practicas.vinculacion.dto.DocumentoCargadoResponse;
import com.avh.practicas.vinculacion.dto.DocumentosAsignacionResponse;
import com.avh.practicas.vinculacion.dto.DocumentosPorCategoriaResponse;
import com.avh.practicas.vinculacion.dto.VinculacionListadoResponse;
import com.avh.practicas.vinculacion.entity.CategoriaDocumento;
import com.avh.practicas.vinculacion.entity.RolFirmaConvenio;
import com.avh.practicas.vinculacion.service.VinculacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class VinculacionController {

    private final VinculacionService vinculacionService;

    @GetMapping("/vinculaciones")
    public Page<VinculacionListadoResponse> listar(
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false) Long programaId,
            @RequestParam(required = false) Long empresaId,
            @RequestParam(required = false) EstadoAsignacion estado,
            Pageable pageable
    ) {
        return vinculacionService.listar(busqueda, programaId, empresaId, estado, pageable);
    }

    @GetMapping("/vinculaciones/practicas/{practicaId}/documentos-detalle")
    public ResponseEntity<ApiResponse<DocumentosAsignacionResponse>> obtenerDocumentosPractica(
            @PathVariable Long practicaId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(vinculacionService.obtenerDocumentosPractica(practicaId)));
    }

    @PostMapping(value = "/vinculaciones/practicas/{practicaId}/documentos/{categoria}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<DocumentoCargadoResponse>> cargarDocumentoPorPractica(
            @PathVariable Long practicaId,
            @PathVariable CategoriaDocumento categoria,
            @RequestParam("archivo") MultipartFile archivo
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Documento cargado correctamente",
                vinculacionService.cargarDocumentoPorPractica(practicaId, categoria, archivo)
        ));
    }

    @GetMapping("/vinculaciones/asignaciones/{asignacionId}/documentos")
    public ResponseEntity<ApiResponse<DocumentosAsignacionResponse>> obtenerDocumentosAsignacion(
            @PathVariable Long asignacionId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(vinculacionService.obtenerDocumentosAsignacion(asignacionId)));
    }

    @PostMapping(value = "/vinculaciones/asignaciones/{asignacionId}/documentos/{categoria}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<DocumentoCargadoResponse>> cargarDocumento(
            @PathVariable Long asignacionId,
            @PathVariable CategoriaDocumento categoria,
            @RequestParam("archivo") MultipartFile archivo
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Documento cargado correctamente",
                vinculacionService.cargarDocumento(asignacionId, categoria, archivo)
        ));
    }

    @PostMapping(value = "/vinculaciones/asignaciones/{asignacionId}/carta", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<DocumentoCargadoResponse>> cargarCarta(
            @PathVariable Long asignacionId,
            @RequestParam("archivo") MultipartFile archivo
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Carta de presentación cargada correctamente",
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

    @GetMapping("/vinculaciones/documentos/{documentoId}/descargar")
    public ResponseEntity<Resource> descargarDocumento(@PathVariable Long documentoId) {
        Resource resource = vinculacionService.descargarDocumento(documentoId);
        String nombre = vinculacionService.nombreDescargaDocumento(documentoId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombre + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
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

    @GetMapping("/practicas/{practicaId}/estudiante")
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> obtenerEstudiantePractica(
            @PathVariable Long practicaId
    ) {
        com.avh.practicas.estudiante.entity.Estudiante estudiante = vinculacionService.obtenerEstudiantePorPractica(practicaId);
        java.util.List<com.avh.practicas.estudiante.dto.DocumentoEstudianteDto> docs = estudiante.getDocumentos().stream()
                .map(com.avh.practicas.estudiante.dto.DocumentoEstudianteDto::desde)
                .toList();

        return ResponseEntity.ok(ApiResponse.ok(java.util.Map.of(
                "id", estudiante.getId(),
                "documentos", docs
        )));
    }

    @ScopeGuard("DOCENTE_ASESOR_ASIGNAR")
    @PatchMapping("/vinculaciones/asignaciones/{asignacionId}/docente-asesor")
    public ResponseEntity<ApiResponse<Void>> asignarDocenteAsesor(
            @PathVariable Long asignacionId,
            @RequestBody java.util.Map<String, Long> body
    ) {
        Long docenteAsesorId = body.get("docenteAsesorId");
        vinculacionService.asignarDocenteAsesor(asignacionId, docenteAsesorId);
        return ResponseEntity.ok(ApiResponse.ok("Docente Asesor asignado correctamente"));
    }
}
