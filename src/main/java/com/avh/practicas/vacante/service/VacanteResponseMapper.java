package com.avh.practicas.vacante.service;

import com.avh.practicas.configuracion.repository.ProgramaRepository;
import com.avh.practicas.configuracion.repository.CatalogoPracticaRepository;
import com.avh.practicas.empresa.repository.EmpresaRepository;
import com.avh.practicas.vacante.dto.VacanteResponse;
import com.avh.practicas.vacante.entity.Vacante;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VacanteResponseMapper {

    private final EmpresaRepository empresaRepository;
    private final ProgramaRepository programaRepository;
    private final CatalogoPracticaRepository catalogoPracticaRepository;

    public VacanteResponse toResponse(Vacante vacante) {
        String empresaNombre = vacante.getEmpresaId() == null
                ? null
                : empresaRepository.findById(vacante.getEmpresaId())
                        .map(e -> e.getRazonSocial())
                        .orElse(null);

        String programaNombre = vacante.getProgramaId() == null
                ? null
                : programaRepository.findById(vacante.getProgramaId())
                        .map(p -> p.getNombre())
                        .orElse(null);

        VacanteResponse response = VacanteResponse.desdeEntidad(vacante, empresaNombre, programaNombre);
        if (vacante.getCatalogoPracticaId() == null) {
            return response;
        }
        return catalogoPracticaRepository.findById(vacante.getCatalogoPracticaId())
                .map(c -> response.conCatalogoPractica(c.getNombre(), c.getNumeroPractica()))
                .orElse(response);
    }
}
