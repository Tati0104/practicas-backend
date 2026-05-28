package com.avh.practicas.usuario.service;

import com.avh.practicas.usuario.entity.Rol;
import com.avh.practicas.usuario.entity.Scope;
import com.avh.practicas.usuario.entity.Usuario;
import com.avh.practicas.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Usuario crearUsuarioDocenteAsesor(String nombreCompleto, String correo) {
        String correoNormalizado = correo == null ? null : correo.trim().toLowerCase();

        return usuarioRepository.findByCorreo(correoNormalizado)
                .map(usuario -> {
                    usuario.setNombre(nombreCompleto);
                    usuario.setRol(Rol.DOCENTE_ASESOR);
                    usuario.setScope(usuario.getScope() == null ? Scope.PROGRAMA : usuario.getScope());
                    usuario.setActivo(true);
                    return usuarioRepository.save(usuario);
                })
                .orElseGet(() -> usuarioRepository.save(Usuario.builder()
                        .nombre(nombreCompleto)
                        .correo(correoNormalizado)
                        .passwordHash(passwordEncoder.encode(generarPasswordTemporal()))
                        .rol(Rol.DOCENTE_ASESOR)
                        .scope(Scope.PROGRAMA)
                        .activo(true)
                        .primeraVez(true)
                        .build()));
    }

    @Transactional(readOnly = true)
    public Usuario obtener(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
    }

    private String generarPasswordTemporal() {
        return "TEMP-" + UUID.randomUUID();
    }
}
