package com.avh.practicas.vinculacion.support;

import com.avh.practicas.auth.entity.Usuario;
import com.avh.practicas.auth.repository.AuthUsuarioRepository;
import com.avh.practicas.shared.exception.AccesoNoAutorizadoException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UsuarioAutenticadoProvider {

    private final AuthUsuarioRepository usuarioRepository;

    public Usuario obtener() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new AccesoNoAutorizadoException("Acceso denegado: usuario no autenticado");
        }

        return usuarioRepository.findByCorreo(auth.getName())
                .orElseThrow(() -> new AccesoNoAutorizadoException("Acceso denegado: usuario no encontrado"));
    }

    public Long obtenerId() {
        return obtener().getId();
    }
}
