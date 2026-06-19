package com.avh.practicas.shared.scope;

import com.avh.practicas.auth.entity.Usuario;
import com.avh.practicas.auth.repository.AuthUsuarioRepository;
import com.avh.practicas.configuracion.entity.Programa;
import com.avh.practicas.configuracion.repository.ProgramaRepository;
import com.avh.practicas.estudiante.repository.EstudianteRepository;
import com.avh.practicas.shared.enums.Scope;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Resuelve qué prácticas puede ver el usuario autenticado, sin que ningún módulo
 * (Seguimiento, Evaluaciones, Cierre) tenga que reimplementar la regla de scope.
 * Reutiliza el mismo criterio que ProgramaServiceProxy.filtrarProgramasPorScope.
 */
@Service
@RequiredArgsConstructor
public class ScopePracticaResolver {

    private final AuthUsuarioRepository usuarioRepository;
    private final EstudianteRepository estudianteRepository;
    private final ProgramaRepository programaRepository;

    public ScopePracticas resolver() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return ScopePracticas.ninguna();
        }

        boolean esEstudiante = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ESTUDIANTE"));
        if (esEstudiante) {
            return estudianteRepository.findByCorreo(auth.getName())
                    .map(e -> ScopePracticas.deEstudiante(e.getId()))
                    .orElse(ScopePracticas.ninguna());
        }

        Usuario usuario = usuarioRepository.findByCorreo(auth.getName()).orElse(null);
        if (usuario == null) {
            return ScopePracticas.ninguna();
        }

        if (usuario.getScope() == Scope.FACULTAD) {
            if (usuario.getFacultad() == null) {
                return ScopePracticas.ninguna();
            }
            Set<Long> programaIds = programaRepository.findByFacultadId(usuario.getFacultad().getId())
                    .stream()
                    .map(Programa::getId)
                    .collect(Collectors.toSet());
            return ScopePracticas.porProgramas(programaIds);
        }

        if (usuario.getScope() == Scope.PROGRAMA) {
            // No aplica hoy a los roles que consultan estos tableros (DOCENTE_ASESOR/ESTUDIANTE
            // tienen su propio camino); por defecto no se le oculta nada a quien llegue por aquí.
            return ScopePracticas.todas();
        }

        // GLOBAL (ADMIN, DIRECCION, o cualquier coordinador con scope GLOBAL asignado manualmente).
        return ScopePracticas.todas();
    }
}