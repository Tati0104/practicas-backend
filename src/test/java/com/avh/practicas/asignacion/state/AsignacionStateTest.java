package com.avh.practicas.asignacion.state;

import com.avh.practicas.asignacion.entity.Asignacion;
import com.avh.practicas.asignacion.entity.EstadoAsignacion;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AsignacionStateTest {

    @Test
    void asignada_iniciarVinculacion_cambiaAEnProcesoVinculacion() {
        Asignacion asignacion = asignacion(EstadoAsignacion.ASIGNADA);

        new AsignacionContext(asignacion).iniciarVinculacion();

        assertEquals(EstadoAsignacion.EN_PROCESO_VINCULACION, asignacion.getEstado());
    }

    @Test
    void asignada_cancelar_cambiaACancelada() {
        Asignacion asignacion = asignacion(EstadoAsignacion.ASIGNADA);

        new AsignacionContext(asignacion).cancelar("No continua");

        assertEquals(EstadoAsignacion.CANCELADA, asignacion.getEstado());
    }

    @Test
    void asignada_cancelar_guardaMotivo() {
        Asignacion asignacion = asignacion(EstadoAsignacion.ASIGNADA);

        new AsignacionContext(asignacion).cancelar("Cambio de empresa");

        assertEquals("Cambio de empresa", asignacion.getMotivoCancelacion());
    }

    @Test
    void asignada_completarVinculacion_lanzaIllegalStateException() {
        Asignacion asignacion = asignacion(EstadoAsignacion.ASIGNADA);

        assertThrows(IllegalStateException.class, () -> new AsignacionContext(asignacion).completarVinculacion());
    }

    @Test
    void asignada_cancelarConMotivoNulo_lanzaIllegalArgumentException() {
        Asignacion asignacion = asignacion(EstadoAsignacion.ASIGNADA);

        assertThrows(IllegalArgumentException.class, () -> new AsignacionContext(asignacion).cancelar(null));
    }

    @Test
    void asignada_cancelarConMotivoEnBlanco_lanzaIllegalArgumentException() {
        Asignacion asignacion = asignacion(EstadoAsignacion.ASIGNADA);

        assertThrows(IllegalArgumentException.class, () -> new AsignacionContext(asignacion).cancelar(" "));
    }

    @Test
    void enProceso_completarVinculacion_cambiaAVinculada() {
        Asignacion asignacion = asignacion(EstadoAsignacion.EN_PROCESO_VINCULACION);

        new AsignacionContext(asignacion).completarVinculacion();

        assertEquals(EstadoAsignacion.VINCULADA, asignacion.getEstado());
    }

    @Test
    void enProceso_completarVinculacion_asignaFechaVinculacion() {
        Asignacion asignacion = asignacion(EstadoAsignacion.EN_PROCESO_VINCULACION);

        new AsignacionContext(asignacion).completarVinculacion();

        assertNotNull(asignacion.getFechaVinculacion());
    }

    @Test
    void enProceso_iniciarVinculacion_lanzaIllegalStateException() {
        Asignacion asignacion = asignacion(EstadoAsignacion.EN_PROCESO_VINCULACION);

        assertThrows(IllegalStateException.class, () -> new AsignacionContext(asignacion).iniciarVinculacion());
    }

    @Test
    void enProceso_cancelar_cambiaACancelada() {
        Asignacion asignacion = asignacion(EstadoAsignacion.EN_PROCESO_VINCULACION);

        new AsignacionContext(asignacion).cancelar("Documentos rechazados");

        assertEquals(EstadoAsignacion.CANCELADA, asignacion.getEstado());
    }

    @Test
    void enProceso_cancelar_guardaMotivo() {
        Asignacion asignacion = asignacion(EstadoAsignacion.EN_PROCESO_VINCULACION);

        new AsignacionContext(asignacion).cancelar("Sin convenio");

        assertEquals("Sin convenio", asignacion.getMotivoCancelacion());
    }

    @Test
    void enProceso_cancelarConMotivoEnBlanco_lanzaIllegalArgumentException() {
        Asignacion asignacion = asignacion(EstadoAsignacion.EN_PROCESO_VINCULACION);

        assertThrows(IllegalArgumentException.class, () -> new AsignacionContext(asignacion).cancelar("\t"));
    }

    @Test
    void vinculada_iniciarVinculacion_lanzaIllegalStateException() {
        Asignacion asignacion = asignacion(EstadoAsignacion.VINCULADA);

        assertThrows(IllegalStateException.class, () -> new AsignacionContext(asignacion).iniciarVinculacion());
    }

    @Test
    void vinculada_completarVinculacion_lanzaIllegalStateException() {
        Asignacion asignacion = asignacion(EstadoAsignacion.VINCULADA);

        assertThrows(IllegalStateException.class, () -> new AsignacionContext(asignacion).completarVinculacion());
    }

    @Test
    void vinculada_cancelar_lanzaIllegalStateException() {
        Asignacion asignacion = asignacion(EstadoAsignacion.VINCULADA);

        assertThrows(IllegalStateException.class, () -> new AsignacionContext(asignacion).cancelar("No aplica"));
    }

    @Test
    void cancelada_iniciarVinculacion_lanzaIllegalStateException() {
        Asignacion asignacion = asignacion(EstadoAsignacion.CANCELADA);

        assertThrows(IllegalStateException.class, () -> new AsignacionContext(asignacion).iniciarVinculacion());
    }

    @Test
    void cancelada_completarVinculacion_lanzaIllegalStateException() {
        Asignacion asignacion = asignacion(EstadoAsignacion.CANCELADA);

        assertThrows(IllegalStateException.class, () -> new AsignacionContext(asignacion).completarVinculacion());
    }

    @Test
    void cancelada_cancelar_lanzaIllegalStateException() {
        Asignacion asignacion = asignacion(EstadoAsignacion.CANCELADA);

        assertThrows(IllegalStateException.class, () -> new AsignacionContext(asignacion).cancelar("No aplica"));
    }

    @Test
    void contexto_transicionar_actualizaEstadoEntidad() {
        Asignacion asignacion = asignacion(EstadoAsignacion.ASIGNADA);

        new AsignacionContext(asignacion).transicionar(EstadoAsignacion.CANCELADA);

        assertEquals(EstadoAsignacion.CANCELADA, asignacion.getEstado());
    }

    @Test
    void contexto_transicionar_actualizaEstadoInterno() {
        Asignacion asignacion = asignacion(EstadoAsignacion.ASIGNADA);
        AsignacionContext context = new AsignacionContext(asignacion);

        context.transicionar(EstadoAsignacion.EN_PROCESO_VINCULACION);
        context.completarVinculacion();

        assertEquals(EstadoAsignacion.VINCULADA, asignacion.getEstado());
    }

    private Asignacion asignacion(EstadoAsignacion estado) {
        return Asignacion.builder()
                .id(1L)
                .estudianteId(10L)
                .vacanteId(20L)
                .estado(estado)
                .build();
    }
}
