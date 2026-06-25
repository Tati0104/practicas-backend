package com.avh.practicas.estudiante.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorValidacion {
    private int fila;
    private String descripcion;
}
