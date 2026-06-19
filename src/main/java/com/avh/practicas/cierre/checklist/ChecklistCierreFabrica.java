package com.avh.practicas.cierre.checklist;

import com.avh.practicas.calificacion.repository.NotaDocenteRepository;
import com.avh.practicas.calificacion.repository.NotaFinalRepository;
import com.avh.practicas.calificacion.repository.NotaTutorRepository;
import com.avh.practicas.cierre.checklist.composite.ChecklistCierre;
import com.avh.practicas.cierre.checklist.composite.GrupoRequisitos;
import com.avh.practicas.cierre.checklist.leaf.*;
import com.avh.practicas.cierre.entity.Encuesta;
import com.avh.practicas.cierre.entity.TipoEncuesta;
import com.avh.practicas.cierre.notificacion.NotificacionRecordatorioDispatcher;
import com.avh.practicas.cierre.notificacion.NotificacionRecordatorioFactory;
import com.avh.practicas.cierre.service.EncuestaService;
import com.avh.practicas.estudiante.entity.InstanciaPractica;
import com.avh.practicas.estudiante.repository.InstanciaPracticaRepository;
import com.avh.practicas.shared.exception.RecursoNoEncontradoException;
import com.avh.practicas.vinculacion.entity.CategoriaDocumento;
import com.avh.practicas.vinculacion.repository.DocumentoPracticaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Construye el árbol Composite del checklist de cierre para una práctica.
 */
@Component
@RequiredArgsConstructor
public class ChecklistCierreFabrica {

    private final InstanciaPracticaRepository practicaRepository;
    private final NotaDocenteRepository notaDocenteRepository;
    private final NotaTutorRepository notaTutorRepository;
    private final NotaFinalRepository notaFinalRepository;
    private final DocumentoPracticaRepository documentoPracticaRepository;
    private final EncuestaService encuestaService;
    private final NotificacionRecordatorioFactory recordatorioFactory;
    private final NotificacionRecordatorioDispatcher recordatorioDispatcher;

    public ChecklistCierre construir(Long practicaId) {
        InstanciaPractica practica = practicaRepository.findById(practicaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Práctica no encontrada: " + practicaId));

        int numCortes = practica.getNumCortes() != null ? practica.getNumCortes() : 0;
        int notasDocente = notaDocenteRepository.findByInstanciaPracticaId(practicaId).size();
        int notasTutor = notaTutorRepository.findByInstanciaPracticaId(practicaId).size();
        boolean tieneNotaFinal = notaFinalRepository.findByInstanciaPracticaId(practicaId).isPresent();

        List<GrupoRequisitos> grupos = new ArrayList<>();

        grupos.add(new GrupoRequisitos(
                "Calificaciones",
                true,
                List.of(
                        new ItemNotaDocente(numCortes, notasDocente),
                        new ItemNotaTutor(numCortes, notasTutor),
                        new ItemNotaFinal(tieneNotaFinal)
                )
        ));

        grupos.add(new GrupoRequisitos(
                "Documentación de vinculación",
                true,
                List.of(
                        new ItemDocumentoCargado(
                                "Carta de presentación",
                                true,
                                tieneDocumento(practicaId, CategoriaDocumento.CARTA_PRESENTACION)
                        ),
                        new ItemDocumentoCargado(
                                "Convenio firmado",
                                true,
                                tieneDocumento(practicaId, CategoriaDocumento.CONVENIO_PRACTICA)
                        )
                )
        ));

        grupos.add(new GrupoRequisitos(
                "Encuestas de cierre",
                true,
                List.of(
                        crearItemEncuesta(practica, TipoEncuesta.TUTOR),
                        crearItemEncuesta(practica, TipoEncuesta.ESTUDIANTE)
                )
        ));

        return new ChecklistCierre(practicaId, grupos);
    }

    private ItemEncuesta crearItemEncuesta(InstanciaPractica practica, TipoEncuesta tipo) {
        Long practicaId = practica.getId();
        Encuesta encuesta = encuestaService.obtenerPorPracticaYTipo(practicaId, tipo).orElse(null);

        return new ItemEncuesta(
                practicaId,
                practica,
                tipo,
                true,
                encuesta,
                encuestaService,
                recordatorioFactory,
                recordatorioDispatcher
        );
    }

    private boolean tieneDocumento(Long practicaId, CategoriaDocumento categoria) {
        return !documentoPracticaRepository
                .findByInstanciaPracticaIdAndCategoriaOrderByFechaDesc(practicaId, categoria)
                .isEmpty();
    }
}
