package com.avh.practicas.vinculacion.documento;

import com.avh.practicas.shared.exception.NegocioException;
import com.avh.practicas.shared.exception.RecursoNoEncontradoException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Sujeto real: operaciones directas sobre el sistema de archivos.
 */
public class DocumentoReal implements IDocumento {

    private final Path rutaArchivo;

    public DocumentoReal(Path rutaArchivo) {
        this.rutaArchivo = rutaArchivo;
    }

    @Override
    public byte[] getContenido() {
        if (!Files.exists(rutaArchivo)) {
            throw new RecursoNoEncontradoException("El archivo no existe en el almacenamiento: " + rutaArchivo);
        }
        try {
            return Files.readAllBytes(rutaArchivo);
        } catch (IOException ex) {
            throw new NegocioException("No se pudo leer el documento: " + ex.getMessage());
        }
    }

    @Override
    public void cargar(MultipartFile archivo, String nombre) {
        try {
            Files.createDirectories(rutaArchivo.getParent());
            archivo.transferTo(rutaArchivo);
        } catch (IOException ex) {
            throw new NegocioException("No se pudo cargar el documento '" + nombre + "': " + ex.getMessage());
        }
    }

    @Override
    public void eliminar() {
        try {
            Files.deleteIfExists(rutaArchivo);
        } catch (IOException ex) {
            throw new NegocioException("No se pudo eliminar el documento: " + ex.getMessage());
        }
    }

    @Override
    public void reemplazar(MultipartFile archivo) {
        try {
            Files.createDirectories(rutaArchivo.getParent());
            archivo.transferTo(rutaArchivo);
        } catch (IOException ex) {
            throw new NegocioException("No se pudo reemplazar el documento: " + ex.getMessage());
        }
    }

    @Override
    public String getRutaAlmacenamiento() {
        return rutaArchivo.toString().replace('\\', '/');
    }
}
