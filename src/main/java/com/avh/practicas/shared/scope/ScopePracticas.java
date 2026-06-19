package com.avh.practicas.shared.scope;

import com.avh.practicas.estudiante.entity.InstanciaPractica;

import java.util.Set;

/**
 * Resultado de resolver qué InstanciaPractica puede ver el usuario autenticado,
 * sin exigirle elegir un programa de antemano. Se reutiliza en Seguimiento,
 * Evaluaciones y Cierre para que los tres apliquen el mismo criterio de visibilidad.
 */
public class ScopePracticas {

    private final boolean todas;
    private final Set<Long> programaIds;
    private final Long estudianteId;

    private ScopePracticas(boolean todas, Set<Long> programaIds, Long estudianteId) {
        this.todas = todas;
        this.programaIds = programaIds;
        this.estudianteId = estudianteId;
    }

    public static ScopePracticas todas() {
        return new ScopePracticas(true, null, null);
    }

    public static ScopePracticas porProgramas(Set<Long> programaIds) {
        return new ScopePracticas(false, programaIds, null);
    }

    public static ScopePracticas deEstudiante(Long estudianteId) {
        return new ScopePracticas(false, null, estudianteId);
    }

    public static ScopePracticas ninguna() {
        return new ScopePracticas(false, Set.of(), null);
    }

    public boolean esVisible(InstanciaPractica practica) {
        if (practica.getExpediente() == null || practica.getExpediente().getEstudiante() == null) {
            return false;
        }

        if (estudianteId != null) {
            return estudianteId.equals(practica.getExpediente().getEstudiante().getId());
        }

        if (todas) {
            return true;
        }

        if (programaIds == null || programaIds.isEmpty()) {
            return false;
        }

        return practica.getExpediente().getEstudiante().getPrograma() != null
                && programaIds.contains(practica.getExpediente().getEstudiante().getPrograma().getId());
    }

    /** true si el resultado está acotado a una sola práctica (la del propio estudiante). */
    public boolean esEstudiantePropio() {
        return estudianteId != null;
    }
}