package com.avh.practicas.empresa.service;

import com.avh.practicas.auth.entity.Usuario;
import com.avh.practicas.auth.repository.UsuarioRepository;
import com.avh.practicas.empresa.entity.Empresa;
import com.avh.practicas.estudiante.entity.Estudiante;
import com.avh.practicas.estudiante.repository.EstudianteRepository;
import com.avh.practicas.shared.enums.Scope;
import com.avh.practicas.shared.exception.AccesoNoAutorizadoException;
import com.avh.practicas.shared.security.ScopeGuard;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Primary
public class EmpresaServiceProxy implements EmpresaService {

    private final EmpresaService realService;
    private final ScopeGuard scopeGuard;
    private final UsuarioRepository usuarioRepository;
    private final EstudianteRepository estudianteRepository;

    public EmpresaServiceProxy(
            @Qualifier("empresaServiceImpl") EmpresaService realService,
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

    @Override
    public Empresa registrar(Empresa empresa) {
        Usuario usuario = obtenerUsuarioActual();
        scopeGuard.verificarScope(usuario, empresa, "REGISTRAR");
        return realService.registrar(empresa);
    }

    @Override
    public Empresa editar(Long id, Empresa empresaActualizada) {
        Usuario usuario = obtenerUsuarioActual();
        realService.obtenerPorId(id).ifPresent(empresa -> 
            scopeGuard.verificarScope(usuario, empresa, "EDITAR")
        );
        return realService.editar(id, empresaActualizada);
    }

    @Override
    public void desactivar(Long id) {
        Usuario usuario = obtenerUsuarioActual();
        realService.obtenerPorId(id).ifPresent(empresa -> 
            scopeGuard.verificarScope(usuario, empresa, "DESACTIVAR")
        );
        realService.desactivar(id);
    }

    @Override
    public void activar(Long id) {
        Usuario usuario = obtenerUsuarioActual();
        realService.obtenerPorId(id).ifPresent(empresa -> 
            scopeGuard.verificarScope(usuario, empresa, "ACTIVAR")
        );
        realService.activar(id);
    }

    @Override
    public Optional<Empresa> obtenerPorId(Long id) {
        Usuario usuario = obtenerUsuarioActual();
        Optional<Empresa> empresa = realService.obtenerPorId(id);
        empresa.ifPresent(emp -> scopeGuard.verificarScope(usuario, emp, "LEER"));
        return empresa;
    }

    @Override
    public Optional<Empresa> obtenerPorNit(String nit) {
        Usuario usuario = obtenerUsuarioActual();
        Optional<Empresa> empresa = realService.obtenerPorNit(nit);
        empresa.ifPresent(emp -> scopeGuard.verificarScope(usuario, emp, "LEER"));
        return empresa;
    }

    @Override
    public List<Empresa> obtenerPorPrograma(Long programaId) {
        Usuario usuario = obtenerUsuarioActual();
        if (usuario != null && usuario.getScope() == Scope.PROGRAMA) {
            Estudiante estudianteAsociado = estudianteRepository.findByCorreo(usuario.getCorreo()).orElse(null);
            if (estudianteAsociado == null || estudianteAsociado.getPrograma() == null 
                    || !estudianteAsociado.getPrograma().getId().equals(programaId)) {
                throw new AccesoNoAutorizadoException("Acceso denegado: recurso fuera del scope");
            }
        }
        return realService.obtenerPorPrograma(programaId);
    }

    @Override
    public Page<Empresa> listar(String sector, String programa, Boolean activo, Pageable pageable) {
        Usuario usuario = obtenerUsuarioActual();
        if (usuario != null && usuario.getScope() == Scope.PROGRAMA) {
            Estudiante estudianteAsociado = estudianteRepository.findByCorreo(usuario.getCorreo()).orElse(null);
            if (estudianteAsociado == null || estudianteAsociado.getPrograma() == null 
                    || programa == null || !estudianteAsociado.getPrograma().getNombre().equalsIgnoreCase(programa)) {
                throw new AccesoNoAutorizadoException("Acceso denegado: recurso fuera del scope");
            }
        }
        return realService.listar(sector, programa, activo, pageable);
    }
}
