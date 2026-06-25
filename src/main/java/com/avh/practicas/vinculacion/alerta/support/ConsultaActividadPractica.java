package com.avh.practicas.vinculacion.alerta.support;

import com.avh.practicas.seguimiento.repository.AvanceTutorRepository;
import com.avh.practicas.seguimiento.repository.BitacoraEstudianteRepository;
import com.avh.practicas.seguimiento.repository.ObservacionDocenteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class ConsultaActividadPractica {

    private final ObservacionDocenteRepository observacionDocenteRepository;
    private final AvanceTutorRepository avanceTutorRepository;
    private final BitacoraEstudianteRepository bitacoraEstudianteRepository;

    /**
     * Hay actividad si existe al menos un registro reciente en observaciones, avances o bitácora.
     */
    public boolean tieneActividadReciente(Long practicaId, LocalDateTime desde) {
        return observacionDocenteRepository.existsByPracticaIdAndFechaDesde(practicaId, desde)
                || avanceTutorRepository.existsByPracticaIdAndFechaDesde(practicaId, desde)
                || bitacoraEstudianteRepository.existsByPracticaIdAndFechaDesde(practicaId, desde);
    }
}
