package com.avh.practicas.vinculacion.service;

import com.avh.practicas.empresa.entity.Empresa;
import com.avh.practicas.empresa.entity.TutorEmpresarial;
import com.avh.practicas.empresa.repository.EmpresaRepository;
import com.avh.practicas.empresa.repository.TutorEmpresarialRepository;
import com.avh.practicas.estudiante.entity.DocenteAsesor;
import com.avh.practicas.estudiante.entity.EstadoPractica;
import com.avh.practicas.estudiante.entity.Estudiante;
import com.avh.practicas.estudiante.entity.InstanciaPractica;
import com.avh.practicas.estudiante.repository.DocenteAsesorRepository;
import com.avh.practicas.estudiante.repository.InstanciaPracticaRepository;
import com.avh.practicas.seguimiento.entity.BitacoraEstudiante;
import com.avh.practicas.seguimiento.repository.BitacoraEstudianteRepository;
import com.avh.practicas.shared.exception.NegocioException;
import com.avh.practicas.shared.exception.RecursoNoEncontradoException;
import com.avh.practicas.vinculacion.dto.ContextoVinculacion;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ServicioEstudiantesImpl implements ServicioEstudiantes {

    private final InstanciaPracticaRepository instanciaPracticaRepository;
    private final EmpresaRepository empresaRepository;
    private final TutorEmpresarialRepository tutorEmpresarialRepository;
    private final DocenteAsesorRepository docenteAsesorRepository;
    private final BitacoraEstudianteRepository bitacoraEstudianteRepository;

    @Override
    @Transactional(readOnly = true)
    public ContextoVinculacion cargarContexto(Long practicaId) {
        InstanciaPractica practica = obtenerPracticaConRelaciones(practicaId);
        Estudiante estudiante = practica.getExpediente().getEstudiante();

        Empresa empresa = empresaRepository.findById(practica.getEmpresaId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró la empresa asociada a la práctica: " + practica.getEmpresaId()));

        TutorEmpresarial tutor = tutorEmpresarialRepository.findById(practica.getTutorId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró el tutor empresarial asociado a la práctica: " + practica.getTutorId()));

        DocenteAsesor docenteAsesor = null;
        if (practica.getDocenteAsesorId() != null) {
            docenteAsesor = docenteAsesorRepository.findById(practica.getDocenteAsesorId())
                    .orElseThrow(() -> new RecursoNoEncontradoException(
                            "No se encontró el docente asesor con id: " + practica.getDocenteAsesorId()));
        }

        return ContextoVinculacion.builder()
                .practica(practica)
                .estudiante(estudiante)
                .empresa(empresa)
                .tutor(tutor)
                .docenteAsesor(docenteAsesor)
                .build();
    }

    @Override
    @Transactional
    public InstanciaPractica activarPractica(Long practicaId) {
        InstanciaPractica practica = obtenerPracticaConRelaciones(practicaId);

        if (practica.getEstado() != EstadoPractica.ASIGNADA_PENDIENTE_INICIO) {
            throw new NegocioException(
                    "Solo se puede confirmar vinculación en prácticas con estado ASIGNADA_PENDIENTE_INICIO. Estado actual: "
                            + practica.getEstado());
        }

        if (practica.getEmpresaId() == null || practica.getTutorId() == null) {
            throw new NegocioException("La práctica debe tener empresa y tutor empresarial asignados antes de confirmar la vinculación.");
        }

        // EN_PRACTICA (requisito PE-34) ↔ EN_CURSO en el modelo persistido
        practica.setEstado(EstadoPractica.EN_CURSO);
        practica.setFechaInicio(LocalDate.now());

        return instanciaPracticaRepository.save(practica);
    }

    @Override
    @Transactional
    public void activarTableroSeguimiento(Long practicaId) {
        obtenerPracticaConRelaciones(practicaId);

        BitacoraEstudiante entrada = BitacoraEstudiante.builder()
                .instanciaPracticaId(practicaId)
                .descripcion("Tablero de seguimiento activado tras confirmación de vinculación.")
                .fecha(LocalDateTime.now())
                .build();

        bitacoraEstudianteRepository.save(entrada);
    }

    private InstanciaPractica obtenerPracticaConRelaciones(Long practicaId) {
        return instanciaPracticaRepository.findByIdWithExpedienteAndEstudiante(practicaId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró la instancia de práctica con id: " + practicaId));
    }
}
