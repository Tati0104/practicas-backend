package com.avh.practicas.dashboard.service;

import com.avh.practicas.dashboard.dto.AlertaDto;
import com.avh.practicas.dashboard.dto.DashboardDto;
import com.avh.practicas.dashboard.dto.FiltrosResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    // Simulación: luego conectar con repositorios reales
    public DashboardDto getDashboard() {
        return DashboardDto.builder()
                .totalEstudiantes(120L)
                .totalEmpresas(15L)
                .totalVacantes(30L)
                .build();
    }

    public List<AlertaDto> getAlertas() {
        return List.of(
                AlertaDto.builder().id(1L).mensaje("Nueva vacante creada").leida(false).build(),
                AlertaDto.builder().id(2L).mensaje("Estudiante asignado").leida(true).build()
        );
    }

    public void marcarAlertaLeida(Long id) {
        // Aquí se implementa la lógica para actualizar la alerta como leída
    }

    public FiltrosResponse getFiltrosDisponibles() {
        return FiltrosResponse.builder()
                .modalidades(List.of("Presencial", "Remoto"))
                .areas(List.of("Desarrollo", "Calidad", "Administración"))
                .build();
    }
}