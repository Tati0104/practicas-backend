package com.avh.practicas.cierre.service;

import com.avh.practicas.cierre.entity.Encuesta;
import com.avh.practicas.cierre.entity.EstadoEncuesta;
import com.avh.practicas.cierre.entity.TipoEncuesta;
import com.avh.practicas.cierre.notificacion.NotificacionRecordatorio;
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
import com.avh.practicas.shared.evento.TipoEventoSistema;
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
        when(encuestaEnlaceService.buildEnlaceEncuesta(practicaId)).thenReturn("http://localhost:5173/calificaciones/1");

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

    @Test
    void crearEncuestaPendiente_RetornaExistenteSinNotificar() {
        Long practicaId = 1L;
        Encuesta existente = encuesta(practicaId, TipoEncuesta.ESTUDIANTE, EstadoEncuesta.PENDIENTE);
        when(practicaRepository.findById(practicaId)).thenReturn(Optional.of(practicaEstudiante(practicaId)));
        when(encuestaRepository.findByInstanciaPracticaIdAndTipo(practicaId, TipoEncuesta.ESTUDIANTE)).thenReturn(Optional.of(existente));

        Encuesta result = service.crearEncuestaPendiente(practicaId, TipoEncuesta.ESTUDIANTE);

        assertSame(existente, result);
        verify(encuestaRepository, never()).save(any());
        verifyNoInteractions(notificadorEventos);
    }

    @Test
    void crearEncuestaPendiente_FallaPracticaNoExiste() {
        Long practicaId = 404L;
        when(practicaRepository.findById(practicaId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                service.crearEncuestaPendiente(practicaId, TipoEncuesta.ESTUDIANTE)
        );

        assertTrue(exception.getMessage().contains("ID: 404"));
        verifyNoInteractions(encuestaRepository, notificadorEventos);
    }

    @Test
    void crearEncuestaPendiente_TutorNotificaConDatosTutor() {
        Long practicaId = 1L;
        Long tutorId = 7L;
        InstanciaPractica practica = InstanciaPractica.builder().id(practicaId).tutorId(tutorId).build();
        TutorEmpresarial tutor = TutorEmpresarial.builder()
                .id(tutorId)
                .nombre("Tutor Uno")
                .correo("tutor@demo.com")
                .usuarioId(77L)
                .build();
        when(practicaRepository.findById(practicaId)).thenReturn(Optional.of(practica));
        when(encuestaRepository.findByInstanciaPracticaIdAndTipo(practicaId, TipoEncuesta.TUTOR)).thenReturn(Optional.empty());
        when(encuestaRepository.save(any(Encuesta.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(tutorRepository.findById(tutorId)).thenReturn(Optional.of(tutor));
        when(encuestaEnlaceService.buildEnlaceEncuesta(practicaId)).thenReturn("http://localhost/encuesta/1");

        Encuesta result = service.crearEncuestaPendiente(practicaId, TipoEncuesta.TUTOR);

        assertEquals(TipoEncuesta.TUTOR, result.getTipo());
        verify(notificadorEventos).notificar(any(EventoSistema.class));
    }

    @Test
    void guardarBorrador_FallaEncuestaNoExiste() {
        Long encuestaId = 10L;
        when(encuestaRepository.findById(encuestaId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                service.guardarBorrador(encuestaId, "{\"p1\":\"si\"}")
        );

        assertTrue(exception.getMessage().contains("encuesta con ID: 10"));
        verify(encuestaRepository, never()).save(any());
    }

    @Test
    void guardarBorrador_FallaEncuestaCompletada() {
        Long encuestaId = 10L;
        when(encuestaRepository.findById(encuestaId)).thenReturn(Optional.of(
                Encuesta.builder().id(encuestaId).estado(EstadoEncuesta.COMPLETADA).build()
        ));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                service.guardarBorrador(encuestaId, "{\"p1\":\"si\"}")
        );

        assertEquals("No se puede guardar borrador de una encuesta completada.", exception.getMessage());
        verify(encuestaRepository, never()).save(any());
    }

    @Test
    void enviar_ExitosoMarcaCompletada() {
        Long encuestaId = 10L;
        Encuesta encuesta = Encuesta.builder()
                .id(encuestaId)
                .estado(EstadoEncuesta.EN_BORRADOR)
                .respuestasJson("{\"p1\":\"si\"}")
                .build();
        when(encuestaRepository.findById(encuestaId)).thenReturn(Optional.of(encuesta));
        when(encuestaRepository.save(any(Encuesta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Encuesta result = service.enviar(encuestaId);

        assertEquals(EstadoEncuesta.COMPLETADA, result.getEstado());
        verify(encuestaRepository).save(encuesta);
    }

    @Test
    void enviar_FallaEncuestaCompletada() {
        Long encuestaId = 10L;
        when(encuestaRepository.findById(encuestaId)).thenReturn(Optional.of(
                Encuesta.builder().id(encuestaId).estado(EstadoEncuesta.COMPLETADA).respuestasJson("{\"p1\":\"si\"}").build()
        ));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                service.enviar(encuestaId)
        );

        assertEquals("La encuesta ya ha sido enviada previamente.", exception.getMessage());
        verify(encuestaRepository, never()).save(any());
    }

    @Test
    void enviar_FallaRespuestasJsonVacio() {
        Long encuestaId = 10L;
        when(encuestaRepository.findById(encuestaId)).thenReturn(Optional.of(
                Encuesta.builder().id(encuestaId).estado(EstadoEncuesta.PENDIENTE).respuestasJson("{}").build()
        ));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                service.enviar(encuestaId)
        );

        assertEquals("Debe completar las respuestas antes de enviar la encuesta.", exception.getMessage());
        verify(encuestaRepository, never()).save(any());
    }

    @Test
    void enviar_FallaEncuestaNoExiste() {
        Long encuestaId = 10L;
        when(encuestaRepository.findById(encuestaId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                service.enviar(encuestaId)
        );

        assertTrue(exception.getMessage().contains("encuesta con ID: 10"));
    }

    @Test
    void enviarRecordatorio_Exitoso() {
        Long practicaId = 1L;
        TipoEncuesta tipo = TipoEncuesta.TUTOR;
        Encuesta encuesta = encuesta(practicaId, tipo, EstadoEncuesta.PENDIENTE);
        NotificacionRecordatorio notificacion = new NotificacionRecordatorio(
                TipoEventoSistema.ENCUESTA_DISPONIBLE,
                practicaId,
                tipo,
                7L,
                "tutor@demo.com",
                "Tutor"
        );
        when(encuestaRepository.findByInstanciaPracticaIdAndTipo(practicaId, tipo)).thenReturn(Optional.of(encuesta));
        when(jdbcTemplate.queryForObject(any(String.class), eq(Integer.class), any())).thenReturn(0);
        when(recordatorioFactory.crear(encuesta.getInstanciaPractica(), tipo, EstadoEncuesta.PENDIENTE)).thenReturn(notificacion);
        when(encuestaRepository.save(any(Encuesta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.enviarRecordatorio(practicaId, tipo);

        verify(recordatorioDispatcher).enviar(notificacion);
        verify(bitacoraService).registrar(eq("encuestas"), eq("RECORDATORIO"), isNull(), contains("practicaId:1"));
        assertNotNull(encuesta.getFechaUltimoRecordatorio());
    }

    @Test
    void validarRecordatorioDiario_ExitosoSinRecordatoriosHoy() {
        Long practicaId = 1L;
        TipoEncuesta tipo = TipoEncuesta.ESTUDIANTE;
        when(encuestaRepository.findByInstanciaPracticaIdAndTipo(practicaId, tipo)).thenReturn(Optional.of(
                encuesta(practicaId, tipo, EstadoEncuesta.PENDIENTE)
        ));
        when(jdbcTemplate.queryForObject(any(String.class), eq(Integer.class), any())).thenReturn(0);

        assertDoesNotThrow(() -> service.validarRecordatorioDiario(practicaId, tipo));
    }

    @Test
    void registrarRecordatorioEnviado_FallaEncuestaCompletada() {
        Long practicaId = 1L;
        TipoEncuesta tipo = TipoEncuesta.ESTUDIANTE;
        when(encuestaRepository.findByInstanciaPracticaIdAndTipo(practicaId, tipo)).thenReturn(Optional.of(
                encuesta(practicaId, tipo, EstadoEncuesta.COMPLETADA)
        ));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                service.registrarRecordatorioEnviado(practicaId, tipo)
        );

        assertEquals("La encuesta ya se encuentra completada.", exception.getMessage());
        verifyNoInteractions(bitacoraService);
    }

    @Test
    void isCompleta_RetornaTrueSoloCuandoEncuestaCompletada() {
        Long practicaId = 1L;
        TipoEncuesta tipo = TipoEncuesta.ESTUDIANTE;
        when(encuestaRepository.findByInstanciaPracticaIdAndTipo(practicaId, tipo)).thenReturn(Optional.of(
                encuesta(practicaId, tipo, EstadoEncuesta.COMPLETADA)
        ));

        assertTrue(service.isCompleta(practicaId, tipo));
    }

    @Test
    void enviarInvitacion_ExistentePendienteRenuevaFechaYNotifica() {
        Long practicaId = 1L;
        TipoEncuesta tipo = TipoEncuesta.ESTUDIANTE;
        Encuesta encuesta = encuesta(practicaId, tipo, EstadoEncuesta.PENDIENTE);
        when(encuestaRepository.findByInstanciaPracticaIdAndTipo(practicaId, tipo)).thenReturn(Optional.of(encuesta));
        when(jdbcTemplate.queryForObject(any(String.class), eq(Long.class), eq(3L))).thenReturn(33L);
        when(encuestaEnlaceService.buildEnlaceEncuesta(practicaId)).thenReturn("http://localhost/encuesta/1");
        when(encuestaRepository.save(any(Encuesta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Encuesta result = service.enviarInvitacion(practicaId, tipo);

        assertSame(encuesta, result);
        assertNotNull(result.getFechaEnvioInvitacion());
        verify(notificadorEventos).notificar(any(EventoSistema.class));
        verify(encuestaRepository).save(encuesta);
    }

    private Encuesta encuesta(Long practicaId, TipoEncuesta tipo, EstadoEncuesta estado) {
        return Encuesta.builder()
                .id(10L)
                .instanciaPractica(practicaEstudiante(practicaId))
                .tipo(tipo)
                .estado(estado)
                .build();
    }

    private InstanciaPractica practicaEstudiante(Long practicaId) {
        Estudiante estudiante = Estudiante.builder()
                .id(3L)
                .nombre("Estudiante Prueba")
                .correo("est@demo.com")
                .build();
        Expediente expediente = Expediente.builder().estudiante(estudiante).build();
        return InstanciaPractica.builder()
                .id(practicaId)
                .expediente(expediente)
                .build();
    }
}
