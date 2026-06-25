package com.avh.practicas.asignacion.service;

import com.avh.practicas.asignacion.dto.AsignacionEstudianteResumen;
import com.avh.practicas.asignacion.dto.AsignacionResponse;
import com.avh.practicas.asignacion.dto.AsignacionVacanteResumen;
import com.avh.practicas.asignacion.entity.Asignacion;
import com.avh.practicas.empresa.repository.EmpresaRepository;
import com.avh.practicas.estudiante.repository.EstudianteRepository;
import com.avh.practicas.vacante.repository.VacanteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AsignacionResponseMapper {

    private final EstudianteRepository estudianteRepository;
    private final VacanteRepository vacanteRepository;
    private final EmpresaRepository empresaRepository;

    public AsignacionResponse toResponse(Asignacion asignacion) {
        AsignacionEstudianteResumen estudiante = estudianteRepository.findById(asignacion.getEstudianteId())
                .map(AsignacionEstudianteResumen::desdeEntidad)
                .orElse(null);

        AsignacionVacanteResumen vacante = vacanteRepository.findById(asignacion.getVacanteId())
                .map(v -> {
                    String empresaNombre = v.getEmpresaId() == null
                            ? null
                            : empresaRepository.findById(v.getEmpresaId())
                                    .map(e -> e.getRazonSocial())
                                    .orElse(null);
                    return AsignacionVacanteResumen.desdeEntidad(v, empresaNombre);
                })
                .orElse(null);

        return AsignacionResponse.desdeEntidad(asignacion, estudiante, vacante);
    }
}
