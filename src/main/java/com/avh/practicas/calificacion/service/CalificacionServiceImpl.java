package com.avh.practicas.calificacion.service;

import com.avh.practicas.calificacion.dto.NotaCorteDto;
import com.avh.practicas.calificacion.dto.NotaFinalRequest;
import com.avh.practicas.calificacion.dto.NotaRequest;
import com.avh.practicas.calificacion.dto.ResumenCalificacionesResponse;
import com.avh.practicas.calificacion.entity.NotaDocente;
import com.avh.practicas.calificacion.entity.NotaFinal;
import com.avh.practicas.calificacion.entity.NotaTutor;
import com.avh.practicas.calificacion.repository.NotaDocenteRepository;
import com.avh.practicas.calificacion.repository.NotaFinalRepository;
import com.avh.practicas.calificacion.repository.NotaTutorRepository;
import com.avh.practicas.estudiante.entity.DocenteAsesor;
import com.avh.practicas.estudiante.entity.InstanciaPractica;
import com.avh.practicas.estudiante.repository.DocenteAsesorRepository;
import com.avh.practicas.estudiante.repository.InstanciaPracticaRepository;
import com.avh.practicas.empresa.entity.TutorEmpresarial;
import com.avh.practicas.empresa.repository.TutorEmpresarialRepository;
import com.avh.practicas.shared.pattern.singleton.GestorConfiguracion;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Implementación del servicio de calificaciones de prácticas.
 * Utiliza el patrón Singleton de GestorConfiguracion para validación de notas máximas.
 */
@Service("calificacionServiceImpl")
@RequiredArgsConstructor
@Transactional
public class CalificacionServiceImpl implements CalificacionService {

    private final InstanciaPracticaRepository practicaRepository;
    private final DocenteAsesorRepository docenteRepository;
    private final TutorEmpresarialRepository tutorRepository;
    private final NotaDocenteRepository notaDocenteRepository;
    private final NotaTutorRepository notaTutorRepository;
    private final NotaFinalRepository notaFinalRepository;
    private final JdbcTemplate jdbcTemplate;

    /**
     * Valida si la práctica está marcada como inmutable (cerrada).
     */
    private void validarPracticaActiva(InstanciaPractica practica) {
        if (Boolean.TRUE.equals(practica.getInmutable())) {
            throw new IllegalStateException("La práctica ya se encuentra cerrada y no permite modificaciones.");
        }
    }

    /**
     * Valida que la nota se encuentre dentro del rango [0, maxNota] del GestorConfiguracion.
     */
    private void validarRangoNota(Double nota) {
        Double maxNota = GestorConfiguracion.getInstancia().getMaxNota();
        if (nota < 0.0 || nota > maxNota) {
            throw new IllegalArgumentException("La calificación debe estar en el rango [0.0, " + maxNota + "].");
        }
    }

    @Override
    public NotaDocente registrarNotaDocente(Long practicaId, Long docenteId, Integer corte, NotaRequest request) {
        InstanciaPractica practica = practicaRepository.findById(practicaId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la práctica con ID: " + practicaId));

        validarPracticaActiva(practica);
        validarRangoNota(request.nota());

        // Validar docente asignado
        if (practica.getDocenteAsesorId() == null || !practica.getDocenteAsesorId().equals(docenteId)) {
            throw new IllegalArgumentException("El docente no está asignado a esta práctica.");
        }

        // Validar rango del corte
        if (corte <= 0 || corte > practica.getNumCortes()) {
            throw new IllegalArgumentException("El corte especificado es inválido para esta práctica.");
        }

        DocenteAsesor docente = docenteRepository.findById(docenteId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el docente con ID: " + docenteId));

        // Buscar nota existente para actualizar, o crear una nueva
        NotaDocente notaDocente = notaDocenteRepository.findByInstanciaPracticaIdAndCorte(practicaId, corte)
                .orElseGet(() -> NotaDocente.builder()
                        .instanciaPractica(practica)
                        .corte(corte)
                        .build());

        notaDocente.setNota(request.nota());
        notaDocente.setObservaciones(request.observaciones());

        return notaDocenteRepository.save(notaDocente);
    }

    @Override
    public NotaTutor registrarNotaTutor(Long practicaId, Long tutorId, Integer corte, NotaRequest request) {
        InstanciaPractica practica = practicaRepository.findById(practicaId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la práctica con ID: " + practicaId));

        validarPracticaActiva(practica);
        validarRangoNota(request.nota());

        // Validar tutor asignado
        if (practica.getTutorId() == null || !practica.getTutorId().equals(tutorId)) {
            throw new IllegalArgumentException("El tutor no está asignado a esta práctica.");
        }

        // Validar rango del corte
        if (corte <= 0 || corte > practica.getNumCortes()) {
            throw new IllegalArgumentException("El corte especificado es inválido para esta práctica.");
        }

        TutorEmpresarial tutor = tutorRepository.findById(tutorId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el tutor con ID: " + tutorId));

        // Buscar nota de tutor existente o crear una nueva
        NotaTutor notaTutor = notaTutorRepository.findByInstanciaPracticaIdAndCorte(practicaId, corte)
                .orElseGet(() -> NotaTutor.builder()
                        .instanciaPractica(practica)
                        .corte(corte)
                        .build());

        notaTutor.setNota(request.nota());
        notaTutor.setObservaciones(request.observaciones());

        return notaTutorRepository.save(notaTutor);
    }

    @Override
    public NotaFinal registrarNotaFinal(Long practicaId, Long docenteAsesorId, NotaFinalRequest request) {
        InstanciaPractica practica = practicaRepository.findById(practicaId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la práctica con ID: " + practicaId));

        validarPracticaActiva(practica);
        validarRangoNota(request.notaFinal());

        if (practica.getDocenteAsesorId() == null || !practica.getDocenteAsesorId().equals(docenteAsesorId)) {
            throw new IllegalArgumentException("El docente no está asignado a esta práctica.");
        }

        if (notaFinalRepository.findByInstanciaPracticaId(practicaId).isPresent()) {
            throw new IllegalStateException("La nota final ya fue registrada y no puede modificarse.");
        }

        // Consultar la nota mínima de aprobación del programa
        Double notaMinima = 3.0; // Default
        try {
            Long programaId = practica.getExpediente().getEstudiante().getPrograma().getId();
            Double programaMin = jdbcTemplate.queryForObject(
                    "SELECT nota_minima_aprobacion FROM config_programas WHERE programa_id = ?",
                    Double.class,
                    programaId
            );
            if (programaMin != null) {
                notaMinima = programaMin;
            }
        } catch (Exception e) {
            // Usar nota mínima del GestorConfiguracion si no está parametrizada en BD
            notaMinima = GestorConfiguracion.getInstancia().getPromedioMinimo();
        }

        boolean aprobada = request.notaFinal() >= notaMinima;

        NotaFinal notaFinal = NotaFinal.builder()
                .instanciaPractica(practica)
                .notaFinal(request.notaFinal())
                .aprobada(aprobada)
                .build();

        return notaFinalRepository.save(notaFinal);
    }

    @Override
    @Transactional(readOnly = true)
    public NotaFinal leerNotaFinal(Long practicaId) {
        return notaFinalRepository.findByInstanciaPracticaId(practicaId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe nota final registrada para la práctica: " + practicaId));
    }

    @Override
    @Transactional(readOnly = true)
    public ResumenCalificacionesResponse obtenerResumen(Long practicaId) {
        InstanciaPractica practica = practicaRepository.findById(practicaId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la práctica con ID: " + practicaId));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ESTUDIANTE"))) {
            String correo = auth.getName();
            if (practica.getExpediente() == null || 
                practica.getExpediente().getEstudiante() == null || 
                !correo.equals(practica.getExpediente().getEstudiante().getCorreo())) {
                throw new com.avh.practicas.shared.exception.NegocioException("No tiene permisos para ver las calificaciones de otro estudiante.");
            }
        }

        List<NotaDocente> docenteNotes = notaDocenteRepository.findByInstanciaPracticaId(practicaId);
        List<NotaTutor> tutorNotes = notaTutorRepository.findByInstanciaPracticaId(practicaId);
        Optional<NotaFinal> finalNote = notaFinalRepository.findByInstanciaPracticaId(practicaId);

        List<NotaCorteDto> notasCortes = new ArrayList<>();
        double sumaNotas = 0.0;
        int cantidadNotas = 0;

        for (int i = 1; i <= practica.getNumCortes(); i++) {
            final int corteActual = i;
            
            Optional<NotaDocente> docNote = docenteNotes.stream()
                    .filter(n -> n.getCorte().equals(corteActual))
                    .findFirst();
                    
            Optional<NotaTutor> tutNote = tutorNotes.stream()
                    .filter(n -> n.getCorte().equals(corteActual))
                    .findFirst();

            Double notaDoc = docNote.map(NotaDocente::getNota).orElse(null);
            String obsDoc = docNote.map(NotaDocente::getObservaciones).orElse(null);
            Double notaTut = tutNote.map(NotaTutor::getNota).orElse(null);
            String obsTut = tutNote.map(NotaTutor::getObservaciones).orElse(null);

            if (notaDoc != null) {
                sumaNotas += notaDoc;
                cantidadNotas++;
            }
            if (notaTut != null) {
                sumaNotas += notaTut;
                cantidadNotas++;
            }

            notasCortes.add(new NotaCorteDto(corteActual, notaDoc, obsDoc, notaTut, obsTut));
        }

        Double promedioEstimado = cantidadNotas > 0 ? (sumaNotas / cantidadNotas) : 0.0;

        Double notaFin = finalNote.map(NotaFinal::getNotaFinal).orElse(null);
        Boolean aprobada = finalNote.map(NotaFinal::getAprobada).orElse(null);

        return new ResumenCalificacionesResponse(
                practicaId,
                notasCortes,
                notaFin,
                aprobada,
                promedioEstimado
        );
    }
}
