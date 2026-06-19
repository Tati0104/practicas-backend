package com.avh.practicas.usuario.service;

import com.avh.practicas.auth.repository.AuthUsuarioRepository;
import com.avh.practicas.empresa.repository.TutorEmpresarialRepository;
import com.avh.practicas.estudiante.repository.DocenteAsesorRepository;
import com.avh.practicas.estudiante.repository.EstudianteRepository;
import com.avh.practicas.shared.exception.NegocioException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CorreoPersonaService {

    private final AuthUsuarioRepository usuarioRepository;
    private final DocenteAsesorRepository docenteAsesorRepository;
    private final TutorEmpresarialRepository tutorEmpresarialRepository;
    private final EstudianteRepository estudianteRepository;

    public String normalizar(String correo) {
        if (correo == null || correo.isBlank()) {
            throw new NegocioException("El correo es obligatorio.");
        }
        return correo.trim().toLowerCase();
    }

    public void validarCorreoDisponible(String correo, Exclusiones exclusiones) {
        String normalizado = normalizar(correo);
        Exclusiones ex = exclusiones != null ? exclusiones : Exclusiones.ninguna();

        if (ex.usuarioId() == null && usuarioRepository.existsByCorreoIgnoreCase(normalizado)) {
            throw new NegocioException("Ya existe un usuario registrado con el correo: " + normalizado);
        }
        if (ex.usuarioId() != null && usuarioRepository.existsByCorreoIgnoreCaseAndIdNot(normalizado, ex.usuarioId())) {
            throw new NegocioException("Ya existe otro usuario registrado con el correo: " + normalizado);
        }

        docenteAsesorRepository.findByCorreoIgnoreCase(normalizado)
                .filter(docente -> ex.docenteId() == null || !docente.getId().equals(ex.docenteId()))
                .ifPresent(docente -> {
                    throw new NegocioException("Ya existe un docente asesor registrado con el correo: " + normalizado);
                });

        tutorEmpresarialRepository.findByCorreoIgnoreCase(normalizado)
                .filter(tutor -> ex.tutorId() == null || !tutor.getId().equals(ex.tutorId()))
                .ifPresent(tutor -> {
                    throw new NegocioException("Ya existe un tutor empresarial registrado con el correo: " + normalizado);
                });

        estudianteRepository.findByCorreoIgnoreCase(normalizado)
                .filter(est -> ex.estudianteId() == null || !est.getId().equals(ex.estudianteId()))
                .ifPresent(est -> {
                    throw new NegocioException("Ya existe un estudiante registrado con el correo: " + normalizado);
                });
    }

    public record Exclusiones(Long usuarioId, Long docenteId, Long tutorId, Long estudianteId) {
        public static Exclusiones ninguna() {
            return new Exclusiones(null, null, null, null);
        }
    }
}
