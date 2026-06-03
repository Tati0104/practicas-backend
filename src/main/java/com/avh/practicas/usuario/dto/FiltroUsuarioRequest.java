package com.avh.practicas.usuario.dto;

import com.avh.practicas.shared.entity.Rol;
import com.avh.practicas.shared.entity.Scope;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FiltroUsuarioRequest {
    private Rol     rol;
    private Boolean activo;
    private Scope   scope;
    private Long    programaId;
    private Long    facultadId;
}