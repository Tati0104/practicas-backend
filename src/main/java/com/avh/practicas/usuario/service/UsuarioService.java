package com.avh.practicas.usuario.service;

import com.avh.practicas.usuario.entity.Rol;
import com.avh.practicas.usuario.entity.Usuario;
import com.avh.practicas.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    @Transactional
    public Usuario crearUsuarioDocenteAsesor(String nombreCompleto, String correo) {
        return usuarioRepository.findByCorreo(correo)
                .map(usuario -> {
                    usuario.setRol(Rol.DOCENTE_ASESOR);
                    usuario.setActivo(true);
                    return usuarioRepository.save(usuario);
                })
                .orElseGet(() -> usuarioRepository.save(Usuario.builder()
                        .correo(correo)
                        .password(generarPasswordTemporal())
                        .rol(Rol.DOCENTE_ASESOR)
                        .activo(true)
                        .build()));
    }

    @Transactional(readOnly = true)
    public Usuario obtener(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
    }

    private String generarPasswordTemporal() {
        // Temporal para integración inicial. El módulo Auth/JWT de Tati debe reemplazarlo por BCrypt.
        return "TEMP-" + UUID.randomUUID();
    }
}
