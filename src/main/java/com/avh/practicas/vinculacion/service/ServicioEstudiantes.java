package com.avh.practicas.vinculacion.service;

import com.avh.practicas.estudiante.entity.InstanciaPractica;
import com.avh.practicas.vinculacion.dto.ContextoVinculacion;

public interface ServicioEstudiantes {

    ContextoVinculacion cargarContexto(Long practicaId);

    InstanciaPractica activarPractica(Long practicaId);

    void activarTableroSeguimiento(Long practicaId);
}
