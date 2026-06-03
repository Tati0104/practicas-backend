package com.avh.practicas.vinculacion.dto;

import com.avh.practicas.empresa.entity.Empresa;
import com.avh.practicas.empresa.entity.TutorEmpresarial;
import com.avh.practicas.estudiante.entity.DocenteAsesor;
import com.avh.practicas.estudiante.entity.Estudiante;
import com.avh.practicas.estudiante.entity.InstanciaPractica;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ContextoVinculacion {

    private final InstanciaPractica practica;
    private final Estudiante estudiante;
    private final Empresa empresa;
    private final TutorEmpresarial tutor;
    private final DocenteAsesor docenteAsesor;
}
