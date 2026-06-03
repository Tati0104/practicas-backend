package com.avh.practicas.cierre.notificacion;

import com.avh.practicas.cierre.entity.EstadoEncuesta;
import com.avh.practicas.cierre.entity.TipoEncuesta;
import com.avh.practicas.empresa.entity.TutorEmpresarial;
import com.avh.practicas.empresa.repository.TutorEmpresarialRepository;
import com.avh.practicas.estudiante.entity.Estudiante;
import com.avh.practicas.estudiante.entity.InstanciaPractica;
import com.avh.practicas.shared.evento.TipoEventoSistema;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Factory Method (PE-nuevo): crea la notificación de recordatorio según el tipo de encuesta.
 */
@Component
@RequiredArgsConstructor
public class NotificacionRecordatorioFactory {

    private final TutorEmpresarialRepository tutorRepository;
    private final JdbcTemplate jdbcTemplate;

    public NotificacionRecordatorio crear(
            InstanciaPractica practica,
            TipoEncuesta tipoEncuesta,
            EstadoEncuesta estadoEncuesta) {

        if (estadoEncuesta == EstadoEncuesta.COMPLETADA) {
            throw new IllegalStateException("No se envía recordatorio a una encuesta completada.");
        }

        Destinatario destinatario = resolverDestinatario(practica, tipoEncuesta);
        if (destinatario.correo().isBlank()) {
            throw new IllegalStateException("No hay destinatario de correo para la encuesta " + tipoEncuesta);
        }

        return new NotificacionRecordatorio(
                TipoEventoSistema.RECORDATORIO_ENCUESTA,
                practica.getId(),
                tipoEncuesta,
                destinatario.usuarioId(),
                destinatario.correo(),
                destinatario.nombre()
        );
    }

    private Destinatario resolverDestinatario(InstanciaPractica practica, TipoEncuesta tipoEncuesta) {
        if (tipoEncuesta == TipoEncuesta.TUTOR) {
            if (practica.getTutorId() == null) {
                return Destinatario.vacio();
            }
            return tutorRepository.findById(practica.getTutorId())
                    .map(tutor -> new Destinatario(tutor.getCorreo(), tutor.getNombre(), tutor.getUsuarioId()))
                    .orElse(Destinatario.vacio());
        }

        if (practica.getExpediente() == null || practica.getExpediente().getEstudiante() == null) {
            return Destinatario.vacio();
        }

        Estudiante estudiante = practica.getExpediente().getEstudiante();
        Long usuarioId = null;
        try {
            usuarioId = jdbcTemplate.queryForObject(
                    "SELECT usuario_id FROM estudiantes WHERE id = ?",
                    Long.class,
                    estudiante.getId()
            );
        } catch (Exception ignored) {
            // Sin usuario vinculado
        }
        return new Destinatario(estudiante.getCorreo(), estudiante.getNombre(), usuarioId);
    }

    private record Destinatario(String correo, String nombre, Long usuarioId) {
        static Destinatario vacio() {
            return new Destinatario("", "", null);
        }
    }
}
