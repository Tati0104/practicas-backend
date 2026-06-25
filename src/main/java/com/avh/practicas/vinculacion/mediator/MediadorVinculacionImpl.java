package com.avh.practicas.vinculacion.mediator;

import com.avh.practicas.correo.service.IMailService;
import com.avh.practicas.empresa.entity.Empresa;
import com.avh.practicas.empresa.entity.TutorEmpresarial;
import com.avh.practicas.estudiante.entity.DocenteAsesor;
import com.avh.practicas.estudiante.entity.Estudiante;
import com.avh.practicas.estudiante.entity.InstanciaPractica;
import com.avh.practicas.shared.evento.EventoSistema;
import com.avh.practicas.shared.evento.NotificadorEventos;
import com.avh.practicas.shared.evento.TipoEventoSistema;
import com.avh.practicas.shared.exception.NegocioException;
import com.avh.practicas.vinculacion.dto.ContextoVinculacion;
import com.avh.practicas.vinculacion.service.ServicioBitacora;
import com.avh.practicas.vinculacion.service.ServicioDocente;
import com.avh.practicas.vinculacion.service.ServicioEstudiantes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MediadorVinculacionImpl implements MediadorVinculacion {

    private final ServicioEstudiantes servicioEstudiantes;
    private final ServicioDocente servicioDocente;
    private final IMailService mailService;
    private final ServicioBitacora servicioBitacora;
    private final NotificadorEventos notificadorEventos;

    @Override
    public void notificar(String evento, Map<String, Object> datos) {
        log.info("MediadorVinculacion — evento: {} | datos: {}", evento, datos);
    }

    @Override
    @Transactional
    public void confirmarVinculacion(Long practicaId) {
        confirmarVinculacion(practicaId, LocalDate.now(), LocalDate.now().plusMonths(6));
    }

    @Override
    @Transactional
    public void confirmarVinculacion(Long practicaId, LocalDate fechaInicio, LocalDate fechaFin) {
        notificar("VINCULACION_INICIADA", Map.of("practicaId", practicaId));

        InstanciaPractica practica = servicioEstudiantes.activarPractica(practicaId, fechaInicio, fechaFin);
        servicioDocente.asignarDocenteAsesor(practicaId);

        ContextoVinculacion contexto = servicioEstudiantes.cargarContexto(practicaId);

        notificarEstudiante(contexto);
        notificarTutorEmpresarial(contexto);
        notificarDocenteAsesor(contexto);

        servicioEstudiantes.activarTableroSeguimiento(practicaId);

        servicioBitacora.registrarVinculacionConfirmada(
                practicaId,
                null,
                "Vinculación confirmada para práctica " + practica.getNombre()
        );

        notificar("VINCULACION_CONFIRMADA", Map.of(
                "practicaId", practicaId,
                "estado", practica.getEstado().name()
        ));
    }

    private void notificarEstudiante(ContextoVinculacion contexto) {
        Estudiante estudiante = contexto.getEstudiante();
        Empresa empresa = contexto.getEmpresa();

        notificadorEventos.notificar(EventoSistema.crear(
                TipoEventoSistema.VINCULACION_CONFIRMADA,
                null,
                "VINCULACION",
                contexto.getPractica().getId(),
                Map.of(
                        "correo", estudiante.getCorreo(),
                        "nombre_estudiante", estudiante.getNombre(),
                        "empresa", empresa.getRazonSocial()
                )
        ));
    }

    private void notificarTutorEmpresarial(ContextoVinculacion contexto) {
        TutorEmpresarial tutor = contexto.getTutor();
        Estudiante estudiante = contexto.getEstudiante();
        Empresa empresa = contexto.getEmpresa();

        enviarCorreoNotificacion(
                tutor.getCorreo(),
                "Nueva práctica vinculada — " + estudiante.getNombre(),
                "<p>Hola " + tutor.getNombre() + ",</p><p>Se confirmó la vinculación de <b>"
                        + estudiante.getNombre() + "</b> con <b>" + empresa.getRazonSocial() + "</b>.</p>"
        );
    }

    private void notificarDocenteAsesor(ContextoVinculacion contexto) {
        DocenteAsesor docente = contexto.getDocenteAsesor();
        if (docente == null) {
            throw new NegocioException("No se pudo notificar al docente asesor: no está asignado.");
        }

        Estudiante estudiante = contexto.getEstudiante();
        Empresa empresa = contexto.getEmpresa();

        enviarCorreoNotificacion(
                docente.getCorreo(),
                "Nuevo estudiante en práctica — " + estudiante.getNombre(),
                "<p>Hola " + docente.getNombre() + ",</p><p>Quedaste asignado como asesor de <b>"
                        + estudiante.getNombre() + "</b> en <b>" + empresa.getRazonSocial() + "</b>.</p>"
        );
    }

    private void enviarCorreoNotificacion(String destinatario, String asunto, String cuerpo) {
        try {
            boolean enviado = mailService.enviar(destinatario, asunto, cuerpo);
            if (!enviado) {
                log.warn("No se pudo enviar el correo de notificación a {}: el servicio de correo no lo confirmó.", destinatario);
            }
        } catch (Exception ex) {
            log.warn("No se pudo enviar el correo de notificación a {}: {}", destinatario, ex.getMessage());
        }
    }
}
