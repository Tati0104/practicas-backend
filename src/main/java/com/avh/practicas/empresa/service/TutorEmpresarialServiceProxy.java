package com.avh.practicas.empresa.service;

import com.avh.practicas.auth.entity.Usuario;
import com.avh.practicas.auth.repository.AuthUsuarioRepository;
import com.avh.practicas.empresa.entity.TutorEmpresarial;
import com.avh.practicas.shared.security.ScopeGuard;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Primary
public class TutorEmpresarialServiceProxy implements TutorEmpresarialService {

    private final TutorEmpresarialService realService;
    private final ScopeGuard scopeGuard;
    private final AuthUsuarioRepository usuarioRepository;

    public TutorEmpresarialServiceProxy(
            @Qualifier("tutorEmpresarialServiceImpl") TutorEmpresarialService realService,
            ScopeGuard scopeGuard,
            AuthUsuarioRepository usuarioRepository) {
        this.realService = realService;
        this.scopeGuard = scopeGuard;
        this.usuarioRepository = usuarioRepository;
    }

    private Usuario obtenerUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            String correo = (String) auth.getPrincipal();
            return usuarioRepository.findByCorreo(correo).orElse(null);
        }
        return null;
    }

    @Override
    public TutorEmpresarial registrar(TutorEmpresarial tutor) {
        Usuario usuario = obtenerUsuarioActual();
        scopeGuard.verificarScope(usuario, tutor, "REGISTRAR");
        return realService.registrar(tutor);
    }

    @Override
    public TutorEmpresarial editar(Long id, TutorEmpresarial tutorActualizado) {
        Usuario usuario = obtenerUsuarioActual();
        realService.obtenerPorId(id).ifPresent(tutor -> 
            scopeGuard.verificarScope(usuario, tutor, "EDITAR")
        );
        return realService.editar(id, tutorActualizado);
    }

    @Override
    public void desactivar(Long id) {
        Usuario usuario = obtenerUsuarioActual();
        realService.obtenerPorId(id).ifPresent(tutor -> 
            scopeGuard.verificarScope(usuario, tutor, "DESACTIVAR")
        );
        realService.desactivar(id);
    }

    @Override
    public void activar(Long id) {
        Usuario usuario = obtenerUsuarioActual();
        realService.obtenerPorId(id).ifPresent(tutor -> 
            scopeGuard.verificarScope(usuario, tutor, "ACTIVAR")
        );
        realService.activar(id);
    }

    @Override
    public Optional<TutorEmpresarial> obtenerPorId(Long id) {
        Usuario usuario = obtenerUsuarioActual();
        Optional<TutorEmpresarial> tutor = realService.obtenerPorId(id);
        tutor.ifPresent(t -> scopeGuard.verificarScope(usuario, t, "LEER"));
        return tutor;
    }

    @Override
    public List<TutorEmpresarial> obtenerPorEmpresa(Long empresaId) {
        // Al ser tutores de una empresa en particular, no tienen una validación de programa directa.
        // Delegamos y dejamos que el controlador/interceptores de controlador manejen otros permisos.
        return realService.obtenerPorEmpresa(empresaId);
    }

    @Override
    public List<TutorEmpresarial> obtenerActivosPorEmpresa(Long empresaId) {
        return realService.obtenerActivosPorEmpresa(empresaId);
    }
}
