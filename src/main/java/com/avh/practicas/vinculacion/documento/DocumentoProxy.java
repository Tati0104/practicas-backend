package com.avh.practicas.vinculacion.documento;

import com.avh.practicas.auth.entity.Usuario;
import com.avh.practicas.bitacora.entity.TipoAccion;
import com.avh.practicas.bitacora.service.BitacoraService;
import com.avh.practicas.estudiante.entity.InstanciaPractica;
import com.avh.practicas.shared.exception.AccesoNoAutorizadoException;
import com.avh.practicas.shared.security.ScopeGuard;
import com.avh.practicas.vinculacion.support.UsuarioAutenticadoProvider;
import org.springframework.web.multipart.MultipartFile;

/**
 * Proxy (PE-32): control de inmutabilidad, scope y auditoría antes de delegar en {@link DocumentoReal}.
 */
public class DocumentoProxy implements IDocumento {

    private static final String MENSAJE_INMUTABLE = "Práctica cerrada: documentos inmutables";

    private final InstanciaPractica practica;
    private final Long documentoId;
    private final DocumentoReal documentoReal;
    private final ScopeGuard scopeGuard;
    private final BitacoraService bitacoraService;
    private final UsuarioAutenticadoProvider usuarioProvider;

    public DocumentoProxy(
            InstanciaPractica practica,
            Long documentoId,
            DocumentoReal documentoReal,
            ScopeGuard scopeGuard,
            BitacoraService bitacoraService,
            UsuarioAutenticadoProvider usuarioProvider
    ) {
        this.practica = practica;
        this.documentoId = documentoId;
        this.documentoReal = documentoReal;
        this.scopeGuard = scopeGuard;
        this.bitacoraService = bitacoraService;
        this.usuarioProvider = usuarioProvider;
    }

    @Override
    public byte[] getContenido() {
        verificarScope("LEER_DOCUMENTO");
        return documentoReal.getContenido();
    }

    @Override
    public void cargar(MultipartFile archivo, String nombre) {
        verificarMutacion("CARGAR_DOCUMENTO");
        documentoReal.cargar(archivo, nombre);
        registrarBitacora(TipoAccion.CREACION, "Documento cargado: " + nombre);
    }

    @Override
    public void eliminar() {
        verificarMutacion("ELIMINAR_DOCUMENTO");
        documentoReal.eliminar();
        registrarBitacora(TipoAccion.CANCELACION, "Documento eliminado. ID: " + documentoId);
    }

    @Override
    public void reemplazar(MultipartFile archivo) {
        verificarMutacion("REEMPLAZAR_DOCUMENTO");
        documentoReal.reemplazar(archivo);
        registrarBitacora(TipoAccion.MODIFICACION, "Documento reemplazado. ID: " + documentoId);
    }

    @Override
    public String getRutaAlmacenamiento() {
        return documentoReal.getRutaAlmacenamiento();
    }

    private void verificarMutacion(String accion) {
        verificarInmutabilidad();
        verificarScope(accion);
    }

    private void verificarInmutabilidad() {
        if (Boolean.TRUE.equals(practica.getInmutable())) {
            throw new AccesoNoAutorizadoException(MENSAJE_INMUTABLE);
        }
    }

    private void verificarScope(String accion) {
        Usuario usuario = usuarioProvider.obtener();
        scopeGuard.verificarScope(usuario, practica, accion);
    }

    private void registrarBitacora(TipoAccion tipoAccion, String detalle) {
        Usuario usuario = usuarioProvider.obtener();
        bitacoraService.registrar(
                usuario.getId(),
                "documentos_practica",
                tipoAccion,
                documentoId,
                null,
                detalle
        );
    }
}
