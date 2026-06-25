package com.avh.practicas.cierre.checklist.dto;

import com.avh.practicas.cierre.checklist.EstadoItem;
import com.avh.practicas.cierre.entity.EstadoEncuesta;
import com.avh.practicas.cierre.entity.TipoEncuesta;

import java.time.LocalDateTime;

public record ItemResumenDto(
        String nombre,
        boolean obligatorio,
        boolean verificado,
        EstadoItem estado,
        TipoEncuesta tipoEncuesta,
        EstadoEncuesta estadoEncuesta,
        LocalDateTime fechaUltimoRecordatorio
) {
    public static ItemResumenDto simple(String nombre, boolean obligatorio, boolean verificado, EstadoItem estado) {
        return new ItemResumenDto(nombre, obligatorio, verificado, estado, null, null, null);
    }
}
