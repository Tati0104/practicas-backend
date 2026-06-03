package com.avh.practicas.reporte.service;

import com.avh.practicas.auth.entity.Usuario;
import java.util.concurrent.CompletableFuture;

/**
 * Servicio para la gestión de respaldos completos en Excel (Sprint 6).
 */
public interface RespaldoService {

    /**
     * Genera un respaldo completo de la base de datos en formato Excel de manera asíncrona.
     *
     * @param usuario Solicitante de la acción.
     * @return CompletableFuture con los bytes del archivo Excel generado.
     */
    CompletableFuture<byte[]> generarRespaldoAsync(Usuario usuario);

    /**
     * Guarda el respaldo localmente en el servidor en la carpeta 'backups/'.
     *
     * @param bytes Contenido en bytes del libro de Excel.
     * @param nombreArchivo Nombre del archivo a guardar.
     * @param usuario Solicitante de la acción.
     */
    void guardarRespaldoLocal(byte[] bytes, String nombreArchivo, Usuario usuario);
}
