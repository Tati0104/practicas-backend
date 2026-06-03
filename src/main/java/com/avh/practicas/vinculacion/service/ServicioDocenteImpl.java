package com.avh.practicas.vinculacion.service;

import com.avh.practicas.estudiante.entity.DocenteAsesor;
import com.avh.practicas.estudiante.entity.Estudiante;
import com.avh.practicas.estudiante.entity.InstanciaPractica;
import com.avh.practicas.estudiante.repository.DocenteAsesorRepository;
import com.avh.practicas.shared.exception.NegocioException;
import com.avh.practicas.shared.exception.RecursoNoEncontradoException;
import com.avh.practicas.vinculacion.repository.PracticaVinculacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ServicioDocenteImpl implements ServicioDocente {

    private final PracticaVinculacionRepository practicaRepository;
    private final DocenteAsesorRepository docenteAsesorRepository;

    @Override
    @Transactional
    public void asignarDocenteAsesor(Long practicaId) {
        InstanciaPractica practica = practicaRepository.findByIdConExpediente(practicaId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró la instancia de práctica con id: " + practicaId));

        if (practica.getDocenteAsesorId() != null) {
            DocenteAsesor asignado = docenteAsesorRepository.findById(practica.getDocenteAsesorId())
                    .orElseThrow(() -> new RecursoNoEncontradoException(
                            "Docente asesor no encontrado: " + practica.getDocenteAsesorId()));
            if (!Boolean.TRUE.equals(asignado.getActivo())) {
                throw new NegocioException("El docente asesor asignado no está activo.");
            }
            return;
        }

        Estudiante estudiante = practica.getExpediente().getEstudiante();
        Long programaId = estudiante.getPrograma().getId();

        List<DocenteAsesor> candidatos = docenteAsesorRepository.findByProgramaIdAndActivoTrue(programaId);
        if (candidatos.isEmpty()) {
            throw new NegocioException("No hay docentes asesores activos para el programa " + programaId);
        }

        practica.setDocenteAsesorId(candidatos.get(0).getId());
        practicaRepository.save(practica);
    }
}
