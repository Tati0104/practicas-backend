package com.avh.practicas.estudiante.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstudianteDto {

    @NotBlank(message = "La identificación es obligatoria")
    private String identificacion;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "Debe proporcionar un correo electrónico válido")
    private String correo;

    private String telefono;
    private String contactoEmergencia;

    @NotNull(message = "El ID del programa es obligatorio")
    private Long programaId;

    @Min(value = 1, message = "El semestre debe ser mayor o igual a 1")
    private Integer semestre;

    @Min(value = 0, message = "Los créditos aprobados no pueden ser negativos")
    private Integer creditosAprobados;

    private Double promedioAcumulado;
}
