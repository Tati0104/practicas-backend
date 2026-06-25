package com.avh.practicas.vinculacion.support;

import com.avh.practicas.asignacion.entity.Asignacion;
import com.avh.practicas.asignacion.entity.EstadoAsignacion;
import com.avh.practicas.shared.pattern.observer.EventoSistema;
import com.avh.practicas.shared.pattern.observer.Observador;
import com.avh.practicas.vinculacion.observer.ObservadorAsignacionBitacora;
import com.avh.practicas.vinculacion.observer.ObservadorAsignacionCorreo;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AsignacionSubjectRegistryTest {

    @Test
    void registrarObservador_agregaObservador() {
        AsignacionSubject subject = subject();
        Observador observador = mock(Observador.class);

        subject.registrarObservador(observador);

        assertTrue(subject.getObservadores().contains(observador));
    }

    @Test
    void registrarObservador_duplicado_noLoAgregaDosVeces() {
        AsignacionSubject subject = subject();
        Observador observador = mock(Observador.class);

        subject.registrarObservador(observador);
        subject.registrarObservador(observador);

        assertEquals(1, subject.getObservadores().size());
    }

    @Test
    void registrarObservador_nulo_noLoAgrega() {
        AsignacionSubject subject = subject();

        subject.registrarObservador(null);

        assertTrue(subject.getObservadores().isEmpty());
    }

    @Test
    void eliminarObservador_remueveObservadorRegistrado() {
        AsignacionSubject subject = subject();
        Observador observador = mock(Observador.class);
        subject.registrarObservador(observador);

        subject.eliminarObservador(observador);

        assertFalse(subject.getObservadores().contains(observador));
    }

    @Test
    void notificarObservadores_sinObservadores_noLanzaExcepcion() {
        AsignacionSubject subject = subject();

        assertDoesNotThrow(() -> subject.notificarObservadores("EVENTO", Map.of("id", 1L)));
    }

    @Test
    void notificarObservadores_conMapa_enviaMapaAlObservador() {
        AsignacionSubject subject = subject();
        Observador observador = mock(Observador.class);
        Map<String, Object> datos = Map.of("estado", "ASIGNADA");
        subject.registrarObservador(observador);

        subject.notificarObservadores("EVENTO", datos);

        verify(observador).actualizar("EVENTO", datos);
    }

    @Test
    void notificarObservadores_conObjeto_enviaObjetoAlObservador() {
        AsignacionSubject subject = subject();
        Observador observador = mock(Observador.class);
        subject.registrarObservador(observador);

        subject.notificarObservadores("EVENTO", "detalle");

        verify(observador).actualizar("EVENTO", "detalle");
    }

    @Test
    void agregarObservador_delegaEnRegistroSeguro() {
        AsignacionSubject subject = subject();
        Observador observador = mock(Observador.class);

        subject.agregarObservador(observador);

        assertEquals(1, subject.getObservadores().size());
    }

    @Test
    void notificar_conEventoSistema_enviaTipoYDatos() {
        AsignacionSubject subject = subject();
        Observador observador = mock(Observador.class);
        Map<String, Object> datos = Map.of("asignacionId", 1L);
        subject.registrarObservador(observador);

        subject.notificar(EventoSistema.crear("CAMBIO", datos));

        verify(observador).actualizar("CAMBIO", datos);
    }

    @Test
    void registry_registraObservadoresBitacoraYCorreo() {
        ObservadorAsignacionBitacora bitacora = mock(ObservadorAsignacionBitacora.class);
        ObservadorAsignacionCorreo correo = mock(ObservadorAsignacionCorreo.class);
        AsignacionSubject subject = subject();

        new AsignacionObservadorRegistry(bitacora, correo).registrarObservadores(subject);

        assertAll(
                () -> assertTrue(subject.getObservadores().contains(bitacora)),
                () -> assertTrue(subject.getObservadores().contains(correo))
        );
    }

    private AsignacionSubject subject() {
        return new AsignacionSubject(Asignacion.builder()
                .id(1L)
                .estudianteId(2L)
                .vacanteId(3L)
                .estado(EstadoAsignacion.ASIGNADA)
                .build());
    }
}
