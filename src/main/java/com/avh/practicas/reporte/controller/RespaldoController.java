package com.avh.practicas.reporte.controller;

import com.avh.practicas.auth.entity.Usuario;
import com.avh.practicas.auth.repository.AuthUsuarioRepository;
import com.avh.practicas.reporte.service.RespaldoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Controlador REST para gestionar la generación de respaldos completos en Excel (Sprint 6).
 */
@RestController("reporteRespaldoController")
@RequestMapping("/respaldo")
@RequiredArgsConstructor
@Slf4j
public class RespaldoController {

    private final RespaldoService respaldoService;
    private final AuthUsuarioRepository usuarioRepository;

    /**
     * Endpoint para generar un respaldo completo de la base de datos.
     * Si la generación tarda menos de 15 segundos, se descarga inmediatamente.
     * En caso de superar los 15 segundos, se retorna HTTP 202 (Accepted) y el proceso continúa asíncronamente
     * para guardarse en la carpeta 'backups/'.
     */
    @GetMapping("/generar")
    @PreAuthorize("hasAnyRole('COORD_PRACTICA', 'ADMIN')")
    public ResponseEntity<?> generarRespaldo() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByCorreo(email)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró usuario asociado al correo: " + email));

        log.info("Usuario {} ha solicitado la generación de respaldo completo.", email);

        // Disparar proceso asíncrono
        CompletableFuture<byte[]> future = respaldoService.generarRespaldoAsync(usuario);

        try {
            // Esperar un máximo de 15 segundos
            byte[] bytes = future.get(15, TimeUnit.SECONDS);

            log.info("Respaldo completado antes del timeout de 15 segundos. Enviando archivo al cliente.");
            String filename = "respaldo_" + System.currentTimeMillis() + ".xlsx";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(bytes);

        } catch (TimeoutException e) {
            log.warn("La generación de respaldo excedió los 15 segundos. Se continuará en segundo plano.");
            String nombreArchivo = "respaldo_timeout_" + System.currentTimeMillis() + ".xlsx";

            // Registrar callback para que se guarde localmente una vez finalice
            future.thenAccept(bytes -> {
                respaldoService.guardarRespaldoLocal(bytes, nombreArchivo, usuario);
            });

            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body("El respaldo está siendo generado en segundo plano. Se guardará localmente con el nombre: " + nombreArchivo);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("El proceso de respaldo fue interrumpido", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Proceso interrumpido: " + e.getMessage());
        } catch (ExecutionException e) {
            log.error("Error en la ejecución de la generación de respaldo", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al generar respaldo: " + e.getCause().getMessage());
        }
    }
}
