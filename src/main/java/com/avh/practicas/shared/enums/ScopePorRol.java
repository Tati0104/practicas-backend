package com.avh.practicas.shared.enums;

/**
 * Asigna el alcance de acceso según el rol del usuario.
 */
public final class ScopePorRol {

    private ScopePorRol() {
    }

    public static Scope resolver(Rol rol) {
        if (rol == null) {
            throw new IllegalArgumentException("El rol es obligatorio para determinar el alcance");
        }

        return switch (rol) {
            case ADMIN, DIRECCION -> Scope.GLOBAL;
            case COORD_ACADEMICA, COORD_PRACTICA, SECRETARIA -> Scope.FACULTAD;
            case DOCENTE_ASESOR, ESTUDIANTE -> Scope.PROGRAMA;
            case EMPRESA, TUTOR_EMPRESARIAL -> Scope.ASIGNADO;
        };
    }
}
