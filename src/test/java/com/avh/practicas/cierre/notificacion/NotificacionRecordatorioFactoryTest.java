package com.avh.practicas.cierre.notificacion;

import com.avh.practicas.cierre.entity.EstadoEncuesta;
import com.avh.practicas.cierre.entity.TipoEncuesta;
import com.avh.practicas.empresa.entity.TutorEmpresarial;
import com.avh.practicas.empresa.repository.TutorEmpresarialRepository;
import com.avh.practicas.estudiante.entity.Estudiante;
import com.avh.practicas.estudiante.entity.Expediente;
import com.avh.practicas.estudiante.entity.InstanciaPractica;
import com.avh.practicas.shared.evento.NotificadorEventos;
import com.avh.practicas.shared.evento.TipoEventoSistema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificacionRecordatorioFactoryTest {

    @Mock
    private TutorEmpresarialRepository tutorRepository;
    @Mock
    private JdbcTemplate jdbcTemplate;
    @Mock
    private NotificadorEventos notificadorEventos;

    private NotificacionRecordatorioFactory factory;

    @BeforeEach
    void setUp() {
        factory = new NotificacionRecordatorioFactory(tutorRepository, jdbcTemplate);
    }

    @Test
    void crear_TutorPendienteRetornaNotificacionConDatosTutor() {
        InstanciaPractica practica = InstanciaPractica.builder().id(1L).tutorId(7L).build();
        when(tutorRepository.findById(7L)).thenReturn(Optional.of(
                TutorEmpresarial.builder().id(7L).correo("tutor@demo.com").nombre("Tutor").usuarioId(70L).build()
        ));

        NotificacionRecordatorio result = factory.crear(practica, TipoEncuesta.TUTOR, EstadoEncuesta.PENDIENTE);

        assertEquals(TipoEventoSistema.RECORDATORIO_ENCUESTA, result.tipoEvento());
        assertEquals("tutor@demo.com", result.correo());
        assertEquals(70L, result.usuarioId());
    }

    @Test
    void crear_FallaSiEncuestaCompletada() {
        InstanciaPractica practica = InstanciaPractica.builder().id(1L).tutorId(7L).build();

        assertThrows(IllegalStateException.class, () ->
                factory.crear(practica, TipoEncuesta.TUTOR, EstadoEncuesta.COMPLETADA)
        );
        verifyNoInteractions(tutorRepository, jdbcTemplate);
    }

    @Test
    void crear_FallaSiTutorNoTieneDestinatario() {
        InstanciaPractica practica = InstanciaPractica.builder().id(1L).build();

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                factory.crear(practica, TipoEncuesta.TUTOR, EstadoEncuesta.PENDIENTE)
        );

        assertTrue(exception.getMessage().contains("TUTOR"));
        verifyNoInteractions(tutorRepository, jdbcTemplate);
    }

    @Test
    void crear_EstudiantePendienteRetornaNotificacionConUsuarioId() {
        InstanciaPractica practica = practicaEstudiante();
        when(jdbcTemplate.queryForObject(any(String.class), eq(Long.class), eq(3L))).thenReturn(30L);

        NotificacionRecordatorio result = factory.crear(practica, TipoEncuesta.ESTUDIANTE, EstadoEncuesta.EN_BORRADOR);

        assertEquals("est@demo.com", result.correo());
        assertEquals("Ana", result.nombreDestinatario());
        assertEquals(30L, result.usuarioId());
    }

    @Test
    void crear_EstudianteSinUsuarioVinculadoMantieneCorreo() {
        InstanciaPractica practica = practicaEstudiante();
        when(jdbcTemplate.queryForObject(any(String.class), eq(Long.class), eq(3L))).thenThrow(new RuntimeException("sin usuario"));

        NotificacionRecordatorio result = factory.crear(practica, TipoEncuesta.ESTUDIANTE, EstadoEncuesta.PENDIENTE);

        assertEquals("est@demo.com", result.correo());
        assertNull(result.usuarioId());
    }

    @Test
    void dispatcher_ConvierteNotificacionAEventoSistema() {
        NotificacionRecordatorioDispatcher dispatcher = new NotificacionRecordatorioDispatcher(notificadorEventos);
        NotificacionRecordatorio notificacion = new NotificacionRecordatorio(
                TipoEventoSistema.RECORDATORIO_ENCUESTA,
                1L,
                TipoEncuesta.TUTOR,
                7L,
                "tutor@demo.com",
                "Tutor"
        );

        dispatcher.enviar(notificacion);

        verify(notificadorEventos).notificar(argThat(evento ->
                evento.getTipo() == TipoEventoSistema.RECORDATORIO_ENCUESTA
                        && "cierre".equals(evento.getModulo())
                        && evento.getDatos().get("correo").equals("tutor@demo.com")
        ));
    }

    private InstanciaPractica practicaEstudiante() {
        Estudiante estudiante = Estudiante.builder()
                .id(3L)
                .nombre("Ana")
                .correo("est@demo.com")
                .build();
        return InstanciaPractica.builder()
                .id(1L)
                .expediente(Expediente.builder().estudiante(estudiante).build())
                .build();
    }
}
