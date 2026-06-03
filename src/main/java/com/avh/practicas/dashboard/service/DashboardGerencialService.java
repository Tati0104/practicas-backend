package com.avh.practicas.dashboard.service;

import com.avh.practicas.configuracion.repository.FacultadRepository;
import com.avh.practicas.dashboard.dto.DashboardGerencialDto;
import com.avh.practicas.dashboard.repository.DashboardGerencialRepository;
import com.avh.practicas.dashboard.support.PeriodoAcademicoUtil;
import com.avh.practicas.shared.exception.RecursoNoEncontradoException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

/**
 * Servicio del dashboard gerencial para Dirección (PE-45).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardGerencialService {

    private final DashboardGerencialRepository repository;
    private final FacultadRepository facultadRepository;

    public DashboardGerencialDto obtenerIndicadores(String periodo, Long facultadId) {
        validarFacultad(facultadId);
        Optional<PeriodoAcademicoUtil.PeriodoAcademico> periodoFiltro = PeriodoAcademicoUtil.parsear(periodo);

        Map<String, Integer> practicantesPorFacultad =
                repository.totalPracticantesActivosPorFacultad(periodoFiltro, facultadId);
        double tasaGlobal = repository.tasaAprobacionGlobal(periodoFiltro, facultadId);
        Map<String, Double> tasaPorPrograma =
                repository.tasaAprobacionPorPrograma(periodoFiltro, facultadId);
        int empresasActivas = repository.contarEmpresasActivas(periodoFiltro, facultadId);
        double tiempoPromedioGestion = repository.tiempoPromedioGestionDias(periodoFiltro, facultadId);
        int practicasCerradas = repository.contarPracticasCerradasEnPeriodo(periodoFiltro, facultadId);

        return new DashboardGerencialDto(
                practicantesPorFacultad,
                redondearTasa(tasaGlobal),
                redondearTasasPorPrograma(tasaPorPrograma),
                empresasActivas,
                redondearDias(tiempoPromedioGestion),
                practicasCerradas,
                periodoFiltro.map(PeriodoAcademicoUtil.PeriodoAcademico::valor).orElse(null),
                facultadId
        );
    }

    private void validarFacultad(Long facultadId) {
        if (facultadId != null && !facultadRepository.existsById(facultadId)) {
            throw new RecursoNoEncontradoException("Facultad no encontrada: " + facultadId);
        }
    }

    private double redondearTasa(double valor) {
        return Math.round(valor * 10000.0) / 10000.0;
    }

    private double redondearDias(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }

    private Map<String, Double> redondearTasasPorPrograma(Map<String, Double> tasas) {
        Map<String, Double> redondeadas = new java.util.LinkedHashMap<>();
        tasas.forEach((programa, tasa) -> redondeadas.put(programa, redondearTasa(tasa)));
        return redondeadas;
    }
}
