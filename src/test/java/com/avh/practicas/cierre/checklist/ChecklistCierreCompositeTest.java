package com.avh.practicas.cierre.checklist;

import com.avh.practicas.cierre.checklist.composite.ChecklistCierre;
import com.avh.practicas.cierre.checklist.composite.GrupoRequisitos;
import com.avh.practicas.cierre.checklist.leaf.ItemNotaDocente;
import com.avh.practicas.cierre.checklist.leaf.ItemNotaFinal;
import com.avh.practicas.cierre.checklist.leaf.ItemNotaTutor;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ChecklistCierreCompositeTest {

    @Test
    void grupoRequisitos_verificaRecursivamenteYCalculaProgreso() {
        GrupoRequisitos grupo = new GrupoRequisitos(
                "Calificaciones",
                true,
                List.of(
                        new ItemNotaDocente(2, 2),
                        new ItemNotaTutor(2, 1),
                        new ItemNotaFinal(false)
                )
        );

        assertFalse(grupo.verificar());
        assertEquals(EstadoItem.EN_BORRADOR, grupo.getEstado());
        assertEquals(1.0 / 3.0, grupo.getProgreso(), 0.001);
    }

    @Test
    void checklistCierre_habilitaBotonSoloSiTodosLosGruposCumplen() {
        GrupoRequisitos calificacionesOk = new GrupoRequisitos(
                "Calificaciones",
                true,
                List.of(new ItemNotaDocente(1, 1), new ItemNotaTutor(1, 1), new ItemNotaFinal(true))
        );
        GrupoRequisitos encuestasPendientes = new GrupoRequisitos(
                "Encuestas",
                true,
                List.of(new ItemNotaFinal(false))
        );

        ChecklistCierre checklist = new ChecklistCierre(10L, List.of(calificacionesOk, encuestasPendientes));

        assertFalse(checklist.habilitarBotonCierre());
        assertTrue(checklist.getResumen().progresoGlobal() > 0);
        assertEquals(10L, checklist.getResumen().practicaId());
    }
}
