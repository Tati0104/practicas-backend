package com.avh.practicas.vacante.service;

import com.avh.practicas.shared.evento.EventoSistema;
import com.avh.practicas.shared.evento.NotificadorEventos;
import com.avh.practicas.vacante.dto.VacanteRequest;
import com.avh.practicas.vacante.dto.VacanteResponse;
import com.avh.practicas.vacante.entity.EstadoVacanteEnum;
import com.avh.practicas.vacante.entity.Vacante;
import com.avh.practicas.vacante.repository.VacanteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VacanteServiceTest {

    @Mock
    private VacanteRepository repository;
    @Mock
    private NotificadorEventos notificadorEventos;
    @Mock
    private VacanteResponseMapper responseMapper;

    @InjectMocks
    private VacanteService service;

    @Test
    void crear_requestValido_creaVacantePendienteConCuposDisponibles() {
        VacanteResponse response = response(EstadoVacanteEnum.PENDIENTE_APROBACION, 2);
        when(repository.save(any(Vacante.class))).thenAnswer(invocation -> {
            Vacante vacante = invocation.getArgument(0);
            vacante.setId(1L);
            return vacante;
        });
        when(responseMapper.toResponse(any(Vacante.class))).thenReturn(response);

        VacanteResponse result = service.crear(request());

        assertSame(response, result);
        verify(repository).save(argThat(v ->
                v.getEstado() == EstadoVacanteEnum.PENDIENTE_APROBACION
                        && v.getCuposDisponibles().equals(v.getCuposTotales())
                        && v.getEmpresaId().equals(5L)));
        verify(notificadorEventos).notificar(any(EventoSistema.class));
    }

    @Test
    void crear_siNotificacionFalla_retornaResponseIgual() {
        VacanteResponse response = response(EstadoVacanteEnum.PENDIENTE_APROBACION, 2);
        doThrow(new RuntimeException("smtp")).when(notificadorEventos).notificar(any(EventoSistema.class));
        when(repository.save(any(Vacante.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(responseMapper.toResponse(any(Vacante.class))).thenReturn(response);

        assertSame(response, service.crear(request()));
    }

    @Test
    void aprobar_vacantePendiente_transicionaAActivaYAsignaAprobador() {
        Vacante vacante = vacante(EstadoVacanteEnum.PENDIENTE_APROBACION, 2);
        when(repository.findById(1L)).thenReturn(Optional.of(vacante));
        when(repository.save(vacante)).thenReturn(vacante);
        when(responseMapper.toResponse(vacante)).thenReturn(response(EstadoVacanteEnum.ACTIVA, 2));

        VacanteResponse result = service.aprobar(1L, 99L);

        assertEquals(EstadoVacanteEnum.ACTIVA, vacante.getEstado());
        assertEquals(99L, vacante.getAprobadoPorId());
        assertEquals(EstadoVacanteEnum.ACTIVA, result.estado());
    }

    @Test
    void aprobar_vacanteNoExiste_lanzaIllegalArgumentException() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.aprobar(1L, 99L));
    }

    @Test
    void aprobar_vacanteActiva_lanzaIllegalStateException() {
        when(repository.findById(1L)).thenReturn(Optional.of(vacante(EstadoVacanteEnum.ACTIVA, 2)));

        assertThrows(IllegalStateException.class, () -> service.aprobar(1L, 99L));
        verify(repository, never()).save(any());
    }

    @Test
    void rechazar_vacantePendiente_transicionaARechazadaConMotivo() {
        Vacante vacante = vacante(EstadoVacanteEnum.PENDIENTE_APROBACION, 2);
        when(repository.findById(1L)).thenReturn(Optional.of(vacante));
        when(repository.save(vacante)).thenReturn(vacante);
        when(responseMapper.toResponse(vacante)).thenReturn(response(EstadoVacanteEnum.RECHAZADA, 2));

        VacanteResponse result = service.rechazar(1L, "No cumple");

        assertEquals(EstadoVacanteEnum.RECHAZADA, vacante.getEstado());
        assertEquals("No cumple", vacante.getMotivoRechazo());
        assertEquals(EstadoVacanteEnum.RECHAZADA, result.estado());
    }

    @Test
    void rechazar_motivoVacio_lanzaIllegalArgumentException() {
        when(repository.findById(1L)).thenReturn(Optional.of(vacante(EstadoVacanteEnum.PENDIENTE_APROBACION, 2)));

        assertThrows(IllegalArgumentException.class, () -> service.rechazar(1L, " "));
        verify(repository, never()).save(any());
    }

    @Test
    void pausar_vacanteActiva_transicionaAPausada() {
        Vacante vacante = vacante(EstadoVacanteEnum.ACTIVA, 2);
        when(repository.findById(1L)).thenReturn(Optional.of(vacante));
        when(repository.save(vacante)).thenReturn(vacante);
        when(responseMapper.toResponse(vacante)).thenReturn(response(EstadoVacanteEnum.PAUSADA, 2));

        service.pausar(1L);

        assertEquals(EstadoVacanteEnum.PAUSADA, vacante.getEstado());
    }

    @Test
    void pausar_vacantePendiente_lanzaIllegalStateException() {
        when(repository.findById(1L)).thenReturn(Optional.of(vacante(EstadoVacanteEnum.PENDIENTE_APROBACION, 2)));

        assertThrows(IllegalStateException.class, () -> service.pausar(1L));
    }

    @Test
    void reactivar_vacantePausada_transicionaAActiva() {
        Vacante vacante = vacante(EstadoVacanteEnum.PAUSADA, 2);
        when(repository.findById(1L)).thenReturn(Optional.of(vacante));
        when(repository.save(vacante)).thenReturn(vacante);
        when(responseMapper.toResponse(vacante)).thenReturn(response(EstadoVacanteEnum.ACTIVA, 2));

        service.reactivar(1L);

        assertEquals(EstadoVacanteEnum.ACTIVA, vacante.getEstado());
    }

    @Test
    void reactivar_vacanteActiva_lanzaIllegalStateException() {
        when(repository.findById(1L)).thenReturn(Optional.of(vacante(EstadoVacanteEnum.ACTIVA, 2)));

        assertThrows(IllegalStateException.class, () -> service.reactivar(1L));
    }

    @Test
    void cerrar_vacanteActiva_transicionaACerrada() {
        Vacante vacante = vacante(EstadoVacanteEnum.ACTIVA, 2);
        when(repository.findById(1L)).thenReturn(Optional.of(vacante));
        when(repository.save(vacante)).thenReturn(vacante);
        when(responseMapper.toResponse(vacante)).thenReturn(response(EstadoVacanteEnum.CERRADA, 2));

        service.cerrar(1L);

        assertEquals(EstadoVacanteEnum.CERRADA, vacante.getEstado());
    }

    @Test
    void cerrar_vacanteRechazada_lanzaIllegalStateException() {
        when(repository.findById(1L)).thenReturn(Optional.of(vacante(EstadoVacanteEnum.RECHAZADA, 2)));

        assertThrows(IllegalStateException.class, () -> service.cerrar(1L));
    }

    @Test
    void descontarCupo_vacanteActiva_reduceCuposDisponibles() {
        Vacante vacante = vacante(EstadoVacanteEnum.ACTIVA, 2);
        when(repository.findById(1L)).thenReturn(Optional.of(vacante));
        when(repository.save(vacante)).thenReturn(vacante);
        when(responseMapper.toResponse(vacante)).thenReturn(response(EstadoVacanteEnum.ACTIVA, 1));

        service.descontarCupo(1L);

        assertEquals(1, vacante.getCuposDisponibles());
        assertEquals(EstadoVacanteEnum.ACTIVA, vacante.getEstado());
    }

    @Test
    void descontarCupo_ultimoCupo_transicionaACuposCompletosYNotifica() {
        Vacante vacante = vacante(EstadoVacanteEnum.ACTIVA, 1);
        when(repository.findById(1L)).thenReturn(Optional.of(vacante));
        when(repository.save(vacante)).thenReturn(vacante);
        when(responseMapper.toResponse(vacante)).thenReturn(response(EstadoVacanteEnum.CUPOS_COMPLETOS, 0));

        service.descontarCupo(1L);

        assertEquals(0, vacante.getCuposDisponibles());
        assertEquals(EstadoVacanteEnum.CUPOS_COMPLETOS, vacante.getEstado());
        verify(notificadorEventos).notificar(any(EventoSistema.class));
    }

    @Test
    void descontarCupo_sinCupos_lanzaIllegalStateException() {
        Vacante vacante = vacante(EstadoVacanteEnum.ACTIVA, 0);
        when(repository.findById(1L)).thenReturn(Optional.of(vacante));

        assertThrows(IllegalStateException.class, () -> service.descontarCupo(1L));
        assertEquals(EstadoVacanteEnum.CUPOS_COMPLETOS, vacante.getEstado());
    }

    @Test
    void liberarCupo_vacanteActiva_incrementaSinSuperarTotal() {
        Vacante vacante = vacante(EstadoVacanteEnum.ACTIVA, 1);
        vacante.setCuposTotales(2);
        when(repository.findById(1L)).thenReturn(Optional.of(vacante));
        when(repository.save(vacante)).thenReturn(vacante);
        when(responseMapper.toResponse(vacante)).thenReturn(response(EstadoVacanteEnum.ACTIVA, 2));

        service.liberarCupo(1L);

        assertEquals(2, vacante.getCuposDisponibles());
    }

    @Test
    void liberarCupo_vacanteCuposCompletos_transicionaAActiva() {
        Vacante vacante = vacante(EstadoVacanteEnum.CUPOS_COMPLETOS, 0);
        vacante.setCuposTotales(1);
        when(repository.findById(1L)).thenReturn(Optional.of(vacante));
        when(repository.save(vacante)).thenReturn(vacante);
        when(responseMapper.toResponse(vacante)).thenReturn(response(EstadoVacanteEnum.ACTIVA, 1));

        service.liberarCupo(1L);

        assertEquals(1, vacante.getCuposDisponibles());
        assertEquals(EstadoVacanteEnum.ACTIVA, vacante.getEstado());
    }

    @Test
    void obtener_vacanteExistente_retornaResponseMapeado() {
        Vacante vacante = vacante(EstadoVacanteEnum.ACTIVA, 2);
        VacanteResponse response = response(EstadoVacanteEnum.ACTIVA, 2);
        when(repository.findById(1L)).thenReturn(Optional.of(vacante));
        when(responseMapper.toResponse(vacante)).thenReturn(response);

        assertSame(response, service.obtener(1L));
    }

    @Test
    void listar_conFiltros_mapeaPagina() {
        PageRequest pageable = PageRequest.of(0, 5);
        Vacante vacante = vacante(EstadoVacanteEnum.ACTIVA, 2);
        when(repository.findAll(any(Specification.class), eq(pageable))).thenReturn(new PageImpl<>(List.of(vacante)));
        when(responseMapper.toResponse(vacante)).thenReturn(response(EstadoVacanteEnum.ACTIVA, 2));

        assertEquals(1, service.listar(5L, 1L, EstadoVacanteEnum.ACTIVA, "Remoto", "TI", pageable).getTotalElements());
    }

    @Test
    void listarDisponiblesParaEstudiante_soloRetornaActivasConCupoDelPrograma() {
        Vacante activa = vacante(EstadoVacanteEnum.ACTIVA, 2);
        Vacante sinCupos = vacante(EstadoVacanteEnum.ACTIVA, 0);
        Vacante otroPrograma = vacante(EstadoVacanteEnum.ACTIVA, 1);
        otroPrograma.setProgramaId(99L);
        Vacante pendiente = vacante(EstadoVacanteEnum.PENDIENTE_APROBACION, 2);
        when(repository.findAll()).thenReturn(List.of(activa, sinCupos, otroPrograma, pendiente));
        when(responseMapper.toResponse(activa)).thenReturn(response(EstadoVacanteEnum.ACTIVA, 2));

        List<VacanteResponse> disponibles = service.listarDisponiblesParaEstudiante(1L);

        assertEquals(1, disponibles.size());
        assertEquals(EstadoVacanteEnum.ACTIVA, disponibles.get(0).estado());
    }

    @Test
    void listarDisponiblesParaEstudiante_programaNuloIncluyeTodasLasActivasConCupo() {
        Vacante sistemas = vacante(EstadoVacanteEnum.ACTIVA, 2);
        Vacante industrial = vacante(EstadoVacanteEnum.ACTIVA, 1);
        industrial.setProgramaId(99L);
        when(repository.findAll()).thenReturn(List.of(sistemas, industrial));
        when(responseMapper.toResponse(sistemas)).thenReturn(response(EstadoVacanteEnum.ACTIVA, 2));
        when(responseMapper.toResponse(industrial)).thenReturn(response(EstadoVacanteEnum.ACTIVA, 1));

        assertEquals(2, service.listarDisponiblesParaEstudiante(null).size());
    }

    private VacanteRequest request() {
        return new VacanteRequest(
                5L,
                1L,
                20L,
                "empresa@test.com",
                "Practicante Backend",
                "Desarrollo Java",
                "Spring",
                2,
                "TI",
                "Remoto",
                LocalDate.now(),
                LocalDate.now().plusMonths(2)
        );
    }

    private Vacante vacante(EstadoVacanteEnum estado, Integer cuposDisponibles) {
        return Vacante.builder()
                .id(1L)
                .empresaId(5L)
                .programaId(1L)
                .creadoPorId(20L)
                .cargo("Practicante Backend")
                .descripcionPerfil("Desarrollo Java")
                .requisitos("Spring")
                .modalidad("Remoto")
                .area("TI")
                .cuposTotales(2)
                .cuposDisponibles(cuposDisponibles)
                .estado(estado)
                .fechaInicioDisponibilidad(LocalDate.now())
                .fechaFinDisponibilidad(LocalDate.now().plusMonths(2))
                .build();
    }

    private VacanteResponse response(EstadoVacanteEnum estado, Integer cuposDisponibles) {
        return new VacanteResponse(
                1L,
                5L,
                "Empresa SAS",
                1L,
                "Sistemas",
                20L,
                null,
                "Practicante Backend",
                "Desarrollo Java",
                "Spring",
                "Remoto",
                "TI",
                2,
                2 - cuposDisponibles,
                cuposDisponibles,
                estado,
                "COLOR",
                null,
                LocalDate.now(),
                LocalDate.now().plusMonths(2)
        );
    }
}
