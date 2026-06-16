package com.avh.practicas.usuario.service;

import com.avh.practicas.auth.entity.Usuario;
import com.avh.practicas.auth.repository.AuthUsuarioRepository;
import com.avh.practicas.correo.service.IMailService;
import com.avh.practicas.shared.enums.Rol;
import com.avh.practicas.shared.enums.Scope;
import com.avh.practicas.shared.enums.ScopePorRol;
import com.avh.practicas.shared.exception.NegocioException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final AuthUsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final CorreoPersonaService correoPersonaService;
    private final IMailService mailService;

    @Transactional
    public Usuario crearUsuarioDocenteAsesor(String nombreCompleto, String correo) {
        return crearConRol(Rol.DOCENTE_ASESOR, nombreCompleto, correo, true);
    }

    @Transactional
    public Usuario crearUsuarioEstudiante(String nombre, String correo) {
        return crearConRol(Rol.ESTUDIANTE, nombre, correo, true);
    }

    @Transactional
    public Usuario crearUsuarioTutorEmpresarial(String nombre, String correo) {
        return crearConRol(Rol.TUTOR_EMPRESARIAL, nombre, correo, true);
    }

    @Transactional
    public Usuario crearConRol(Rol rol, String nombre, String correo, boolean enviarCorreo) {
        String correoNormalizado = correoPersonaService.normalizar(correo);
        correoPersonaService.validarCorreoDisponible(correoNormalizado, CorreoPersonaService.Exclusiones.ninguna());

        String passwordTemporal = generarPasswordTemporal();
        Usuario usuario = Usuario.builder()
                .nombre(nombre.trim())
                .correo(correoNormalizado)
                .passwordHash(passwordEncoder.encode(passwordTemporal))
                .rol(rol)
                .scope(ScopePorRol.resolver(rol))
                .activo(true)
                .primeraVez(true)
                .build();

        usuario = usuarioRepository.save(usuario);

        if (enviarCorreo) {
            enviarCorreoBienvenida(usuario.getCorreo(), usuario.getNombre(), passwordTemporal);
        }

        return usuario;
    }

    @Transactional
    public void sincronizarPerfil(Usuario usuario, String nombre, String correo) {
        if (usuario == null) {
            return;
        }
        usuario.setNombre(nombre.trim());
        usuario.setCorreo(correoPersonaService.normalizar(correo));
        usuarioRepository.save(usuario);
    }

    @Transactional(readOnly = true)
    public Usuario obtener(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new NegocioException("Usuario no encontrado"));
    }

    private void enviarCorreoBienvenida(String correo, String nombre, String passwordTemporal) {
        mailService.enviar(
                correo,
                "Acceso al Sistema de Prácticas — AVH",
                "<p>Bienvenido/a <b>" + nombre + "</b>.</p>" +
                        "<p>Tu contraseña temporal es: <b>" + passwordTemporal + "</b></p>" +
                        "<p>Debes cambiarla en tu primer inicio de sesión.</p>"
        );
    }

    private String generarPasswordTemporal() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(8);
        for (int i = 0; i < 8; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
