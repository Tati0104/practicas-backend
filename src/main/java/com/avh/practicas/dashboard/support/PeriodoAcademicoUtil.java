package com.avh.practicas.dashboard.support;

import com.avh.practicas.shared.exception.BadRequestException;

import java.time.LocalDate;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Parsea el filtro {@code periodo} con formato {@code año-semestre} (ej. {@code 2025-1}).
 */
public final class PeriodoAcademicoUtil {

    private static final Pattern FORMATO = Pattern.compile("^(\\d{4})-(\\d)$");

    private PeriodoAcademicoUtil() {
    }

    public static Optional<PeriodoAcademico> parsear(String periodo) {
        if (periodo == null || periodo.isBlank()) {
            return Optional.empty();
        }
        var matcher = FORMATO.matcher(periodo.trim());
        if (!matcher.matches()) {
            throw new BadRequestException("Formato de periodo inválido. Use año-semestre, ej: 2025-1");
        }
        int anio = Integer.parseInt(matcher.group(1));
        int semestre = Integer.parseInt(matcher.group(2));
        if (semestre < 1 || semestre > 2) {
            throw new BadRequestException("El semestre debe ser 1 o 2");
        }
        return Optional.of(new PeriodoAcademico(anio, semestre, periodo.trim()));
    }

    public record PeriodoAcademico(int anio, int semestre, String valor) {
        public LocalDate fechaInicio() {
            return semestre == 1
                    ? LocalDate.of(anio, 1, 1)
                    : LocalDate.of(anio, 7, 1);
        }

        public LocalDate fechaFin() {
            return semestre == 1
                    ? LocalDate.of(anio, 6, 30)
                    : LocalDate.of(anio, 12, 31);
        }
    }
}
