package com.avh.practicas.shared.security;

import com.avh.practicas.usuario.entity.Rol;
import com.avh.practicas.usuario.entity.Usuario;
import org.springframework.stereotype.Component;

@Component
public class ScopeGuard {

    public void verificarPrograma(Usuario usuario, Long programaIdRecurso, String accion) {
        if (usuario == null) {
            throw new SecurityException("Acceso denegado: usuario no autenticado");
        }

        if (tieneRol(usuario, Rol.ADMIN) || tieneRol(usuario, Rol.DIRECCION)) {
            return;
        }

        // La base de datos define el scope por tablas especializadas:
        // coordinador_empresarial, secretaria_coord_empresarial, docente_asesor, estudiante, etc.
        // Cuando se integre Auth completo, aquí se consultará la tabla correspondiente al rol.
        if (programaIdRecurso == null) {
            throw new SecurityException("Acceso denegado: programa del recurso no definido para la acción " + accion);
        }
    }

    public void verificarRol(Usuario usuario, Rol rolRequerido, String accion) {
        if (usuario == null) {
            throw new SecurityException("Acceso denegado: usuario no autenticado");
        }

        if (tieneRol(usuario, Rol.ADMIN)) {
            return;
        }

        if (!tieneRol(usuario, rolRequerido)) {
            throw new SecurityException("Acceso denegado: rol insuficiente para la acción " + accion);
        }
    }

    private boolean tieneRol(Usuario usuario, Rol rol) {
        return usuario.getIdRol() != null && usuario.getIdRol().equals(rol.getIdRol());
    }
}
