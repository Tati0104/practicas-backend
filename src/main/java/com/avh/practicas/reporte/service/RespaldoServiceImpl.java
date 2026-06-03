package com.avh.practicas.reporte.service;

import com.avh.practicas.auth.entity.Usuario;
import com.avh.practicas.reporte.backup.pattern.abstractfactory.RespaldoFabricaExcel;
import com.avh.practicas.reporte.backup.pattern.bridge.PoiRenderizadorExcel;
import com.avh.practicas.reporte.backup.pattern.bridge.RenderizadorExcel;
import com.avh.practicas.seguimiento.service.BitacoraService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Implementación de RespaldoService para la generación de reportes consolidados en Excel (Sprint 6).
 * Utiliza patrones Abstract Factory y Bridge para desacoplar el origen de los datos y el motor de renderizado.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RespaldoServiceImpl implements RespaldoService {

    private final RespaldoFabricaExcel respaldoFabricaExcel;
    private final BitacoraService bitacoraService;

    @Async
    @Override
    public CompletableFuture<byte[]> generarRespaldoAsync(Usuario usuario) {
        log.info("Iniciando generación asíncrona de respaldo de base de datos solicitado por {}", usuario.getCorreo());
        bitacoraService.registrar("respaldo", "GENERAR", usuario, "Inicio de generación de respaldo completo en Excel.");

        try {
            // Instanciar el implementador concreto de Bridge (Apache POI)
            RenderizadorExcel renderizador = new PoiRenderizadorExcel();

            // Hojas del libro de respaldo definidas en el diseño
            List<String> hojas = List.of(
                    "Usuarios",
                    "Estudiantes",
                    "Expedientes con prácticas",
                    "Empresas",
                    "Vacantes",
                    "Notas registradas",
                    "Evaluaciones y encuestas",
                    "Bitácora últimos 2 años"
            );

            // Generar cada hoja utilizando la fábrica abstracta
            for (String hoja : hojas) {
                renderizador.crearHoja(hoja);

                // Escribir encabezados
                List<String> encabezados = respaldoFabricaExcel.obtenerEncabezados(hoja);
                List<Object> headersObj = new ArrayList<>(encabezados);
                renderizador.escribirFila(hoja, 0, headersObj);

                // Escribir datos
                List<List<Object>> datos = respaldoFabricaExcel.obtenerDatos(hoja);
                int rowNum = 1;
                for (List<Object> fila : datos) {
                    renderizador.escribirFila(hoja, rowNum++, fila);
                }
            }

            // Renderizar y obtener flujo de bytes
            byte[] bytes = renderizador.exportarBytes();
            log.info("Generación de respaldo en Excel finalizada con éxito.");
            bitacoraService.registrar("respaldo", "GENERAR", usuario, "Finalización de generación de respaldo completo en Excel con éxito.");
            
            return CompletableFuture.completedFuture(bytes);
        } catch (Exception e) {
            log.error("Error al generar el respaldo de base de datos", e);
            bitacoraService.registrar("respaldo", "GENERAR", usuario, "Error en la generación de respaldo completo en Excel: " + e.getMessage());
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public void guardarRespaldoLocal(byte[] bytes, String nombreArchivo, Usuario usuario) {
        try {
            File directory = new File("backups");
            if (!directory.exists()) {
                boolean created = directory.mkdirs();
                if (created) {
                    log.info("Directorio de respaldos creado: backups/");
                }
            }

            File file = new File(directory, nombreArchivo);
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(bytes);
            }

            log.info("Respaldo guardado localmente en: {}", file.getAbsolutePath());
            bitacoraService.registrar("respaldo", "GUARDAR_LOCAL", usuario,
                    "Respaldo guardado localmente debido a timeout del cliente: " + file.getName());
        } catch (Exception e) {
            log.error("Error al guardar respaldo localmente", e);
            bitacoraService.registrar("respaldo", "GUARDAR_LOCAL", usuario,
                    "Error al guardar respaldo localmente: " + e.getMessage());
        }
    }
}
