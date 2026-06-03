package com.avh.practicas.vinculacion.documento;

import com.avh.practicas.bitacora.service.BitacoraService;
import com.avh.practicas.estudiante.entity.InstanciaPractica;
import com.avh.practicas.shared.security.ScopeGuard;
import com.avh.practicas.vinculacion.config.VinculacionProperties;
import com.avh.practicas.vinculacion.support.UsuarioAutenticadoProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DocumentoProxyFactory {

    private final ScopeGuard scopeGuard;
    private final BitacoraService bitacoraService;
    private final UsuarioAutenticadoProvider usuarioProvider;
    private final VinculacionProperties properties;

    public IDocumento crear(InstanciaPractica practica, Path rutaArchivo, Long documentoId) {
        DocumentoReal real = new DocumentoReal(rutaArchivo);
        return new DocumentoProxy(practica, documentoId, real, scopeGuard, bitacoraService, usuarioProvider);
    }

    public IDocumento crearParaNuevaCarga(
            InstanciaPractica practica,
            String subdirectorio,
            String nombreOriginal,
            Long documentoId
    ) {
        String nombreSeguro = UUID.randomUUID() + "_" + nombreOriginal;
        Path ruta = Paths.get(
                properties.getDirectorioUpload(),
                subdirectorio,
                String.valueOf(practica.getId()),
                nombreSeguro
        );
        return crear(practica, ruta, documentoId);
    }

    public IDocumento crearDesdeRutaExistente(InstanciaPractica practica, String urlAlmacenada, Long documentoId) {
        return crear(practica, Paths.get(urlAlmacenada), documentoId);
    }
}
