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
import com.avh.practicas.seguimiento.entity.BitacoraEstudiante;
import com.avh.practicas.seguimiento.repository.BitacoraEstudianteRepository;
import com.avh.practicas.shared.exception.NegocioException;
import com.avh.practicas.shared.exception.RecursoNoEncontradoException;
import com.avh.practicas.vinculacion.dto.ContextoVinculacion;
import com.avh.practicas.vinculacion.repository.PracticaVinculacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ServicioEstudiantesImpl implements ServicioEstudiantes {

    private final PracticaVinculacionRepository practicaRepository;
    private final EmpresaRepository empresaRepository;
    private final TutorEmpresarialRepository tutorRepository;
    private final DocenteAsesorRepository docenteAsesorRepository;
    private final BitacoraEstudianteRepository bitacoraEstudianteRepository;

    @Override
    @Transactional(readOnly = true)
    public ContextoVinculacion cargarContexto(Long practicaId) {
        InstanciaPractica practica = obtenerPractica(practicaId);
        Estudiante estudiante = practica.getExpediente().getEstudiante();

        if (practica.getEmpresaId() == null || practica.getTutorId() == null) {
            throw new NegocioException("La práctica no tiene empresa o tutor empresarial configurados.");
        }

        Empresa empresa = empresaRepository.findById(practica.getEmpresaId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Empresa no encontrada: " + practica.getEmpresaId()));

        TutorEmpresarial tutor = tutorRepository.findById(practica.getTutorId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Tutor no encontrado: " + practica.getTutorId()));

        DocenteAsesor docenteAsesor = null;
        if (practica.getDocenteAsesorId() != null) {
            docenteAsesor = docenteAsesorRepository.findById(practica.getDocenteAsesorId())
                    .orElseThrow(() -> new RecursoNoEncontradoException(
                            "Docente asesor no encontrado: " + practica.getDocenteAsesorId()));
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
    public InstanciaPractica activarPractica(Long practicaId, LocalDate fechaInicio, LocalDate fechaFin) {
        InstanciaPractica practica = obtenerPractica(practicaId);

        if (practica.getEstado() != EstadoPractica.ASIGNADA_PENDIENTE_INICIO) {
            throw new NegocioException(
                    "Solo se puede activar una práctica en estado ASIGNADA_PENDIENTE_INICIO. Estado actual: "
                            + practica.getEstado());
        }

        if (fechaFin.isBefore(fechaInicio)) {
            throw new NegocioException("La fecha de fin no puede ser anterior a la fecha de inicio.");
        }

        practica.setEstado(EstadoPractica.EN_CURSO);
        practica.setFechaInicio(fechaInicio);
        practica.setFechaFin(fechaFin);

        return practicaRepository.save(practica);
    }

    @Override
    @Transactional
    public void activarTableroSeguimiento(Long practicaId) {
        InstanciaPractica practica = obtenerPractica(practicaId);
        Estudiante estudiante = practica.getExpediente().getEstudiante();

        bitacoraEstudianteRepository.save(BitacoraEstudiante.builder()
                .instanciaPractica(practica)
                .estudiante(estudiante)
                .descripcion("Tablero de seguimiento activado tras confirmación de vinculación.")
                .fecha(LocalDateTime.now())
                .build());
    }

    private InstanciaPractica obtenerPractica(Long practicaId) {
        return practicaRepository.findByIdConExpediente(practicaId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró la instancia de práctica con id: " + practicaId));
    }
}
