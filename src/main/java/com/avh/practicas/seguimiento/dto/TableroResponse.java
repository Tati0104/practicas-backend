package com.avh.practicas.seguimiento.dto;

import java.time.LocalDateTime;

/**
 * DTO que representa cada fila del tablero de seguimiento para el coordinador o secretaria.
 */
public record TableroResponse(
    Long id,
    String estudiante,
    String empresa,
    String docente,
    Integer corte,
    String estadoSeguimiento,
    java.time.LocalDateTime fechaUltimaActividad,
    Integer numeroPractica,
    String estadoPractica
) {}
