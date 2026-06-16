package com.avh.practicas.usuario.dto;

import com.avh.practicas.shared.enums.Rol;
import com.avh.practicas.shared.enums.Scope;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CrearUsuarioRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no tiene formato válido")
    private String correo;

    @NotNull(message = "El rol es obligatorio")
    private Rol rol;

    /** Ignorado: se asigna automáticamente según el rol. */
    private Scope scope;

    private Long programaId;
    private Long facultadId;
    private Long empresaId;
    private String cargoTutor;
    private String telefonoTutor;
    private String identificacion;
    private String telefono;
}