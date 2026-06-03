package com.avh.practicas.cierre.service;

import com.avh.practicas.cierre.entity.Encuesta;
import com.avh.practicas.cierre.entity.EstadoEncuesta;
import com.avh.practicas.cierre.entity.TipoEncuesta;
import com.avh.practicas.cierre.repository.EncuestaRepository;
import com.avh.practicas.empresa.entity.TutorEmpresarial;
import com.avh.practicas.empresa.repository.TutorEmpresarialRepository;
import com.avh.practicas.estudiante.entity.Estudiante;
import com.avh.practicas.estudiante.entity.InstanciaPractica;
import com.avh.practicas.estudiante.repository.InstanciaPracticaRepository;
import com.avh.practicas.seguimiento.service.BitacoraService;
import com.avh.practicas.shared.evento.EventoSistema;
import com.avh.practicas.shared.evento.NotificadorEventos;
import com.avh.practicas.shared.evento.TipoEventoSistema;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Implementación del servicio de encuestas de práctica.
 * Aplica el patrón Factory Method para la creación y despacho de notificaciones correspondientes.
 */
@Service("encuestaServiceImpl")
@RequiredArgsConstructor
@Transactional
public class EncuestaServiceImpl implements EncuestaService {

    private final EncuestaRepository encuestaRepository;
    private final InstanciaPracticaRepository practicaRepository;
    private final TutorEmpresarialRepository tutorRepository;
    private final NotificadorEventos notificadorEventos;
    private final BitacoraService bitacoraService;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Encuesta crearEncuestaPendiente(Long practicaId, TipoEncuesta tipo) {
        InstanciaPractica practica = practicaRepository.findById(practicaId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la práctica con ID: " + practicaId));

        // Verificar si ya existe una encuesta para este tipo en la práctica
        Optional<Encuesta> existente = encuestaRepository.findByInstanciaPracticaIdAndTipo(practicaId, tipo);
        if (existente.isPresent()) {
            return existente.get();
        }

        // Factory Method: Crear y persistir la encuesta PENDIENTE
        Encuesta encuesta = Encuesta.builder()
                .instanciaPractica(practica)
                .tipo(tipo)
                .estado(EstadoEncuesta.PENDIENTE)
                .fechaEnvioInvitacion(LocalDateTime.now())
                .build();

        Encuesta guardada = encuestaRepository.save(encuesta);

        // Disparar invitación de correo según el tipo
        despacharNotificacionEncuesta(TipoEventoSistema.ENCUESTA_DISPONIBLE, practica, tipo);

        return guardada;
    }

    @Override
    public Encuesta guardarBorrador(Long encuestaId, String respuestasJson) {
        Encuesta encuesta = encuestaRepository.findById(encuestaId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la encuesta con ID: " + encuestaId));

        if (encuesta.getEstado() == EstadoEncuesta.COMPLETADA) {
            throw new IllegalStateException("No se puede guardar borrador de una encuesta completada.");
        }

        encuesta.setRespuestasJson(respuestasJson);
        encuesta.setEstado(EstadoEncuesta.EN_BORRADOR);

        return encuestaRepository.save(encuesta);
    }

    @Override
    public Encuesta enviar(Long encuestaId) {
        Encuesta encuesta = encuestaRepository.findById(encuestaId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la encuesta con ID: " + encuestaId));

        if (encuesta.getEstado() == EstadoEncuesta.COMPLETADA) {
            throw new IllegalStateException("La encuesta ya ha sido enviada previamente.");
        }

        // Validar respuestas requeridas
        if (encuesta.getRespuestasJson() == null || encuesta.getRespuestasJson().trim().isEmpty() || encuesta.getRespuestasJson().equals("{}")) {
            throw new IllegalArgumentException("Debe completar las respuestas antes de enviar la encuesta.");
        }

        encuesta.setEstado(EstadoEncuesta.COMPLETADA);
        return encuestaRepository.save(encuesta);
    }

    @Override
    public void enviarRecordatorio(Long practicaId, TipoEncuesta tipo) {
        Encuesta encuesta = encuestaRepository.findByInstanciaPracticaIdAndTipo(practicaId, tipo)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la encuesta para esta práctica y tipo."));

        if (encuesta.getEstado() == EstadoEncuesta.COMPLETADA) {
            throw new IllegalStateException("La encuesta ya se encuentra completada.");
        }

        // Consultar en la bitácora si se envió un recordatorio hoy
        Integer countHoy = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM bitacora_auditoria WHERE tabla_afectada = 'encuestas' AND accion = 'RECORDATORIO' AND detalle LIKE ? AND fecha >= CURRENT_DATE",
                Integer.class,
                "%" + tipo.name() + "%practicaId:" + practicaId + "%"
        );

        if (countHoy != null && countHoy > 0) {
            throw new IllegalStateException("Máximo un recordatorio diario permitido por encuesta.");
        }

        // Despachar el recordatorio por correo
        despacharNotificacionEncuesta(TipoEventoSistema.RECORDATORIO_ENCUESTA, encuesta.getInstanciaPractica(), tipo);

        // Registrar en la bitácora de auditoría
        bitacoraService.registrar("encuestas", "RECORDATORIO", null, "Recordatorio enviado a " + tipo.name() + " para la encuesta de la practicaId:" + practicaId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isCompleta(Long practicaId, TipoEncuesta tipo) {
        return encuestaRepository.findByInstanciaPracticaIdAndTipo(practicaId, tipo)
                .map(e -> e.getEstado() == EstadoEncuesta.COMPLETADA)
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Encuesta> obtenerPorPracticaYTipo(Long practicaId, TipoEncuesta tipo) {
        return encuestaRepository.findByInstanciaPracticaIdAndTipo(practicaId, tipo);
    }

    /**
     * Helper para preparar la información y despachar el evento del sistema para notificar por correo.
     */
    private void despacharNotificacionEncuesta(TipoEventoSistema eventoTipo, InstanciaPractica practica, TipoEncuesta destinatarioTipo) {
        String correo = "";
        String nombre = "";
        Long usuarioId = null;

        if (destinatarioTipo == TipoEncuesta.TUTOR) {
            if (practica.getTutorId() != null) {
                TutorEmpresarial tutor = tutorRepository.findById(practica.getTutorId()).orElse(null);
                if (tutor != null) {
                    correo = tutor.getCorreo();
                    nombre = tutor.getNombre();
                    usuarioId = tutor.getUsuarioId();
                }
            }
        } else {
            if (practica.getExpediente() != null && practica.getExpediente().getEstudiante() != null) {
                Estudiante estudiante = practica.getExpediente().getEstudiante();
                correo = estudiante.getCorreo();
                nombre = estudiante.getNombre();
                try {
                    usuarioId = jdbcTemplate.queryForObject(
                            "SELECT usuario_id FROM estudiantes WHERE id = ?",
                            Long.class,
                            estudiante.getId()
                    );
                } catch (Exception e) {
                    // Ignorar
                }
            }
        }

        if (correo.isEmpty()) {
            return; // No se puede enviar correo sin destinatario
        }

        Map<String, Object> datos = new HashMap<>();
        datos.put("correo", correo);
        datos.put("nombre", nombre);

        EventoSistema evento = EventoSistema.crear(
                eventoTipo,
                usuarioId,
                "cierre",
                practica.getId(),
                datos
        );

        notificadorEventos.notificar(evento);
    }
}
