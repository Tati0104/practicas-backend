package com.avh.practicas.vinculacion.support;

import com.avh.practicas.shared.exception.NegocioException;
import com.avh.practicas.vinculacion.config.VinculacionProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class ValidadorArchivoVinculacion {

    private static final Set<String> TIPOS_PERMITIDOS = Set.of(
            "application/pdf",
            "image/jpeg",
            "image/jpg",
            "image/png"
    );

    private static final Set<String> EXTENSIONES_PERMITIDAS = Set.of("pdf", "jpg", "jpeg", "png");

    private final VinculacionProperties properties;

    public void validar(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new NegocioException("Debe adjuntar un archivo.");
        }

        if (archivo.getSize() > properties.getTamanoMaximoBytes()) {
            throw new NegocioException("El archivo supera el tamaño máximo permitido de 10MB.");
        }

        String contentType = archivo.getContentType();
        if (contentType != null && TIPOS_PERMITIDOS.contains(contentType.toLowerCase(Locale.ROOT))) {
            return;
        }

        String extension = obtenerExtension(archivo.getOriginalFilename());
        if (!EXTENSIONES_PERMITIDAS.contains(extension)) {
            throw new NegocioException("Tipo de archivo no permitido. Use PDF, JPG o PNG.");
        }
    }

    public String resolverTipo(MultipartFile archivo) {
        String contentType = archivo.getContentType();
        if (StringUtils.hasText(contentType)) {
            return contentType;
        }
        return "application/" + obtenerExtension(archivo.getOriginalFilename());
    }

    private String obtenerExtension(String nombre) {
        if (!StringUtils.hasText(nombre) || !nombre.contains(".")) {
            return "";
        }
        return nombre.substring(nombre.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }
}
