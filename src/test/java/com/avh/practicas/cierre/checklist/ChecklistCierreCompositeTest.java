package com.avh.practicas.cierre.checklist;

import com.avh.practicas.cierre.checklist.composite.ChecklistCierre;
import com.avh.practicas.cierre.checklist.composite.GrupoRequisitos;
import com.avh.practicas.cierre.checklist.leaf.ItemDocumentoCargado;
import com.avh.practicas.cierre.checklist.leaf.ItemNotaDocente;
import com.avh.practicas.cierre.checklist.leaf.ItemNotaFinal;
import com.avh.practicas.cierre.checklist.leaf.ItemNotaTutor;
import com.avh.practicas.cierre.entity.TipoEncuesta;
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
                        new ItemNotaDocente(1),
                        new ItemNotaTutor(0),
                        new ItemNotaFinal(false)
                )
        );

        assertFalse(grupo.verificar());
        assertEquals(EstadoItem.PENDIENTE, grupo.getEstado());
        assertEquals(1.0 / 3.0, grupo.getProgreso(), 0.001);
    }

    @Test
    void checklistCierre_habilitaBotonSoloSiTodosLosGruposCumplen() {
        GrupoRequisitos calificacionesOk = new GrupoRequisitos(
                "Calificaciones",
                true,
                List.of(new ItemNotaDocente(1), new ItemNotaTutor(1), new ItemNotaFinal(true))
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

    @Test
    void itemNotaDocente_SinNotaQuedaPendiente() {
        ItemNotaDocente item = new ItemNotaDocente(0);

        assertFalse(item.verificar());
        assertEquals(EstadoItem.PENDIENTE, item.getEstado());
        assertTrue(item.isObligatorio());
    }

    @Test
    void itemNotaDocente_ConUnaNotaQuedaCompletado() {
        ItemNotaDocente item = new ItemNotaDocente(1);

        assertTrue(item.verificar());
        assertEquals(EstadoItem.COMPLETADO, item.getEstado());
    }

    @Test
    void itemNotaTutor_ConUnaNotaQuedaCompletado() {
        ItemNotaTutor item = new ItemNotaTutor(1);

        assertTrue(item.verificar());
        assertEquals(EstadoItem.COMPLETADO, item.getEstado());
    }

    @Test
    void itemNotaTutor_ValoresNegativosSeNormalizanComoPendiente() {
        ItemNotaTutor item = new ItemNotaTutor(-5);

        assertFalse(item.verificar());
        assertEquals(EstadoItem.PENDIENTE, item.getEstado());
    }

    @Test
    void itemDocumentoCargado_OpcionalPendienteConservaNombreYObligatoriedad() {
        ItemDocumentoCargado item = new ItemDocumentoCargado("Anexo", false, false);

        assertEquals("Anexo", item.getNombre());
        assertFalse(item.isObligatorio());
        assertFalse(item.verificar());
        assertEquals(EstadoItem.PENDIENTE, item.getEstado());
    }

    @Test
    void grupoRequisitos_IgnoraOpcionalPendiente() {
        GrupoRequisitos grupo = new GrupoRequisitos(
                "Opcionales",
                true,
                List.of(new ItemDocumentoCargado("Anexo", false, false))
        );

        assertTrue(grupo.verificar());
        assertEquals(EstadoItem.COMPLETADO, grupo.getEstado());
        assertEquals(1.0, grupo.getProgreso());
    }

    @Test
    void grupoRequisitos_ObtieneHojasDeGruposAnidados() {
        GrupoRequisitos interno = new GrupoRequisitos("Interno", true, List.of(new ItemNotaFinal(true)));
        GrupoRequisitos externo = new GrupoRequisitos("Externo", true, List.of(interno, new ItemNotaTutor(1)));

        assertEquals(2, externo.getItemsHoja().size());
    }

    @Test
    void checklistCierre_VacioNoHabilitaYResumenQuedaEnCero() {
        ChecklistCierre checklist = new ChecklistCierre(10L, List.of());

        assertFalse(checklist.habilitarBotonCierre());
        assertEquals(0, checklist.getResumen().totalItems());
        assertEquals(0.0, checklist.getResumen().progresoGlobal());
    }

    @Test
    void checklistCierre_BuscarItemEncuestaFallaSiNoExiste() {
        ChecklistCierre checklist = new ChecklistCierre(10L, List.of(
                new GrupoRequisitos("Calificaciones", true, List.of(new ItemNotaFinal(true)))
        ));

        assertThrows(IllegalArgumentException.class, () -> checklist.buscarItemEncuesta(TipoEncuesta.TUTOR));
    }

    @Test
    void checklistCierre_TodosLosGruposObligatoriosCompletosHabilitaCierre() {
        ChecklistCierre checklist = new ChecklistCierre(10L, List.of(
                new GrupoRequisitos("Calificaciones", true, List.of(new ItemNotaDocente(1), new ItemNotaTutor(1))),
                new GrupoRequisitos("Final", true, List.of(new ItemNotaFinal(true)))
        ));

        assertTrue(checklist.habilitarBotonCierre());
        assertTrue(checklist.getResumen().habilitarBotonCierre());
        assertEquals(1.0, checklist.getResumen().progresoGlobal());
    }
}
