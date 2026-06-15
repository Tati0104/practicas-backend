package com.avh.practicas.usuario.service;

import com.avh.practicas.auth.entity.Usuario;
import com.avh.practicas.auth.repository.AuthUsuarioRepository;
import com.avh.practicas.configuracion.entity.Facultad;
import com.avh.practicas.configuracion.repository.FacultadRepository;
import com.avh.practicas.correo.service.IMailService;
import com.avh.practicas.empresa.entity.Empresa;
import com.avh.practicas.empresa.entity.TutorEmpresarial;
import com.avh.practicas.empresa.repository.EmpresaRepository;
import com.avh.practicas.empresa.repository.TutorEmpresarialRepository;
import com.avh.practicas.shared.enums.Rol;
import com.avh.practicas.shared.enums.Scope;
import com.avh.practicas.shared.enums.ScopePorRol;
import com.avh.practicas.shared.exception.NegocioException;
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
    private final FacultadRepository facultadRepository;
    private final EmpresaRepository empresaRepository;
    private final TutorEmpresarialRepository tutorEmpresarialRepository;
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
                .scope(ScopePorRol.resolver(dto.getRol()))
                .activo(true)
                .primeraVez(true)
                .build();

        aplicarFacultad(usuario, dto.getRol(), dto.getFacultadId());
        usuario = usuarioRepository.save(usuario);

        if (dto.getRol() == Rol.TUTOR_EMPRESARIAL) {
            registrarTutorEmpresarial(usuario, dto);
        }

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
        usuario.setScope(ScopePorRol.resolver(dto.getRol()));
        aplicarFacultad(usuario, dto.getRol(), dto.getFacultadId());
        usuario = usuarioRepository.save(usuario);

        if (dto.getRol() == Rol.TUTOR_EMPRESARIAL) {
            actualizarTutorEmpresarial(usuario, dto);
        }

        return toDto(usuario);
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

    private void aplicarFacultad(Usuario usuario, Rol rol, Long facultadId) {
        Scope scope = ScopePorRol.resolver(rol);
        if (scope == Scope.FACULTAD) {
            if (facultadId == null) {
                throw new NegocioException("Debe seleccionar la facultad para este rol.");
            }
            Facultad facultad = facultadRepository.findById(facultadId)
                    .orElseThrow(() -> new RecursoNoEncontradoException(
                            "No se encontró la facultad con id: " + facultadId));
            usuario.setFacultad(facultad);
            return;
        }
        usuario.setFacultad(null);
    }

    private void registrarTutorEmpresarial(Usuario usuario, CrearUsuarioRequest dto) {
        Empresa empresa = obtenerEmpresaActiva(dto.getEmpresaId());
        validarDatosTutor(dto.getEmpresaId(), dto.getTelefonoTutor());

        if (tutorEmpresarialRepository.existsByCorreo(dto.getCorreo())) {
            throw new NegocioException("Ya existe un tutor registrado con el correo: " + dto.getCorreo());
        }

        TutorEmpresarial tutor = TutorEmpresarial.builder()
                .empresa(empresa)
                .nombre(usuario.getNombre())
                .cargo(dto.getCargoTutor() != null && !dto.getCargoTutor().isBlank()
                        ? dto.getCargoTutor()
                        : "Tutor empresarial")
                .correo(usuario.getCorreo())
                .telefono(dto.getTelefonoTutor().trim())
                .usuarioId(usuario.getId())
                .activo(true)
                .build();

        tutorEmpresarialRepository.save(tutor);
    }

    private void actualizarTutorEmpresarial(Usuario usuario, EditarUsuarioRequest dto) {
        Empresa empresa = obtenerEmpresaActiva(dto.getEmpresaId());
        validarDatosTutor(dto.getEmpresaId(), dto.getTelefonoTutor());

        TutorEmpresarial tutor = tutorEmpresarialRepository.findByCorreo(usuario.getCorreo())
                .orElseGet(() -> TutorEmpresarial.builder()
                        .correo(usuario.getCorreo())
                        .usuarioId(usuario.getId())
                        .activo(true)
                        .build());

        tutor.setEmpresa(empresa);
        tutor.setNombre(usuario.getNombre());
        tutor.setCargo(dto.getCargoTutor() != null && !dto.getCargoTutor().isBlank()
                ? dto.getCargoTutor()
                : "Tutor empresarial");
        tutor.setTelefono(dto.getTelefonoTutor().trim());
        tutor.setUsuarioId(usuario.getId());
        tutor.setActivo(true);
        tutorEmpresarialRepository.save(tutor);
    }

    private Empresa obtenerEmpresaActiva(Long empresaId) {
        if (empresaId == null) {
            throw new NegocioException("Debe seleccionar la empresa para el tutor empresarial.");
        }
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró la empresa con id: " + empresaId));
        if (!Boolean.TRUE.equals(empresa.getActivo())) {
            throw new NegocioException("La empresa seleccionada está inactiva.");
        }
        return empresa;
    }

    private void validarDatosTutor(Long empresaId, String telefono) {
        if (empresaId == null) {
            throw new NegocioException("Debe seleccionar la empresa para el tutor empresarial.");
        }
        if (telefono == null || telefono.isBlank()) {
            throw new NegocioException("El teléfono es obligatorio para el tutor empresarial.");
        }
    }

    private UsuarioDto toDto(Usuario u) {
        UsuarioDto.UsuarioDtoBuilder builder = UsuarioDto.builder()
                .id(u.getId())
                .nombre(u.getNombre())
                .correo(u.getCorreo())
                .rol(u.getRol())
                .scope(u.getScope())
                .activo(Boolean.TRUE.equals(u.getActivo()))
                .primeraVez(Boolean.TRUE.equals(u.getPrimeraVez()))
                .facultadId(u.getFacultad() != null ? u.getFacultad().getId() : null);

        if (u.getRol() == Rol.TUTOR_EMPRESARIAL) {
            tutorEmpresarialRepository.findByCorreo(u.getCorreo()).ifPresent(tutor -> {
                builder.empresaId(tutor.getEmpresa().getId());
                builder.cargoTutor(tutor.getCargo());
                builder.telefonoTutor(tutor.getTelefono());
            });
        }

        return builder.build();
    }
}
