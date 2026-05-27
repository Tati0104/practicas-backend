package com.avh.practicas.auth.service;

import com.avh.practicas.auth.dto.LoginResponse;
import com.avh.practicas.auth.entity.Usuario;
import com.avh.practicas.auth.repository.UsuarioRepository;
import com.avh.practicas.auth.security.JwtTokenProvider;
import com.avh.practicas.shared.exception.NegocioException;
import com.avh.practicas.shared.exception.RecursoNoEncontradoException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private static final int HORAS_VALIDEZ_TOKEN_RECUPERACION = 24;

    private final UsuarioRepository usuarioRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final IMailService mailService;

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(String correo, String password) {
        Usuario usuario = usuarioRepository.findByCorreo(correo.trim())
                .orElseThrow(() -> new NegocioException("Credenciales inválidas"));

        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new NegocioException("El usuario se encuentra inactivo");
        }

        if (!passwordEncoder.matches(password, usuario.getPasswordHash())) {
            throw new NegocioException("Credenciales inválidas");
        }

        return LoginResponse.builder()
                .token(jwtTokenProvider.generarToken(usuario))
                .rol(usuario.getRol())
                .scope(usuario.getScope())
                .nombre(usuario.getNombre())
                .build();
    }

    @Override
    public void cambiarPassword(Long id, String nuevaPassword) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el usuario con id: " + id));

        usuario.setPasswordHash(passwordEncoder.encode(nuevaPassword));
        usuario.setPrimeraVez(false);
        usuarioRepository.save(usuario);
    }

    @Override
    public void recuperar(String correo) {
        usuarioRepository.findByCorreo(correo.trim()).ifPresent(usuario -> {
            String token = UUID.randomUUID().toString();
            usuario.setTokenRecuperacion(token);
            usuario.setTokenExpiracion(LocalDateTime.now().plusHours(HORAS_VALIDEZ_TOKEN_RECUPERACION));
            usuarioRepository.save(usuario);
            mailService.enviarRecuperacionPassword(usuario.getCorreo(), token);
        });
    }

    @Override
    public void resetear(String token, String nuevaPassword) {
        Usuario usuario = usuarioRepository.findByTokenRecuperacion(token)
                .orElseThrow(() -> new NegocioException("Token de recuperación inválido"));

        if (usuario.getTokenExpiracion() == null
                || usuario.getTokenExpiracion().isBefore(LocalDateTime.now())) {
            throw new NegocioException("Token de recuperación expirado");
        }

        usuario.setPasswordHash(passwordEncoder.encode(nuevaPassword));
        usuario.setPrimeraVez(false);
        usuario.setTokenRecuperacion(null);
        usuario.setTokenExpiracion(null);
        usuarioRepository.save(usuario);
    }
}
