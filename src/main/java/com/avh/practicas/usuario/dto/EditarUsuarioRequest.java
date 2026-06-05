package com.avh.practicas.usuario.dto;

import com.avh.practicas.shared.enums.Rol;
import com.avh.practicas.shared.enums.Scope;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EditarUsuarioRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotNull(message = "El rol es obligatorio")
    private Rol rol;

    @NotNull(message = "El scope es obligatorio")
    private Scope scope;

    private Long programaId;
    private Long facultadId;
}