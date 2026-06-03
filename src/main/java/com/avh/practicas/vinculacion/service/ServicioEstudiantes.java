package com.avh.practicas.vinculacion.service;

import com.avh.practicas.estudiante.entity.InstanciaPractica;
import com.avh.practicas.vinculacion.dto.ContextoVinculacion;

import java.time.LocalDate;

public interface ServicioEstudiantes {

    ContextoVinculacion cargarContexto(Long practicaId);

    InstanciaPractica activarPractica(Long practicaId, LocalDate fechaInicio, LocalDate fechaFin);

    void activarTableroSeguimiento(Long practicaId);
}
