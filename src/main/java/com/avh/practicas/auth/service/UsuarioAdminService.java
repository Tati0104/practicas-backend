package com.avh.practicas.auth.service;

import com.avh.practicas.auth.dto.UsuarioAdminRequest;
import com.avh.practicas.auth.dto.UsuarioAdminResponse;
import com.avh.practicas.auth.entity.Usuario;
import com.avh.practicas.auth.repository.AuthUsuarioRepository;
import com.avh.practicas.shared.enums.Rol;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioAdminService {

    private static final String CARACTERES = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final AuthUsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UsuarioAdminResponse> listarUsuarios() {
        return usuarioRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public UsuarioAdminResponse crearUsuario(UsuarioAdminRequest request) {
        String correo = normalizarCorreo(request.getCorreo());

        if (usuarioRepository.existsByCorreo(correo)) {
            throw new IllegalArgumentException("Ya existe un usuario con este correo");
        }

        String password = request.getPassword();

        if (password == null || password.isBlank()) {
            password = generarPasswordTemporal();
        }

        Usuario usuario = Usuario.builder()
                .nombre(request.getNombre())
                .correo(correo)
                .passwordHash(passwordEncoder.encode(password))
                .rol(request.getRol())
                .scope(request.getScope())
                .activo(request.getActivo() == null ? true : request.getActivo())
                .primeraVez(true)
                .build();

        Usuario guardado = usuarioRepository.save(usuario);
        return mapToResponse(guardado);
    }

    @Transactional
    public UsuarioAdminResponse actualizarUsuario(Long id, UsuarioAdminRequest request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        String correo = normalizarCorreo(request.getCorreo());

        usuarioRepository.findByCorreo(correo)
                .filter(encontrado -> !encontrado.getId().equals(id))
                .ifPresent(encontrado -> {
                    throw new IllegalArgumentException("Ya existe otro usuario con este correo");
                });

        usuario.setNombre(request.getNombre());
        usuario.setCorreo(correo);
        usuario.setRol(request.getRol());
        usuario.setScope(request.getScope());
        usuario.setActivo(request.getActivo() == null ? usuario.getActivo() : request.getActivo());

        Usuario actualizado = usuarioRepository.save(usuario);
        return mapToResponse(actualizado);
    }

    @Transactional
    public UsuarioAdminResponse activarUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        usuario.setActivo(true);
        return mapToResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioAdminResponse inactivarUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if (usuario.getRol() == Rol.ADMIN && Boolean.TRUE.equals(usuario.getActivo())) {
            long adminsActivos = usuarioRepository.countByRolAndActivo(Rol.ADMIN, true);

            if (adminsActivos <= 1) {
                throw new IllegalArgumentException("No se puede inactivar el único ADMIN activo");
            }
        }

        usuario.setActivo(false);
        return mapToResponse(usuarioRepository.save(usuario));
    }

    private UsuarioAdminResponse mapToResponse(Usuario usuario) {
        return UsuarioAdminResponse.builder()
                .id(usuario.getId())
                .nombre(usuario.getNombre())
                .correo(usuario.getCorreo())
                .rol(usuario.getRol())
                .scope(usuario.getScope())
                .activo(usuario.getActivo())
                .build();
    }

    private String normalizarCorreo(String correo) {
        return correo == null ? null : correo.trim().toLowerCase();
    }

    private String generarPasswordTemporal() {
        StringBuilder password = new StringBuilder();

        for (int i = 0; i < 8; i++) {
            password.append(CARACTERES.charAt(RANDOM.nextInt(CARACTERES.length())));
        }

        return password.toString();
    }
}