package com.avh.practicas.vinculacion.documento;

import org.springframework.web.multipart.MultipartFile;

/**
 * Contrato de acceso a documentos de práctica (PE-32 Proxy).
 */
public interface IDocumento {

    byte[] getContenido();

    void cargar(MultipartFile archivo, String nombre);

    void eliminar();

    void reemplazar(MultipartFile archivo);

    String getRutaAlmacenamiento();
}
