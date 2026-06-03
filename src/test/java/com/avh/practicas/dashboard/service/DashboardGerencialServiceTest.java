package com.avh.practicas.dashboard.service;

import com.avh.practicas.configuracion.repository.FacultadRepository;
import com.avh.practicas.dashboard.repository.DashboardGerencialRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardGerencialServiceTest {

    @Mock
    private DashboardGerencialRepository repository;
    @Mock
    private FacultadRepository facultadRepository;

    @InjectMocks
    private DashboardGerencialService service;

    @Test
    void obtenerIndicadores_agregaMetricas() {
        when(facultadRepository.existsById(1L)).thenReturn(true);
        when(repository.totalPracticantesActivosPorFacultad(any(), eq(1L)))
                .thenReturn(Map.of("Ingeniería", 12));
        when(repository.tasaAprobacionGlobal(any(), eq(1L))).thenReturn(0.75);
        when(repository.tasaAprobacionPorPrograma(any(), eq(1L)))
                .thenReturn(Map.of("Sistemas", 0.8));
        when(repository.contarEmpresasActivas(any(), eq(1L))).thenReturn(5);
        when(repository.tiempoPromedioGestionDias(any(), eq(1L))).thenReturn(14.5);
        when(repository.contarPracticasCerradasEnPeriodo(any(), eq(1L))).thenReturn(3);

        var dto = service.obtenerIndicadores("2025-1", 1L);

        assertEquals(12, dto.totalPracticantesActivosPorFacultad().get("Ingeniería"));
        assertEquals(0.75, dto.tasaAprobacionGlobal());
        assertEquals(5, dto.empresasActivas());
        assertEquals(14.5, dto.tiempoPromedioGestion());
        assertEquals(3, dto.practicasCerradasEnPeriodo());
        assertEquals("2025-1", dto.periodo());
        assertEquals(1L, dto.facultadId());
    }
}
