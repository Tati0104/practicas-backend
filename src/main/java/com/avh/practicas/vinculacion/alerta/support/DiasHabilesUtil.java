package com.avh.practicas.vinculacion.alerta.support;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Utilidad para umbral de inactividad en días hábiles (lunes a viernes).
 */
public final class DiasHabilesUtil {

    private DiasHabilesUtil() {
    }

    public static LocalDateTime inicioHaceDiasHabiles(int diasHabiles) {
        LocalDate fecha = LocalDate.now();
        int restantes = diasHabiles;
        while (restantes > 0) {
            fecha = fecha.minusDays(1);
            if (esDiaHabil(fecha)) {
                restantes--;
            }
        }
        return fecha.atStartOfDay();
    }

    public static boolean esDiaHabil(LocalDate fecha) {
        DayOfWeek dia = fecha.getDayOfWeek();
        return dia != DayOfWeek.SATURDAY && dia != DayOfWeek.SUNDAY;
    }

    public static LocalDateTime finDelDia(LocalDate fecha) {
        return fecha.atTime(LocalTime.MAX);
    }
}
