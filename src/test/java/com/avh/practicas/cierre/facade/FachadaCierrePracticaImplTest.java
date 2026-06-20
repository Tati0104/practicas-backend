package com.avh.practicas.cierre.facade;

import com.avh.practicas.calificacion.entity.NotaFinal;
import com.avh.practicas.calificacion.repository.NotaFinalRepository;
import com.avh.practicas.calificacion.service.CalificacionService;
import com.avh.practicas.cierre.checklist.ChecklistCierreFabrica;
import com.avh.practicas.cierre.checklist.composite.ChecklistCierre;
import com.avh.practicas.cierre.checklist.composite.GrupoRequisitos;
import com.avh.practicas.cierre.checklist.leaf.ItemNotaFinal;
import com.avh.practicas.cierre.exception.CierreNoPermitidoException;
import com.avh.practicas.cierre.service.EncuestaService;
import com.avh.practicas.cierre.support.DocumentoProxyActivador;
import com.avh.practicas.empresa.repository.TutorEmpresarialRepository;
import com.avh.practicas.estudiante.entity.EstadoPractica;
import com.avh.practicas.estudiante.entity.Expediente;
import com.avh.practicas.estudiante.entity.Estudiante;
import com.avh.practicas.estudiante.entity.InstanciaPractica;
import com.avh.practicas.estudiante.repository.DocenteAsesorRepository;
import com.avh.practicas.estudiante.repository.InstanciaPracticaRepository;
import com.avh.practicas.shared.evento.NotificadorEventos;
import com.avh.practicas.shared.scope.ScopePracticaResolver;
import com.avh.practicas.shared.scope.ScopePracticas;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FachadaCierrePracticaImplTest {

    @Mock
    private ChecklistCierreFabrica checklistFabrica;
    @Mock
    private CalificacionService calificacionService;
    @Mock
    private InstanciaPracticaRepository practicaRepository;
    @Mock
    private NotaFinalRepository notaFinalRepository;
    @Mock
    private DocumentoProxyActivador documentoProxyActivador;
    @Mock
    private NotificadorEventos notificadorEventos;
    @Mock
    private TutorEmpresarialRepository tutorRepository;
    @Mock
    private DocenteAsesorRepository docenteRepository;
    @Mock
    private ScopePracticaResolver scopePracticaResolver;
    @Mock
    private ScopePracticas scopePracticas;
    @Mock
    private EncuestaService encuestaService;

    private FachadaCierrePracticaImpl fachada;

    @BeforeEach
    void setUp() {
        fachada = new FachadaCierrePracticaImpl(
                checklistFabrica,
                calificacionService,
                notaFinalRepository,
                practicaRepository,
                documentoProxyActivador,
                notificadorEventos,
                tutorRepository,
                docenteRepository,
                scopePracticaResolver,
                encuestaService
        );
        lenient().when(scopePracticaResolver.resolver()).thenReturn(scopePracticas);
        lenient().when(scopePracticas.esVisible(any(InstanciaPractica.class))).thenReturn(true);
    }

    @Test
    void ejecutarCierre_fallaSiChecklistIncompleto() {
        ChecklistCierre checklist = new ChecklistCierre(1L, List.of(
                new GrupoRequisitos("G", true, List.of(new ItemNotaFinal(false)))
        ));
        when(checklistFabrica.construir(1L)).thenReturn(checklist);

        assertThrows(CierreNoPermitidoException.class, () -> fachada.ejecutarCierre(1L, 99L));
    }

    @Test
    void ejecutarCierre_exitosoMarcaCompletada() {
        Long practicaId = 1L;
        Estudiante estudiante = Estudiante.builder().nombre("Ana").correo("ana@demo.com").build();
        Expediente expediente = Expediente.builder().estudiante(estudiante).build();
        InstanciaPractica practica = InstanciaPractica.builder()
                .id(practicaId)
                .expediente(expediente)
                .estado(EstadoPractica.EN_CURSO)
                .inmutable(false)
                .build();

        ChecklistCierre checklist = new ChecklistCierre(practicaId, List.of(
                new GrupoRequisitos("G", true, List.of(new ItemNotaFinal(true)))
        ));

        NotaFinal notaFinal = NotaFinal.builder()
                .instanciaPractica(practica)
                .notaFinal(4.5)
                .aprobada(false)
                .build();

        when(checklistFabrica.construir(practicaId)).thenReturn(checklist);
        when(practicaRepository.findById(practicaId)).thenReturn(Optional.of(practica));
        when(calificacionService.leerNotaFinal(practicaId)).thenReturn(notaFinal);
        when(practicaRepository.save(any(InstanciaPractica.class))).thenAnswer(inv -> inv.getArgument(0));
        when(notaFinalRepository.save(any(NotaFinal.class))).thenAnswer(inv -> inv.getArgument(0));

        var response = fachada.ejecutarCierre(practicaId, 10L);

        assertEquals(EstadoPractica.COMPLETADA, response.estado());
        assertTrue(response.aprobada());
        verify(documentoProxyActivador).activarParaPractica(practicaId);
        verify(notificadorEventos, atLeastOnce()).notificar(any());
    }
}
