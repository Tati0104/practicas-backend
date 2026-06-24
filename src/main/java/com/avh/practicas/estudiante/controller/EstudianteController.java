package com.avh.practicas.estudiante.controller;

import com.avh.practicas.estudiante.adapter.ImportadorEstudiantes;
import com.avh.practicas.estudiante.dto.EstudianteDto;
import com.avh.practicas.estudiante.dto.ResultadoImportacion;
import com.avh.practicas.estudiante.entity.EstadoAptitud;
import com.avh.practicas.estudiante.entity.Estudiante;
import com.avh.practicas.estudiante.service.EstudianteService;
import com.avh.practicas.shared.exception.NegocioException;
import com.avh.practicas.shared.exception.RecursoNoEncontradoException;
import com.avh.practicas.shared.pattern.proxy.ScopeGuard;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/estudiantes")
@RequiredArgsConstructor
public class EstudianteController {

    private final EstudianteService estudianteService;
    private final ImportadorEstudiantes importadorExcelAdapter;

    @GetMapping
    @ScopeGuard("ESTUDIANTE_LISTAR")
    public ResponseEntity<Page<Estudiante>> listar(
            @RequestParam(required = false) String programa,
            @RequestParam(required = false) String facultad,
            @RequestParam(required = false) EstadoAptitud aptitud,
            @RequestParam(required = false) String estadoPractica,
            @RequestParam(required = false) String busqueda,
            Pageable pageable) {
        Page<Estudiante> estudiantes = estudianteService.listar(programa, facultad, aptitud, estadoPractica, busqueda, pageable);
        return ResponseEntity.ok(estudiantes);
    }

    @GetMapping("/{id}")
    @ScopeGuard("ESTUDIANTE_LISTAR")
    public ResponseEntity<Estudiante> obtenerPorId(@PathVariable Long id) {
        Estudiante estudiante = estudianteService.obtenerPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el estudiante con id: " + id));
        return ResponseEntity.ok(estudiante);
    }

    @PostMapping
    @ScopeGuard("ESTUDIANTE_REGISTRAR")
    public ResponseEntity<Estudiante> registrar(@Valid @RequestBody EstudianteDto dto) {
        Estudiante estudiante = estudianteService.registrar(dto);
        return new ResponseEntity<>(estudiante, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @ScopeGuard("ESTUDIANTE_EDITAR")
    public ResponseEntity<Estudiante> editar(@PathVariable Long id, @Valid @RequestBody EstudianteDto dto) {
        Estudiante estudianteActualizado = estudianteService.editar(id, dto);
        return ResponseEntity.ok(estudianteActualizado);
    }

    @DeleteMapping("/{id}")
    @ScopeGuard("ESTUDIANTE_EDITAR")
    public ResponseEntity<Map<String, String>> eliminar(@PathVariable Long id) {
        estudianteService.eliminar(id);
        return ResponseEntity.ok(Map.of("mensaje", "Estudiante eliminado correctamente"));
    }

    @PatchMapping("/{id}/aptitud")
    @ScopeGuard("ESTUDIANTE_APTITUD")
    public ResponseEntity<Estudiante> actualizarAptitud(
            @PathVariable Long id, 
            @RequestBody Map<String, Object> body) {
        
        if (!body.containsKey("aptitud")) {
            throw new NegocioException("Debe proporcionar el campo 'aptitud' (APTO / NO_APTO) en el cuerpo de la solicitud.");
        }

        String aptitudStr = ((String) body.get("aptitud")).toUpperCase();
        Estudiante estudiante;

        if ("APTO".equals(aptitudStr)) {
            Integer numeroPractica = null;
            if (body.get("numeroPractica") instanceof Number n) {
                numeroPractica = n.intValue();
            } else if (body.get("numeroPractica") != null) {
                try {
                    numeroPractica = Integer.parseInt(String.valueOf(body.get("numeroPractica")));
                } catch (NumberFormatException ignored) {
                    throw new NegocioException("El campo numeroPractica debe ser un número entre 1 y 5.");
                }
            }
            estudiante = estudianteService.marcarApto(id, numeroPractica);
        } else if ("NO_APTO".equals(aptitudStr)) {
            String motivo = (String) body.getOrDefault("motivo", "No cumple requisitos académicos.");
            estudiante = estudianteService.marcarNoApto(id, motivo);
        } else {
            throw new NegocioException("El valor de aptitud proporcionado no es válido. Debe ser APTO o NO_APTO.");
        }

        return ResponseEntity.ok(estudiante);
    }

    @PostMapping("/importar")
    @ScopeGuard("ESTUDIANTE_IMPORTAR")
    public ResponseEntity<?> importarExcel(@RequestParam("archivo") MultipartFile archivo) {
        // Limite de 5MB
        if (archivo.getSize() > 5 * 1024 * 1024) {
            return ResponseEntity.badRequest().body(Map.of("error", "El archivo supera el tamaño máximo permitido de 5MB."));
        }

        try {
            byte[] bytes = archivo.getBytes();
            ResultadoImportacion preLectura = importadorExcelAdapter.importar(bytes);

            if (preLectura.getTotal() > 50) {
                // Ejecución asíncrona @Async
                importadorExcelAdapter.importarAsync(bytes).thenAccept(resultado -> {
                    if (resultado.getErrores().isEmpty()) {
                        estudianteService.importar(resultado.getExitosos());
                    }
                });

                return ResponseEntity.accepted().body(Map.of(
                        "mensaje", "El archivo contiene " + preLectura.getTotal() + " registros (mayor a 50). Se ha iniciado el procesamiento asíncrono en segundo plano."
                ));
            } else {
                // Ejecución síncrona
                if (preLectura.getErrores().isEmpty()) {
                    estudianteService.importar(preLectura.getExitosos());
                }
                return ResponseEntity.ok(preLectura);
            }

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al leer el archivo enviado: " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/documentos")
    @ScopeGuard("ESTUDIANTE_EDITAR")
    public ResponseEntity<?> cargarDocumento(
            @PathVariable Long id,
            @RequestParam("tipo") String tipo,
            @RequestParam("archivo") MultipartFile archivo) {
        
        Estudiante estudiante = estudianteService.obtenerPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el estudiante con id: " + id));

        try {
            // Guardar localmente
            java.nio.file.Path directorio = java.nio.file.Paths.get("uploads", "estudiantes", String.valueOf(id));
            java.nio.file.Files.createDirectories(directorio);
            String nombreSeguro = java.util.UUID.randomUUID() + "_" + archivo.getOriginalFilename();
            java.nio.file.Path destino = directorio.resolve(nombreSeguro);
            archivo.transferTo(destino);

            String url = destino.toString().replace('\\', '/');

            com.avh.practicas.estudiante.entity.DocumentoEstudiante doc = com.avh.practicas.estudiante.entity.DocumentoEstudiante.builder()
                    .estudianteId(id)
                    .nombre(archivo.getOriginalFilename())
                    .url(url)
                    .tipo(tipo)
                    .build();

            estudiante.getDocumentos().add(doc);
            estudianteService.guardar(estudiante);

            return ResponseEntity.ok(Map.of(
                    "mensaje", "Documento cargado correctamente",
                    "url", url
            ));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al guardar el archivo: " + e.getMessage()));
        }
    }
}
