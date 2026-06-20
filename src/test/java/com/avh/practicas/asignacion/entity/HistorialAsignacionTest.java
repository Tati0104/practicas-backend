package com.avh.practicas.asignacion.entity;

import com.avh.practicas.asignacion.dto.HistorialAsignacionResponse;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class HistorialAsignacionTest {

    @Test
    void builder_asignaCamposBasicos() {
        HistorialAsignacion historial = HistorialAsignacion.builder()
                .asignacionId(1L)
                .estadoAnterior(EstadoAsignacion.ASIGNADA)
                .estadoNuevo(EstadoAsignacion.EN_PROCESO_VINCULACION)
                .responsableId(9L)
                .motivo("Inicio")
                .build();

        assertEquals(1L, historial.getAsignacionId());
        assertEquals(EstadoAsignacion.EN_PROCESO_VINCULACION, historial.getEstadoNuevo());
    }

    @Test
    void prePersist_sinFecha_asignaFechaActual() {
        HistorialAsignacion historial = HistorialAsignacion.builder()
                .asignacionId(1L)
                .estadoNuevo(EstadoAsignacion.ASIGNADA)
                .build();

        historial.prePersist();

        assertNotNull(historial.getFecha());
    }

    @Test
    void prePersist_conFechaExistente_noLaReemplaza() {
        LocalDateTime fecha = LocalDateTime.of(2026, 1, 15, 8, 30);
        HistorialAsignacion historial = HistorialAsignacion.builder()
                .asignacionId(1L)
                .estadoNuevo(EstadoAsignacion.ASIGNADA)
                .fecha(fecha)
                .build();

        historial.prePersist();

        assertSame(fecha, historial.getFecha());
    }

    @Test
    void response_desdeEntidad_mapeaTodosLosCampos() {
        LocalDateTime fecha = LocalDateTime.of(2026, 2, 20, 10, 0);
        HistorialAsignacion historial = new HistorialAsignacion(
                7L,
                1L,
                EstadoAsignacion.ASIGNADA,
                EstadoAsignacion.CANCELADA,
                11L,
                "Cupo liberado",
                fecha
        );

        HistorialAsignacionResponse response = HistorialAsignacionResponse.desdeEntidad(historial);

        assertAll(
                () -> assertEquals(7L, response.id()),
                () -> assertEquals(1L, response.asignacionId()),
                () -> assertEquals(EstadoAsignacion.ASIGNADA, response.estadoAnterior()),
                () -> assertEquals(EstadoAsignacion.CANCELADA, response.estadoNuevo()),
                () -> assertEquals(11L, response.responsableId()),
                () -> assertEquals("Cupo liberado", response.motivo()),
                () -> assertEquals(fecha, response.fecha())
        );
    }

    @Test
    void setters_actualizanCamposEditables() {
        HistorialAsignacion historial = new HistorialAsignacion();

        historial.setAsignacionId(4L);
        historial.setResponsableId(8L);
        historial.setMotivo("Ajuste");

        assertAll(
                () -> assertEquals(4L, historial.getAsignacionId()),
                () -> assertEquals(8L, historial.getResponsableId()),
                () -> assertEquals("Ajuste", historial.getMotivo())
        );
    }
}
