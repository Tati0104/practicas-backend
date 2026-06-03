package com.avh.practicas.vinculacion.mediator;

import com.avh.practicas.correo.service.IMailService;
import com.avh.practicas.correo.service.ObservadorCorreo;
import com.avh.practicas.empresa.entity.Empresa;
import com.avh.practicas.empresa.entity.TutorEmpresarial;
import com.avh.practicas.estudiante.entity.DocenteAsesor;
import com.avh.practicas.estudiante.entity.Estudiante;
import com.avh.practicas.estudiante.entity.InstanciaPractica;
import com.avh.practicas.shared.evento.EventoSistema;
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

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MediadorVinculacionImpl implements MediadorVinculacion {

    private final ServicioEstudiantes servicioEstudiantes;
    private final ServicioDocente servicioDocente;
    private final IMailService mailService;
    private final ServicioBitacora servicioBitacora;
    private final ObservadorCorreo observadorCorreo;

    @Override
    public void notificar(String evento, Map<String, Object> datos) {
        log.info("MediadorVinculacion — evento: {} | datos: {}", evento, datos);
    }

    @Override
    @Transactional
    public void confirmarVinculacion(Long practicaId) {
        notificar("VINCULACION_INICIADA", Map.of("practicaId", practicaId));

        InstanciaPractica practica = servicioEstudiantes.activarPractica(practicaId);
        servicioDocente.asignarDocenteAsesor(practicaId);

        ContextoVinculacion contexto = servicioEstudiantes.cargarContexto(practicaId);

        notificarEstudiante(contexto);
        notificarTutorEmpresarial(contexto);
        notificarDocenteAsesor(contexto);

        servicioEstudiantes.activarTableroSeguimiento(practicaId);

        servicioBitacora.registrarVinculacionConfirmada(
                practicaId,
                null,
                "Vinculación confirmada. Práctica " + practica.getNombre()
                        + " activada en estado EN_CURSO (EN_PRACTICA)."
        );

        notificar("VINCULACION_CONFIRMADA", Map.of(
                "practicaId", practicaId,
                "estado", practica.getEstado().name()
        ));
    }

    private void notificarEstudiante(ContextoVinculacion contexto) {
        Estudiante estudiante = contexto.getEstudiante();
        Empresa empresa = contexto.getEmpresa();

        EventoSistema evento = EventoSistema.crear(
                TipoEventoSistema.VINCULACION_CONFIRMADA,
                null,
                "VINCULACION",
                contexto.getPractica().getId(),
                Map.of(
                        "correo", estudiante.getCorreo(),
                        "nombre_estudiante", estudiante.getNombre(),
                        "empresa", empresa.getRazonSocial()
                )
        );

        observadorCorreo.actualizar(evento);
    }

    private void notificarTutorEmpresarial(ContextoVinculacion contexto) {
        TutorEmpresarial tutor = contexto.getTutor();
        Estudiante estudiante = contexto.getEstudiante();
        Empresa empresa = contexto.getEmpresa();

        String asunto = "Nueva práctica vinculada — " + estudiante.getNombre();
        String cuerpo = "<p>Hola " + tutor.getNombre() + ",</p>"
                + "<p>Se confirmó la vinculación del estudiante <b>" + estudiante.getNombre()
                + "</b> con <b>" + empresa.getRazonSocial() + "</b>.</p>";

        enviarCorreoObligatorio(tutor.getCorreo(), asunto, cuerpo);
    }

    private void notificarDocenteAsesor(ContextoVinculacion contexto) {
        DocenteAsesor docente = contexto.getDocenteAsesor();
        if (docente == null) {
            throw new NegocioException("No se pudo notificar al docente asesor: no está asignado a la práctica.");
        }

        Estudiante estudiante = contexto.getEstudiante();
        Empresa empresa = contexto.getEmpresa();

        String asunto = "Nuevo estudiante en práctica — " + estudiante.getNombre();
        String cuerpo = "<p>Hola " + docente.getNombre() + ",</p>"
                + "<p>Quedaste asignado como docente asesor de <b>" + estudiante.getNombre()
                + "</b> en <b>" + empresa.getRazonSocial() + "</b>.</p>";

        enviarCorreoObligatorio(docente.getCorreo(), asunto, cuerpo);
    }

    private void enviarCorreoObligatorio(String destinatario, String asunto, String cuerpo) {
        boolean enviado = mailService.enviar(destinatario, asunto, cuerpo);
        if (!enviado) {
            throw new NegocioException("No se pudo enviar el correo a: " + destinatario);
        }
    }
}
