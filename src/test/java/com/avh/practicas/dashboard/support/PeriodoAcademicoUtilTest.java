package com.avh.practicas.dashboard.support;

import com.avh.practicas.shared.exception.BadRequestException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class PeriodoAcademicoUtilTest {

    @Test
    void parsear_periodoValidoSemestre1() {
        var periodo = PeriodoAcademicoUtil.parsear("2025-1").orElseThrow();
        assertEquals(2025, periodo.anio());
        assertEquals(1, periodo.semestre());
        assertEquals(LocalDate.of(2025, 1, 1), periodo.fechaInicio());
        assertEquals(LocalDate.of(2025, 6, 30), periodo.fechaFin());
    }

    @Test
    void parsear_periodoInvalido_lanzaExcepcion() {
        assertThrows(BadRequestException.class, () -> PeriodoAcademicoUtil.parsear("2025-3"));
    }

    @Test
    void parsear_vacio_retornaEmpty() {
        assertTrue(PeriodoAcademicoUtil.parsear(null).isEmpty());
        assertTrue(PeriodoAcademicoUtil.parsear("  ").isEmpty());
    }
}
