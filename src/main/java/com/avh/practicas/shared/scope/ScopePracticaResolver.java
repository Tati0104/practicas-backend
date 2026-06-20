package com.avh.practicas.shared.scope;

import com.avh.practicas.auth.entity.Usuario;
import com.avh.practicas.auth.repository.AuthUsuarioRepository;
import com.avh.practicas.configuracion.entity.Programa;
import com.avh.practicas.configuracion.repository.ProgramaRepository;
import com.avh.practicas.empresa.repository.EmpresaRepository;
import com.avh.practicas.empresa.repository.TutorEmpresarialRepository;
import com.avh.practicas.estudiante.repository.DocenteAsesorRepository;
import com.avh.practicas.estudiante.repository.EstudianteRepository;
import com.avh.practicas.shared.enums.Rol;
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
    private final DocenteAsesorRepository docenteAsesorRepository;
    private final EmpresaRepository empresaRepository;
    private final TutorEmpresarialRepository tutorEmpresarialRepository;

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

        if (usuario.getRol() == Rol.ADMIN || usuario.getRol() == Rol.COORD_PRACTICA) {
            return ScopePracticas.todas();
        }

        if (usuario.getRol() == Rol.DOCENTE_ASESOR) {
            return docenteAsesorRepository.findByUsuario_Id(usuario.getId())
                    .or(() -> docenteAsesorRepository.findByCorreoIgnoreCase(usuario.getCorreo()))
                    .map(d -> ScopePracticas.deDocente(d.getId()))
                    .orElse(ScopePracticas.ninguna());
        }

        if (usuario.getRol() == Rol.EMPRESA) {
            return empresaRepository.findByUsuarioId(usuario.getId())
                    .map(e -> ScopePracticas.deEmpresa(e.getId()))
                    .orElse(ScopePracticas.ninguna());
        }

        if (usuario.getRol() == Rol.TUTOR_EMPRESARIAL) {
            return tutorEmpresarialRepository.findByUsuarioId(usuario.getId())
                    .or(() -> tutorEmpresarialRepository.findByCorreoIgnoreCase(usuario.getCorreo()))
                    .map(t -> ScopePracticas.deTutor(t.getId()))
                    .orElse(ScopePracticas.ninguna());
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
