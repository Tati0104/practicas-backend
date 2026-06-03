package com.avh.practicas.vinculacion.adapter;

import com.avh.practicas.shared.exception.NegocioException;
import com.avh.practicas.vinculacion.config.VinculacionProperties;
import com.avh.practicas.vinculacion.port.AlmacenArchivosPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AlmacenArchivosLocalAdapter implements AlmacenArchivosPort {

    private final VinculacionProperties properties;

    @Override
    public String guardar(Long asignacionId, CategoriaAlmacen categoria, MultipartFile archivo) {
        try {
            Path directorio = Paths.get(properties.getDirectorioUpload(), categoria.name().toLowerCase(),
                    String.valueOf(asignacionId));
            Files.createDirectories(directorio);

            String nombreSeguro = UUID.randomUUID() + "_" + archivo.getOriginalFilename();
            Path destino = directorio.resolve(nombreSeguro);
            archivo.transferTo(destino);

            return destino.toString().replace('\\', '/');
        } catch (IOException ex) {
            throw new NegocioException("No se pudo guardar el archivo: " + ex.getMessage());
        }
    }
}
