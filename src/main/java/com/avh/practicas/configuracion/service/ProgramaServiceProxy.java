package com.avh.practicas.configuracion.service;

import com.avh.practicas.auth.entity.Usuario;
import com.avh.practicas.auth.repository.UsuarioRepository;
import com.avh.practicas.configuracion.entity.Programa;
import com.avh.practicas.estudiante.entity.Estudiante;
import com.avh.practicas.estudiante.repository.EstudianteRepository;
import com.avh.practicas.shared.enums.Scope;
import com.avh.practicas.shared.security.ScopeGuard;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Primary
public class ProgramaServiceProxy implements ProgramaService {

    private final ProgramaService realService;
    private final ScopeGuard scopeGuard;
    private final UsuarioRepository usuarioRepository;
    private final EstudianteRepository estudianteRepository;

    public ProgramaServiceProxy(
            @Qualifier("programaServiceImpl") ProgramaService realService,
            ScopeGuard scopeGuard,
            UsuarioRepository usuarioRepository,
            EstudianteRepository estudianteRepository) {
        this.realService = realService;
        this.scopeGuard = scopeGuard;
        this.usuarioRepository = usuarioRepository;
        this.estudianteRepository = estudianteRepository;
    }

    private Usuario obtenerUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            String correo = (String) auth.getPrincipal();
            return usuarioRepository.findByCorreo(correo).orElse(null);
        }
        return null;
    }

    private Programa obtenerProgramaDelUsuario(Usuario usuario) {
        return estudianteRepository.findByCorreo(usuario.getCorreo())
                .map(Estudiante::getPrograma)
                .orElse(null);
    }

    @Override
    public Programa crear(Programa programa) {
        Usuario usuario = obtenerUsuarioActual();
        scopeGuard.verificarScope(usuario, programa, "CREAR");
        return realService.crear(programa);
    }

    @Override
    public Programa editar(Long id, Programa programaActualizado) {
        Usuario usuario = obtenerUsuarioActual();
        realService.obtenerPorId(id).ifPresent(programa -> 
            scopeGuard.verificarScope(usuario, programa, "EDITAR")
        );
        return realService.editar(id, programaActualizado);
    }

    @Override
    public void desactivar(Long id) {
        Usuario usuario = obtenerUsuarioActual();
        realService.obtenerPorId(id).ifPresent(programa -> 
            scopeGuard.verificarScope(usuario, programa, "DESACTIVAR")
        );
        realService.desactivar(id);
    }

    @Override
    public void activar(Long id) {
        Usuario usuario = obtenerUsuarioActual();
        realService.obtenerPorId(id).ifPresent(programa -> 
            scopeGuard.verificarScope(usuario, programa, "ACTIVAR")
        );
        realService.activar(id);
    }

    @Override
    public Optional<Programa> obtenerPorId(Long id) {
        Usuario usuario = obtenerUsuarioActual();
        Optional<Programa> programa = realService.obtenerPorId(id);
        programa.ifPresent(p -> scopeGuard.verificarScope(usuario, p, "LEER"));
        return programa;
    }

    @Override
    public List<Programa> obtenerTodos() {
        Usuario usuario = obtenerUsuarioActual();
        List<Programa> todos = realService.obtenerTodos();
        if (usuario != null && usuario.getScope() == Scope.PROGRAMA) {
            Programa programaUsuario = obtenerProgramaDelUsuario(usuario);
            if (programaUsuario != null) {
                return todos.stream()
                        .filter(p -> p.getId().equals(programaUsuario.getId()))
                        .collect(Collectors.toList());
            } else {
                return Collections.emptyList();
            }
        }
        return todos;
    }

    @Override
    public List<Programa> obtenerPorFacultad(Long facultadId) {
        Usuario usuario = obtenerUsuarioActual();
        List<Programa> todos = realService.obtenerPorFacultad(facultadId);
        if (usuario != null && usuario.getScope() == Scope.PROGRAMA) {
            Programa programaUsuario = obtenerProgramaDelUsuario(usuario);
            if (programaUsuario != null) {
                return todos.stream()
                        .filter(p -> p.getId().equals(programaUsuario.getId()))
                        .collect(Collectors.toList());
            } else {
                return Collections.emptyList();
            }
        }
        return todos;
    }

    @Override
    public List<Programa> obtenerActivosPorFacultad(Long facultadId) {
        Usuario usuario = obtenerUsuarioActual();
        List<Programa> todos = realService.obtenerActivosPorFacultad(facultadId);
        if (usuario != null && usuario.getScope() == Scope.PROGRAMA) {
            Programa programaUsuario = obtenerProgramaDelUsuario(usuario);
            if (programaUsuario != null) {
                return todos.stream()
                        .filter(p -> p.getId().equals(programaUsuario.getId()))
                        .collect(Collectors.toList());
            } else {
                return Collections.emptyList();
            }
        }
        return todos;
    }
}
