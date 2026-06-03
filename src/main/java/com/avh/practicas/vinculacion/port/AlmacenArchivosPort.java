package com.avh.practicas.vinculacion.port;

import org.springframework.web.multipart.MultipartFile;

/**
 * Puerto de almacenamiento de archivos (DIP — el dominio no depende del filesystem).
 */
public interface AlmacenArchivosPort {

    String guardar(Long asignacionId, CategoriaAlmacen categoria, MultipartFile archivo);

    enum CategoriaAlmacen {
        CARTA,
        CONVENIO
    }
}
