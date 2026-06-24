package com.avh.practicas.estudiante.controller;

import com.avh.practicas.auth.entity.Usuario;
import com.avh.practicas.auth.repository.AuthUsuarioRepository;
import com.avh.practicas.estudiante.entity.Expediente;
import com.avh.practicas.estudiante.entity.Estudiante;
import com.avh.practicas.estudiante.repository.EstudianteRepository;
import com.avh.practicas.estudiante.repository.ExpedienteRepository;
import com.avh.practicas.shared.exception.RecursoNoEncontradoException;
import com.avh.practicas.shared.pattern.proxy.ScopeGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/expedientes")
@RequiredArgsConstructor
public class ExpedienteController {

    private final ExpedienteRepository expedienteRepository;
    private final EstudianteRepository estudianteRepository;
    private final AuthUsuarioRepository usuarioRepository;
    private final com.avh.practicas.shared.security.ScopeGuard scopeGuard;

    @GetMapping("/{estudianteId}")
    @ScopeGuard("EXPEDIENTE_VER")
    public ResponseEntity<Expediente> obtenerPorEstudianteId(@PathVariable Long estudianteId) {
        Expediente expediente = expedienteRepository.findByEstudianteIdWithInstancias(estudianteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el expediente para el estudiante con id: " + estudianteId));

        Estudiante estudiante = estudianteRepository.findById(estudianteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el estudiante con id: " + estudianteId));

        Usuario usuario = obtenerUsuarioActual();
        scopeGuard.verificarScope(usuario, estudiante, "LEER");

        expediente.getInstanciasPractica().sort(
                java.util.Comparator.comparing(
                        com.avh.practicas.estudiante.entity.InstanciaPractica::getNumeroPractica,
                        java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder())));

        return ResponseEntity.ok(expediente);
    }

    private Usuario obtenerUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        String correo = (String) auth.getPrincipal();
        return usuarioRepository.findByCorreoIgnoreCase(correo).orElse(null);
    }
}
