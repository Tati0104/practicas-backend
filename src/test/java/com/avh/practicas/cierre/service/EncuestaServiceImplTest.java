package com.avh.practicas.cierre.service;

import com.avh.practicas.cierre.entity.Encuesta;
import com.avh.practicas.cierre.entity.EstadoEncuesta;
import com.avh.practicas.cierre.entity.TipoEncuesta;
import com.avh.practicas.cierre.notificacion.NotificacionRecordatorioDispatcher;
import com.avh.practicas.cierre.notificacion.NotificacionRecordatorioFactory;
import com.avh.practicas.cierre.repository.EncuestaRepository;
import com.avh.practicas.cierre.support.EncuestaEnlaceService;
import com.avh.practicas.empresa.entity.TutorEmpresarial;
import com.avh.practicas.empresa.repository.TutorEmpresarialRepository;
import com.avh.practicas.estudiante.entity.Estudiante;
import com.avh.practicas.estudiante.entity.Expediente;
import com.avh.practicas.estudiante.entity.InstanciaPractica;
import com.avh.practicas.estudiante.repository.InstanciaPracticaRepository;
import com.avh.practicas.seguimiento.service.BitacoraService;
import com.avh.practicas.shared.evento.EventoSistema;
import com.avh.practicas.shared.evento.NotificadorEventos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para validar las reglas de negocio de EncuestaServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class EncuestaServiceImplTest {

    @Mock
    private EncuestaRepository encuestaRepository;
    @Mock
    private InstanciaPracticaRepository practicaRepository;
    @Mock
    private TutorEmpresarialRepository tutorRepository;
    @Mock
    private NotificadorEventos notificadorEventos;
    @Mock
    private BitacoraService bitacoraService;
    @Mock
    private JdbcTemplate jdbcTemplate;
    @Mock
    private NotificacionRecordatorioFactory recordatorioFactory;
    @Mock
    private NotificacionRecordatorioDispatcher recordatorioDispatcher;
    @Mock
    private EncuestaEnlaceService encuestaEnlaceService;

    private EncuestaServiceImpl service;

    @BeforeEach
    void setUp() {
        when(encuestaEnlaceService.buildEnlaceEncuesta(any())).thenReturn("http://localhost:5173/calificaciones/1");
        service = new EncuestaServiceImpl(
                encuestaRepository,
                practicaRepository,
                tutorRepository,
                notificadorEventos,
                bitacoraService,
                jdbcTemplate,
                recordatorioFactory,
                recordatorioDispatcher,
                encuestaEnlaceService
        );
    }

    @Test
    void crearEncuestaPendiente_ExitosoNuevo() {
        // Arrange
        Long practicaId = 1L;
        Estudiante estudiante = Estudiante.builder().nombre("Estudiante Prueba").correo("est@demo.com").build();
        Expediente expediente = Expediente.builder().estudiante(estudiante).build();
        InstanciaPractica practica = InstanciaPractica.builder()
                .id(practicaId)
                .expediente(expediente)
                .build();

        when(practicaRepository.findById(practicaId)).thenReturn(Optional.of(practica));
        when(encuestaRepository.findByInstanciaPracticaIdAndTipo(practicaId, TipoEncuesta.ESTUDIANTE)).thenReturn(Optional.empty());
        when(encuestaRepository.save(any(Encuesta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Encuesta result = service.crearEncuestaPendiente(practicaId, TipoEncuesta.ESTUDIANTE);

        // Assert
        assertNotNull(result);
        assertEquals(EstadoEncuesta.PENDIENTE, result.getEstado());
        assertEquals(TipoEncuesta.ESTUDIANTE, result.getTipo());
        verify(notificadorEventos).notificar(any(EventoSistema.class));
        verify(encuestaRepository).save(any(Encuesta.class));
    }

    @Test
    void guardarBorrador_Exitoso() {
        // Arrange
        Long encuestaId = 10L;
        String respuestas = "{\"p1\":\"si\"}";
        Encuesta encuesta = Encuesta.builder()
                .id(encuestaId)
                .estado(EstadoEncuesta.PENDIENTE)
                .build();

        when(encuestaRepository.findById(encuestaId)).thenReturn(Optional.of(encuesta));
        when(encuestaRepository.save(any(Encuesta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Encuesta result = service.guardarBorrador(encuestaId, respuestas);

        // Assert
        assertNotNull(result);
        assertEquals(EstadoEncuesta.EN_BORRADOR, result.getEstado());
        assertEquals(respuestas, result.getRespuestasJson());
    }

    @Test
    void enviar_FallaRespuestasVacias() {
        // Arrange
        Long encuestaId = 10L;
        Encuesta encuesta = Encuesta.builder()
                .id(encuestaId)
                .estado(EstadoEncuesta.EN_BORRADOR)
                .respuestasJson("")
                .build();

        when(encuestaRepository.findById(encuestaId)).thenReturn(Optional.of(encuesta));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                service.enviar(encuestaId)
        );
        assertTrue(exception.getMessage().contains("Debe completar las respuestas"));
    }

    @Test
    void enviarRecordatorio_FallaDuplicadoDiario() {
        // Arrange
        Long practicaId = 1L;
        TipoEncuesta tipo = TipoEncuesta.TUTOR;
        Encuesta encuesta = Encuesta.builder()
                .instanciaPractica(InstanciaPractica.builder().id(practicaId).build())
                .estado(EstadoEncuesta.PENDIENTE)
                .build();

        when(encuestaRepository.findByInstanciaPracticaIdAndTipo(practicaId, tipo)).thenReturn(Optional.of(encuesta));
        // Simular que ya se envió un recordatorio hoy
        when(jdbcTemplate.queryForObject(any(String.class), eq(Integer.class), any())).thenReturn(1);

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                service.enviarRecordatorio(practicaId, tipo)
        );
        assertEquals("Máximo un recordatorio diario permitido por encuesta.", exception.getMessage());
    }
}
