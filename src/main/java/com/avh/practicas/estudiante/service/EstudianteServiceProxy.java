package com.avh.practicas.estudiante.service;

import com.avh.practicas.auth.entity.Usuario;
import com.avh.practicas.auth.repository.AuthUsuarioRepository;
import com.avh.practicas.configuracion.entity.Programa;
import com.avh.practicas.configuracion.repository.ProgramaRepository;
import com.avh.practicas.estudiante.dto.EstudianteDto;
import com.avh.practicas.estudiante.entity.EstadoAptitud;
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
public class EstudianteServiceProxy implements EstudianteService {

    private final EstudianteService realService;
    private final ScopeGuard scopeGuard;
    private final AuthUsuarioRepository usuarioRepository;
    private final EstudianteRepository estudianteRepository;
    private final ProgramaRepository programaRepository;

    public EstudianteServiceProxy(
            @Qualifier("estudianteServiceImpl") EstudianteService realService,
            ScopeGuard scopeGuard,
            AuthUsuarioRepository usuarioRepository,
            EstudianteRepository estudianteRepository,
            ProgramaRepository programaRepository) {
        this.realService = realService;
        this.scopeGuard = scopeGuard;
        this.usuarioRepository = usuarioRepository;
        this.estudianteRepository = estudianteRepository;
        this.programaRepository = programaRepository;
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
    public Estudiante registrar(EstudianteDto dto) {
        Usuario usuario = obtenerUsuarioActual();
        if (dto != null) {
            Programa programa = programaRepository.findById(dto.getProgramaId()).orElse(null);
            scopeGuard.verificarScope(usuario, programa, "REGISTRAR");
        }
        return realService.registrar(dto);
    }

    @Override
    public Estudiante marcarApto(Long id) {
        Usuario usuario = obtenerUsuarioActual();
        realService.obtenerPorId(id).ifPresent(estudiante -> 
            scopeGuard.verificarScope(usuario, estudiante, "EDITAR")
        );
        return realService.marcarApto(id);
    }

    @Override
    public Estudiante marcarNoApto(Long id, String motivo) {
        Usuario usuario = obtenerUsuarioActual();
        realService.obtenerPorId(id).ifPresent(estudiante -> 
            scopeGuard.verificarScope(usuario, estudiante, "EDITAR")
        );
        return realService.marcarNoApto(id, motivo);
    }

    @Override
    public Optional<Estudiante> obtenerPorId(Long id) {
        Usuario usuario = obtenerUsuarioActual();
        Optional<Estudiante> estudiante = realService.obtenerPorId(id);
        estudiante.ifPresent(est -> scopeGuard.verificarScope(usuario, est, "LEER"));
        return estudiante;
    }

    @Override
    public Optional<Estudiante> obtenerPorIdentificacion(String identificacion) {
        Usuario usuario = obtenerUsuarioActual();
        Optional<Estudiante> estudiante = realService.obtenerPorIdentificacion(identificacion);
        estudiante.ifPresent(est -> scopeGuard.verificarScope(usuario, est, "LEER"));
        return estudiante;
    }

    @Override
    public Page<Estudiante> listar(String programa, String facultad, EstadoAptitud aptitud, String estadoPractica, String busqueda, Pageable pageable) {
        Usuario usuario = obtenerUsuarioActual();
        if (usuario != null && usuario.getScope() == Scope.PROGRAMA) {
            // Si el scope es PROGRAMA, verificar correspondencia
            Estudiante estudianteAsociado = estudianteRepository.findByCorreo(usuario.getCorreo()).orElse(null);
            if (estudianteAsociado == null || estudianteAsociado.getPrograma() == null 
                    || programa == null || !estudianteAsociado.getPrograma().getNombre().equalsIgnoreCase(programa)) {
                throw new AccesoNoAutorizadoException("Acceso denegado: recurso fuera del scope");
            }
        }
        return realService.listar(programa, facultad, aptitud, estadoPractica, busqueda, pageable);
    }

    @Override
    public void importar(List<Estudiante> estudiantes) {
        Usuario usuario = obtenerUsuarioActual();
        if (estudiantes != null) {
            for (Estudiante est : estudiantes) {
                if (est.getPrograma() != null) {
                    scopeGuard.verificarScope(usuario, est.getPrograma(), "IMPORTAR");
                }
            }
        }
        realService.importar(estudiantes);
    }

    @Override
    public Estudiante guardar(Estudiante estudiante) {
        Usuario usuario = obtenerUsuarioActual();
        scopeGuard.verificarScope(usuario, estudiante, "GUARDAR");
        return realService.guardar(estudiante);
    }
}
