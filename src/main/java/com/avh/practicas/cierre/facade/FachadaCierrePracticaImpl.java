package com.avh.practicas.cierre.facade;

import com.avh.practicas.calificacion.entity.NotaFinal;
import com.avh.practicas.calificacion.repository.NotaFinalRepository;
import com.avh.practicas.calificacion.service.CalificacionService;
import com.avh.practicas.cierre.checklist.ChecklistCierreFabrica;
import com.avh.practicas.cierre.checklist.composite.ChecklistCierre;
import com.avh.practicas.cierre.checklist.dto.ResumenChecklist;
import com.avh.practicas.cierre.checklist.leaf.ItemEncuesta;
import com.avh.practicas.cierre.dto.CierrePracticaResponse;
import com.avh.practicas.cierre.entity.TipoEncuesta;
import com.avh.practicas.cierre.exception.CierreNoPermitidoException;
import com.avh.practicas.cierre.state.practica.PracticaContext;
import com.avh.practicas.cierre.support.DocumentoProxyActivador;
import com.avh.practicas.empresa.entity.TutorEmpresarial;
import com.avh.practicas.empresa.repository.TutorEmpresarialRepository;
import com.avh.practicas.estudiante.entity.DocenteAsesor;
import com.avh.practicas.estudiante.entity.EstadoPractica;
import com.avh.practicas.estudiante.entity.Estudiante;
import com.avh.practicas.estudiante.entity.InstanciaPractica;
import com.avh.practicas.estudiante.repository.DocenteAsesorRepository;
import com.avh.practicas.estudiante.repository.InstanciaPracticaRepository;
import com.avh.practicas.shared.evento.EventoSistema;
import com.avh.practicas.shared.evento.NotificadorEventos;
import com.avh.practicas.shared.evento.TipoEventoSistema;
import com.avh.practicas.shared.exception.RecursoNoEncontradoException;
import com.avh.practicas.shared.pattern.singleton.GestorConfiguracion;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Facade de cierre de práctica (PE-43): orquesta checklist, calificación, archivo y notificaciones.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FachadaCierrePracticaImpl implements FachadaCierrePractica {

    private final ChecklistCierreFabrica checklistFabrica;
    private final CalificacionService calificacionService;
    private final NotaFinalRepository notaFinalRepository;
    private final InstanciaPracticaRepository practicaRepository;
    private final DocumentoProxyActivador documentoProxyActivador;
    private final NotificadorEventos notificadorEventos;
    private final TutorEmpresarialRepository tutorRepository;
    private final DocenteAsesorRepository docenteRepository;

    @Value("${cierre.coord-academica-correo:}")
    private String correoCoordAcademica;

    @Override
    @Transactional(readOnly = true)
    public ResumenChecklist verificarChecklist(Long practicaId) {
        return checklistFabrica.construir(practicaId).getResumen();
    }

    @Override
    @Transactional
    public CierrePracticaResponse ejecutarCierre(Long practicaId, Long coordinadorId) {
        ChecklistCierre checklist = checklistFabrica.construir(practicaId);
        if (!checklist.habilitarBotonCierre()) {
            ResumenChecklist resumen = checklist.getResumen();
            throw new CierreNoPermitidoException(
                    "Checklist incompleto: " + resumen.itemsPendientes() + " ítem(s) pendiente(s). "
                            + "Progreso global: " + Math.round(resumen.progresoGlobal() * 100) + "%");
        }

        InstanciaPractica practica = practicaRepository.findById(practicaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Práctica no encontrada: " + practicaId));

        if (Boolean.TRUE.equals(practica.getInmutable())) {
            throw new CierreNoPermitidoException("La práctica ya fue cerrada previamente.");
        }

        NotaFinal notaFinal = calificacionService.leerNotaFinal(practicaId);
        boolean aprobada = determinarResultado(notaFinal.getNotaFinal());
        String resultado = aprobada ? "APROBADA" : "REPROBADA";

        archivarExpediente(practica);
        documentoProxyActivador.activarParaPractica(practicaId);
        actualizarEstadoPractica(practica, aprobada);
        practicaRepository.save(practica);

        notaFinal.setAprobada(aprobada);
        notaFinalRepository.save(notaFinal);

        notificarActores(practica, coordinadorId, resultado, notaFinal.getNotaFinal());
        if (aprobada) {
            notificarCoordAcademica(practica, coordinadorId, resultado, notaFinal.getNotaFinal());
        }

        log.info("Cierre ejecutado para práctica {} — resultado {}", practicaId, resultado);

        return new CierrePracticaResponse(
                practicaId,
                practica.getEstado(),
                notaFinal.getNotaFinal(),
                aprobada,
                resultado
        );
    }

    @Override
    @Transactional
    public void enviarRecordatorioEncuesta(Long practicaId, TipoEncuesta tipo) {
        ChecklistCierre checklist = checklistFabrica.construir(practicaId);
        checklist.buscarItemEncuesta(tipo).enviarRecordatorio();
    }

    private boolean determinarResultado(Double notaFinal) {
        double notaMinima = GestorConfiguracion.getInstancia().getNotaMinimaAprobacion();
        return notaFinal != null && notaFinal >= notaMinima;
    }

    private void archivarExpediente(InstanciaPractica practica) {
        practica.setInmutable(true);
        practicaRepository.save(practica);
    }

    private void actualizarEstadoPractica(InstanciaPractica practica, boolean aprobada) {
        PracticaContext context = new PracticaContext(practica);
        context.actualizarPorCierre(practica, aprobada);
    }

    private void notificarActores(
            InstanciaPractica practica,
            Long coordinadorId,
            String resultado,
            Double notaFinal) {

        Estudiante estudiante = practica.getExpediente() != null
                ? practica.getExpediente().getEstudiante()
                : null;
        String nombreEstudiante = estudiante != null ? estudiante.getNombre() : "Estudiante";

        for (String correo : correosActores(practica, estudiante)) {
            Map<String, Object> datos = new HashMap<>();
            datos.put("correo", correo);
            datos.put("nombre_estudiante", nombreEstudiante);
            datos.put("resultado", resultado);
            datos.put("nota_final", notaFinal);

            notificadorEventos.notificar(EventoSistema.crear(
                    TipoEventoSistema.PRACTICA_CERRADA,
                    coordinadorId,
                    "cierre",
                    practica.getId(),
                    datos
            ));
        }
    }

    private void notificarCoordAcademica(
            InstanciaPractica practica,
            Long coordinadorId,
            String resultado,
            Double notaFinal) {

        Estudiante estudiante = practica.getExpediente() != null
                ? practica.getExpediente().getEstudiante()
                : null;

        Map<String, Object> datos = new HashMap<>();
        datos.put("correo", correoCoordAcademica);
        datos.put("nombre_estudiante", estudiante != null ? estudiante.getNombre() : "Estudiante");
        datos.put("resultado", resultado);
        datos.put("nota_final", notaFinal);
        datos.put("practica_id", practica.getId());

        notificadorEventos.notificar(EventoSistema.crear(
                TipoEventoSistema.PRACTICA_COMPLETADA,
                coordinadorId,
                "cierre",
                practica.getId(),
                datos
        ));
    }

    private List<String> correosActores(InstanciaPractica practica, Estudiante estudiante) {
        List<String> correos = new ArrayList<>();

        if (estudiante != null && estudiante.getCorreo() != null && !estudiante.getCorreo().isBlank()) {
            correos.add(estudiante.getCorreo());
        }

        if (practica.getTutorId() != null) {
            tutorRepository.findById(practica.getTutorId())
                    .map(TutorEmpresarial::getCorreo)
                    .filter(c -> !c.isBlank())
                    .ifPresent(correos::add);
        }

        if (practica.getDocenteAsesorId() != null) {
            docenteRepository.findById(practica.getDocenteAsesorId())
                    .map(DocenteAsesor::getCorreo)
                    .filter(c -> !c.isBlank())
                    .ifPresent(correos::add);
        }

        if (correos.isEmpty()) {
            log.warn("FachadaCierrePracticaImpl: sin destinatarios válidos para notificar cierre de práctica id={}, no se enviará correo",
                    practica.getId());
        }

        return correos;
    }
}
