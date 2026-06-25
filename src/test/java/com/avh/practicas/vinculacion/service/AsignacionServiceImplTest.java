package com.avh.practicas.vinculacion.service;

import com.avh.practicas.asignacion.entity.Asignacion;
import com.avh.practicas.asignacion.entity.EstadoAsignacion;
import com.avh.practicas.asignacion.repository.AsignacionRepository;
import com.avh.practicas.empresa.entity.Empresa;
import com.avh.practicas.empresa.entity.TutorEmpresarial;
import com.avh.practicas.empresa.repository.EmpresaRepository;
import com.avh.practicas.empresa.repository.TutorEmpresarialRepository;
import com.avh.practicas.estudiante.entity.Estudiante;
import com.avh.practicas.estudiante.repository.EstudianteRepository;
import com.avh.practicas.shared.exception.NegocioException;
import com.avh.practicas.shared.exception.RecursoNoEncontradoException;
import com.avh.practicas.vacante.entity.Vacante;
import com.avh.practicas.vacante.repository.VacanteRepository;
import com.avh.practicas.vinculacion.dto.AsignacionResponse;
import com.avh.practicas.vinculacion.dto.CancelarAsignacionRequest;
import com.avh.practicas.vinculacion.dto.CrearAsignacionRequest;
import com.avh.practicas.vinculacion.support.AsignacionObservadorRegistry;
import com.avh.practicas.vinculacion.support.AsignacionSubject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AsignacionServiceImplTest {

    @Mock
    private AsignacionRepository asignacionRepository;
    @Mock
    private EstudianteRepository estudianteRepository;
    @Mock
    private VacanteRepository vacanteRepository;
    @Mock
    private EmpresaRepository empresaRepository;
    @Mock
    private TutorEmpresarialRepository tutorRepository;
    @Mock
    private AsignacionObservadorRegistry observadorRegistry;

    @InjectMocks
    private AsignacionServiceImpl service;

    @Test
    void crear_conDatosValidos_guardaAsignacionAsignada() {
        prepararContextoBasico();
        when(asignacionRepository.save(any(Asignacion.class))).thenAnswer(invocation -> {
            Asignacion asignacion = invocation.getArgument(0);
            asignacion.setId(99L);
            return asignacion;
        });

        AsignacionResponse response = service.crear(new CrearAsignacionRequest(20L, 10L));

        assertAll(
                () -> assertEquals(99L, response.id()),
                () -> assertEquals(EstadoAsignacion.ASIGNADA, response.estado()),
                () -> assertEquals(20L, response.vacanteId()),
                () -> assertEquals(10L, response.estudianteId())
        );
    }

    @Test
    void crear_conDatosValidos_registraObservadoresAntesDeNotificar() {
        prepararContextoBasico();
        when(asignacionRepository.save(any(Asignacion.class))).thenAnswer(invocation -> invocation.getArgument(0));
        ArgumentCaptor<AsignacionSubject> captor = ArgumentCaptor.forClass(AsignacionSubject.class);

        service.crear(new CrearAsignacionRequest(20L, 10L));

        verify(observadorRegistry).registrarObservadores(captor.capture());
        assertEquals(EstadoAsignacion.ASIGNADA, captor.getValue().getAsignacion().getEstado());
    }

    @Test
    void crear_estudianteNoExiste_lanzaRecursoNoEncontradoException() {
        when(estudianteRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.crear(new CrearAsignacionRequest(20L, 10L)));
        verify(asignacionRepository, never()).save(any());
    }

    @Test
    void crear_vacanteNoExiste_lanzaRecursoNoEncontradoException() {
        when(estudianteRepository.findById(10L)).thenReturn(Optional.of(estudiante()));
        when(vacanteRepository.findById(20L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.crear(new CrearAsignacionRequest(20L, 10L)));
        verify(asignacionRepository, never()).save(any());
    }

    @Test
    void cambiarEstado_mismoEstado_retornaSinGuardar() {
        Asignacion asignacion = asignacion(EstadoAsignacion.ASIGNADA);
        when(asignacionRepository.findById(1L)).thenReturn(Optional.of(asignacion));

        AsignacionResponse response = service.cambiarEstado(1L, EstadoAsignacion.ASIGNADA);

        assertEquals(EstadoAsignacion.ASIGNADA, response.estado());
        verify(asignacionRepository, never()).save(any());
    }

    @Test
    void cambiarEstado_asignacionCancelada_lanzaNegocioException() {
        when(asignacionRepository.findById(1L)).thenReturn(Optional.of(asignacion(EstadoAsignacion.CANCELADA)));

        assertThrows(NegocioException.class, () -> service.cambiarEstado(1L, EstadoAsignacion.VINCULADA));
        verify(asignacionRepository, never()).save(any());
    }

    @Test
    void cambiarEstado_nuevoEstadoValido_actualizaEstado() {
        prepararContextoBasico();
        Asignacion asignacion = asignacion(EstadoAsignacion.ASIGNADA);
        when(asignacionRepository.findById(1L)).thenReturn(Optional.of(asignacion));
        when(asignacionRepository.save(asignacion)).thenReturn(asignacion);

        AsignacionResponse response = service.cambiarEstado(1L, EstadoAsignacion.EN_PROCESO_VINCULACION);

        assertEquals(EstadoAsignacion.EN_PROCESO_VINCULACION, response.estado());
    }

    @Test
    void cancelar_asignacionValida_actualizaEstadoYMotivo() {
        prepararContextoBasico();
        Asignacion asignacion = asignacion(EstadoAsignacion.EN_PROCESO_VINCULACION);
        when(asignacionRepository.findById(1L)).thenReturn(Optional.of(asignacion));
        when(asignacionRepository.save(asignacion)).thenReturn(asignacion);

        AsignacionResponse response = service.cancelar(1L, new CancelarAsignacionRequest("Sin documentos"));

        assertAll(
                () -> assertEquals(EstadoAsignacion.CANCELADA, response.estado()),
                () -> assertEquals("Sin documentos", response.motivoCancelacion())
        );
    }

    @Test
    void cancelar_asignacionYaCancelada_lanzaNegocioException() {
        when(asignacionRepository.findById(1L)).thenReturn(Optional.of(asignacion(EstadoAsignacion.CANCELADA)));

        assertThrows(NegocioException.class, () -> service.cancelar(1L, new CancelarAsignacionRequest("Duplicado")));
        verify(asignacionRepository, never()).save(any());
    }

    private void prepararContextoBasico() {
        when(estudianteRepository.findById(10L)).thenReturn(Optional.of(estudiante()));
        when(vacanteRepository.findById(20L)).thenReturn(Optional.of(vacante()));
        when(empresaRepository.findById(30L)).thenReturn(Optional.of(empresa()));
        when(tutorRepository.findByEmpresaIdAndActivoTrue(30L)).thenReturn(List.of(tutor()));
    }

    private Asignacion asignacion(EstadoAsignacion estado) {
        return Asignacion.builder()
                .id(1L)
                .estudianteId(10L)
                .vacanteId(20L)
                .estado(estado)
                .build();
    }

    private Estudiante estudiante() {
        return Estudiante.builder()
                .id(10L)
                .nombre("Ana Perez")
                .correo("ana@test.com")
                .build();
    }

    private Vacante vacante() {
        return Vacante.builder()
                .id(20L)
                .empresaId(30L)
                .cargo("Practicante")
                .build();
    }

    private Empresa empresa() {
        return Empresa.builder()
                .id(30L)
                .razonSocial("Empresa SAS")
                .build();
    }

    private TutorEmpresarial tutor() {
        return TutorEmpresarial.builder()
                .id(40L)
                .nombre("Tutor")
                .correo("tutor@test.com")
                .build();
    }
}
