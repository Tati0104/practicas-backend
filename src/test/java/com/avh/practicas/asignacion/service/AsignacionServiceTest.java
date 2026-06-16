package com.avh.practicas.asignacion.service;

import com.avh.practicas.asignacion.dto.AsignacionRequest;
import com.avh.practicas.asignacion.dto.AsignacionResponse;
import com.avh.practicas.asignacion.entity.Asignacion;
import com.avh.practicas.asignacion.entity.EstadoAsignacion;
import com.avh.practicas.asignacion.entity.HistorialAsignacion;
import com.avh.practicas.asignacion.repository.AsignacionRepository;
import com.avh.practicas.asignacion.repository.HistorialAsignacionRepository;
import com.avh.practicas.bitacora.service.BitacoraService;
import com.avh.practicas.configuracion.entity.Programa;
import com.avh.practicas.estudiante.entity.EstadoAptitud;
import com.avh.practicas.estudiante.entity.Estudiante;
import com.avh.practicas.estudiante.repository.EstudianteRepository;
import com.avh.practicas.shared.evento.EventoSistema;
import com.avh.practicas.shared.evento.NotificadorEventos;
import com.avh.practicas.shared.exception.NegocioException;
import com.avh.practicas.vacante.dto.VacanteResponse;
import com.avh.practicas.vacante.entity.EstadoVacanteEnum;
import com.avh.practicas.vacante.entity.Vacante;
import com.avh.practicas.vacante.repository.VacanteRepository;
import com.avh.practicas.vacante.service.VacanteResponseMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AsignacionServiceTest {

    @Mock
    private AsignacionRepository asignacionRepository;
    @Mock
    private HistorialAsignacionRepository historialRepository;
    @Mock
    private EstudianteRepository estudianteRepository;
    @Mock
    private VacanteRepository vacanteRepository;
    @Mock
    private VacanteResponseMapper vacanteResponseMapper;
    @Mock
    private AsignacionResponseMapper asignacionResponseMapper;
    @Mock
    private BitacoraService bitacoraService;
    @Mock
    private NotificadorEventos notificadorEventos;

    @InjectMocks
    private AsignacionService service;

    @Test
    void asignar_requestValido_creaAsignacionConEstadoAsignada() {
        Estudiante estudiante = estudiante(EstadoAptitud.APTO, 1L);
        Vacante vacante = vacante(EstadoVacanteEnum.ACTIVA, 2);
        AsignacionResponse esperado = response(EstadoAsignacion.ASIGNADA);
        when(estudianteRepository.findById(10L)).thenReturn(Optional.of(estudiante));
        when(vacanteRepository.findById(20L)).thenReturn(Optional.of(vacante));
        when(asignacionRepository.existsByEstudianteIdAndEstadoIn(eq(10L), any())).thenReturn(false);
        when(asignacionRepository.save(any(Asignacion.class))).thenAnswer(invocation -> {
            Asignacion asignacion = invocation.getArgument(0);
            asignacion.setId(100L);
            return asignacion;
        });
        when(vacanteResponseMapper.toResponse(vacante)).thenReturn(vacanteResponse());
        when(asignacionResponseMapper.toResponse(any(Asignacion.class))).thenReturn(esperado);

        AsignacionResponse response = service.asignar(new AsignacionRequest(5L, 10L, 20L, "Perfil compatible"));

        assertAll(
                () -> assertSame(esperado, response),
                () -> assertEquals(1, vacante.getCuposDisponibles()),
                () -> verify(historialRepository).save(any(HistorialAsignacion.class)),
                () -> verify(notificadorEventos).notificar(any(EventoSistema.class))
        );
    }

    @Test
    void asignar_estudianteNoApto_lanzaNegocioException() {
        when(estudianteRepository.findById(10L)).thenReturn(Optional.of(estudiante(EstadoAptitud.NO_APTO, 1L)));
        when(vacanteRepository.findById(20L)).thenReturn(Optional.of(vacante(EstadoVacanteEnum.ACTIVA, 2)));

        assertThrows(NegocioException.class, () -> service.asignar(new AsignacionRequest(5L, 10L, 20L, null)));
        verify(asignacionRepository, never()).save(any());
    }

    @Test
    void asignar_estudianteConAsignacionActiva_lanzaNegocioException() {
        when(estudianteRepository.findById(10L)).thenReturn(Optional.of(estudiante(EstadoAptitud.APTO, 1L)));
        when(vacanteRepository.findById(20L)).thenReturn(Optional.of(vacante(EstadoVacanteEnum.ACTIVA, 2)));
        when(asignacionRepository.existsByEstudianteIdAndEstadoIn(eq(10L), any())).thenReturn(true);

        assertThrows(NegocioException.class, () -> service.asignar(new AsignacionRequest(5L, 10L, 20L, null)));
        verify(asignacionRepository, never()).save(any());
    }

    @Test
    void iniciarVinculacion_asignada_transicionaYRegistraHistorial() {
        Asignacion asignacion = asignacion(EstadoAsignacion.ASIGNADA);
        AsignacionResponse esperado = response(EstadoAsignacion.EN_PROCESO_VINCULACION);
        prepararNotificacion(asignacion);
        when(asignacionRepository.findById(1L)).thenReturn(Optional.of(asignacion));
        when(asignacionRepository.save(asignacion)).thenReturn(asignacion);
        when(asignacionResponseMapper.toResponse(asignacion)).thenReturn(esperado);

        AsignacionResponse response = service.iniciarVinculacion(1L, 5L, "Inicio documental");

        assertAll(
                () -> assertSame(esperado, response),
                () -> assertEquals(EstadoAsignacion.EN_PROCESO_VINCULACION, asignacion.getEstado()),
                () -> verify(historialRepository).save(any(HistorialAsignacion.class))
        );
    }

    @Test
    void cancelar_asignada_liberaCupoDeVacante() {
        Asignacion asignacion = asignacion(EstadoAsignacion.ASIGNADA);
        Vacante vacante = vacante(EstadoVacanteEnum.ACTIVA, 1);
        AsignacionResponse esperado = response(EstadoAsignacion.CANCELADA);
        when(asignacionRepository.findById(1L)).thenReturn(Optional.of(asignacion));
        when(vacanteRepository.findById(20L)).thenReturn(Optional.of(vacante));
        when(estudianteRepository.findById(10L)).thenReturn(Optional.of(estudiante(EstadoAptitud.APTO, 1L)));
        when(vacanteRepository.save(vacante)).thenReturn(vacante);
        when(asignacionRepository.save(asignacion)).thenReturn(asignacion);
        when(vacanteResponseMapper.toResponse(vacante)).thenReturn(vacanteResponse());
        when(asignacionResponseMapper.toResponse(asignacion)).thenReturn(esperado);

        AsignacionResponse response = service.cancelar(1L, 5L, "Renuncia");

        assertAll(
                () -> assertSame(esperado, response),
                () -> assertEquals(EstadoAsignacion.CANCELADA, asignacion.getEstado()),
                () -> assertEquals(2, vacante.getCuposDisponibles())
        );
    }

    private void prepararNotificacion(Asignacion asignacion) {
        when(estudianteRepository.findById(asignacion.getEstudianteId())).thenReturn(Optional.of(estudiante(EstadoAptitud.APTO, 1L)));
        when(vacanteRepository.findById(asignacion.getVacanteId())).thenReturn(Optional.of(vacante(EstadoVacanteEnum.ACTIVA, 2)));
        when(vacanteResponseMapper.toResponse(any(Vacante.class))).thenReturn(vacanteResponse());
    }

    private Asignacion asignacion(EstadoAsignacion estado) {
        return Asignacion.builder()
                .id(1L)
                .estudianteId(10L)
                .vacanteId(20L)
                .coordinadorId(5L)
                .estado(estado)
                .build();
    }

    private Estudiante estudiante(EstadoAptitud estadoAptitud, Long programaId) {
        return Estudiante.builder()
                .id(10L)
                .nombre("Ana Perez")
                .correo("ana@test.com")
                .programa(Programa.builder().id(programaId).nombre("Sistemas").build())
                .estadoAptitud(estadoAptitud)
                .build();
    }

    private Vacante vacante(EstadoVacanteEnum estado, Integer cuposDisponibles) {
        return Vacante.builder()
                .id(20L)
                .empresaId(30L)
                .programaId(1L)
                .cargo("Practicante")
                .cuposTotales(2)
                .cuposDisponibles(cuposDisponibles)
                .estado(estado)
                .build();
    }

    private AsignacionResponse response(EstadoAsignacion estado) {
        return new AsignacionResponse(
                1L,
                10L,
                20L,
                5L,
                estado,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    private VacanteResponse vacanteResponse() {
        return new VacanteResponse(
                20L,
                30L,
                "Empresa SAS",
                1L,
                "Sistemas",
                null,
                null,
                "Practicante",
                "Perfil",
                "Requisitos",
                "Remoto",
                "TI",
                2,
                0,
                2,
                EstadoVacanteEnum.ACTIVA,
                "VERDE",
                null,
                LocalDate.now(),
                LocalDate.now().plusMonths(1)
        );
    }
}
