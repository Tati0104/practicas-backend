package com.avh.practicas.usuario.service;

import com.avh.practicas.auth.entity.Usuario;
import com.avh.practicas.auth.repository.AuthUsuarioRepository;
import com.avh.practicas.correo.service.IMailService;
import com.avh.practicas.shared.enums.Rol;
import com.avh.practicas.shared.exception.RecursoNoEncontradoException;
import com.avh.practicas.usuario.dto.*;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UsuarioAdminService {

    private final AuthUsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final IMailService mailService;

    @Transactional(readOnly = true)
    public Page<UsuarioDto> listar(FiltroUsuarioRequest filtros, Pageable pageable) {
        Specification<Usuario> spec = construirEspecificacion(filtros);
        return usuarioRepository.findAll(spec, pageable).map(this::toDto);
    }

    public UsuarioDto crear(CrearUsuarioRequest dto) {
        if (usuarioRepository.existsByCorreo(dto.getCorreo())) {
            throw new IllegalArgumentException(
                    "Ya existe un usuario registrado con el correo: " + dto.getCorreo());
        }

        String passwordTemporal = generarPasswordTemporal();

        Usuario usuario = Usuario.builder()
                .nombre(dto.getNombre())
                .correo(dto.getCorreo())
                .passwordHash(passwordEncoder.encode(passwordTemporal))
                .rol(dto.getRol())
                .scope(dto.getScope())
                .activo(true)
                .primeraVez(true)
                .build();

        usuario = usuarioRepository.save(usuario);

        mailService.enviar(
                dto.getCorreo(),
                "Acceso al Sistema de Prácticas — AVH",
                "<p>Bienvenido/a <b>" + dto.getNombre() + "</b>.</p>" +
                        "<p>Tu contraseña temporal es: <b>" + passwordTemporal + "</b></p>" +
                        "<p>Debes cambiarla en tu primer inicio de sesión.</p>"
        );

        return toDto(usuario);
    }

    public UsuarioDto editar(Long id, EditarUsuarioRequest dto) {
        Usuario usuario = buscarPorId(id);
        usuario.setNombre(dto.getNombre());
        usuario.setRol(dto.getRol());
        usuario.setScope(dto.getScope());
        return toDto(usuarioRepository.save(usuario));
    }

    public void activar(Long id) {
        Usuario usuario = buscarPorId(id);
        usuario.setActivo(true);
        usuarioRepository.save(usuario);
    }

    public void inactivar(Long id) {
        Usuario usuario = buscarPorId(id);

        if (usuario.getRol() == Rol.ADMIN) {
            long adminsActivos = usuarioRepository.countByRolAndActivo(Rol.ADMIN, true);
            if (adminsActivos <= 1) {
                throw new IllegalStateException(
                        "No se puede inactivar: es el único administrador activo del sistema");
            }
        }

        usuario.setActivo(false);
        usuarioRepository.save(usuario);
    }

    private Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Usuario no encontrado con id: " + id));
    }

    private Specification<Usuario> construirEspecificacion(FiltroUsuarioRequest f) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (f.getRol() != null) {
                predicates.add(cb.equal(root.get("rol"), f.getRol()));
            }
            if (f.getActivo() != null) {
                predicates.add(cb.equal(root.get("activo"), f.getActivo()));
            }
            if (f.getScope() != null) {
                predicates.add(cb.equal(root.get("scope"), f.getScope()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
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

    private UsuarioDto toDto(Usuario u) {
        return UsuarioDto.builder()
                .id(u.getId())
                .nombre(u.getNombre())
                .correo(u.getCorreo())
                .rol(u.getRol())
                .scope(u.getScope())
                .activo(Boolean.TRUE.equals(u.getActivo()))
                .primeraVez(Boolean.TRUE.equals(u.getPrimeraVez()))
                .build();
    }
}
