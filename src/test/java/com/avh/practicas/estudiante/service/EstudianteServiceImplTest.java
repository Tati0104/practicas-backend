package com.avh.practicas.estudiante.service;

import com.avh.practicas.auth.entity.Usuario;
import com.avh.practicas.configuracion.entity.CatalogoPractica;
import com.avh.practicas.configuracion.entity.Programa;
import com.avh.practicas.configuracion.repository.CatalogoPracticaRepository;
import com.avh.practicas.configuracion.repository.ProgramaRepository;
import com.avh.practicas.estudiante.dto.EstudianteDto;
import com.avh.practicas.estudiante.entity.EstadoAptitud;
import com.avh.practicas.estudiante.entity.EstadoPractica;
import com.avh.practicas.estudiante.entity.Estudiante;
import com.avh.practicas.estudiante.entity.Expediente;
import com.avh.practicas.estudiante.entity.InstanciaPractica;
import com.avh.practicas.estudiante.repository.EstudianteRepository;
import com.avh.practicas.estudiante.repository.ExpedienteRepository;
import com.avh.practicas.estudiante.repository.InstanciaPracticaRepository;
import com.avh.practicas.shared.exception.CatalogoPracticaNoEncontradaException;
import com.avh.practicas.shared.exception.NegocioException;
import com.avh.practicas.shared.exception.RecursoNoEncontradoException;
import com.avh.practicas.shared.enums.Rol;
import com.avh.practicas.shared.enums.Scope;
import com.avh.practicas.usuario.service.CorreoPersonaService;
import com.avh.practicas.usuario.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EstudianteServiceImplTest {

    @Mock
    private EstudianteRepository estudianteRepository;
    @Mock
    private ExpedienteRepository expedienteRepository;
    @Mock
    private InstanciaPracticaRepository instanciaPracticaRepository;
    @Mock
    private ProgramaRepository programaRepository;
    @Mock
    private CatalogoPracticaRepository catalogoPracticaRepository;
    @Mock
    private CorreoPersonaService correoPersonaService;
    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private EstudianteServiceImpl service;

    @BeforeEach
    void setUp() {
        lenient().when(correoPersonaService.normalizar(anyString()))
                .thenAnswer(invocation -> invocation.getArgument(0, String.class).trim().toLowerCase());
    }

    @Test
    void registrar_estudianteValido_creaEstudianteYExpediente() {
        EstudianteDto dto = dto();
        Programa programa = programa(true);
        when(estudianteRepository.existsByIdentificacion("123")).thenReturn(false);
        when(programaRepository.findById(1L)).thenReturn(Optional.of(programa));
        when(usuarioService.crearUsuarioEstudiante("Estudiante", "est@test.com")).thenReturn(usuario(10L));
        when(estudianteRepository.save(any(Estudiante.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(expedienteRepository.save(any(Expediente.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Estudiante response = service.registrar(dto);

        assertEquals("123", response.getIdentificacion());
        assertEquals(EstadoAptitud.SIN_EVALUAR, response.getEstadoAptitud());
        assertSame(programa, response.getPrograma());
        assertNotNull(response.getExpediente());
        verify(expedienteRepository).save(any(Expediente.class));
    }

    @Test
    void registrar_identificacionDuplicada_lanzaNegocioException() {
        EstudianteDto dto = dto();
        when(estudianteRepository.existsByIdentificacion("123")).thenReturn(true);

        assertThrows(NegocioException.class, () -> service.registrar(dto));
        verify(estudianteRepository, never()).save(any());
    }

    @Test
    void registrar_correoDuplicado_lanzaNegocioException() {
        EstudianteDto dto = dto();
        when(estudianteRepository.existsByIdentificacion("123")).thenReturn(false);
        doThrow(new NegocioException("Correo duplicado"))
                .when(correoPersonaService).validarCorreoDisponible(eq("est@test.com"), any());

        assertThrows(NegocioException.class, () -> service.registrar(dto));
        verify(estudianteRepository, never()).save(any());
    }

    @Test
    void registrar_programaNoExiste_lanzaRecursoNoEncontrado() {
        EstudianteDto dto = dto();
        when(estudianteRepository.existsByIdentificacion("123")).thenReturn(false);
        when(programaRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.registrar(dto));
    }

    @Test
    void registrar_programaInactivo_lanzaNegocioException() {
        EstudianteDto dto = dto();
        when(estudianteRepository.existsByIdentificacion("123")).thenReturn(false);
        when(programaRepository.findById(1L)).thenReturn(Optional.of(programa(false)));

        assertThrows(NegocioException.class, () -> service.registrar(dto));
        verify(estudianteRepository, never()).save(any());
    }

    @Test
    void editar_estudianteExistente_actualizaDatosEditables() {
        Estudiante estudiante = estudianteAptoBase();
        EstudianteDto dto = dto();
        dto.setNombre("Nombre Editado");
        dto.setTelefono("555");
        dto.setCreditosAprobados(null);
        dto.setPromedioAcumulado(null);
        Programa programa = Programa.builder().id(2L).nombre("Industrial").activo(true).build();
        dto.setProgramaId(2L);
        when(estudianteRepository.findById(7L)).thenReturn(Optional.of(estudiante));
        when(programaRepository.findById(2L)).thenReturn(Optional.of(programa));
        when(estudianteRepository.save(estudiante)).thenReturn(estudiante);

        Estudiante response = service.editar(7L, dto);

        assertEquals("Nombre Editado", response.getNombre());
        assertEquals("555", response.getTelefono());
        assertEquals(0, response.getCreditosAprobados());
        assertEquals(0.0, response.getPromedioAcumulado());
        assertSame(programa, response.getPrograma());
    }

    @Test
    void editar_estudianteNoExiste_lanzaRecursoNoEncontrado() {
        when(estudianteRepository.findById(7L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.editar(7L, dto()));
    }

    @Test
    void marcarApto_estudianteValido_creaPrimeraPractica() {
        Estudiante estudiante = estudianteAptoBase();
        CatalogoPractica plantilla = plantilla(true);
        when(estudianteRepository.findById(7L)).thenReturn(Optional.of(estudiante));
        when(catalogoPracticaRepository.findByProgramaIdAndNumeroPractica(1L, 1)).thenReturn(Optional.of(plantilla));
        when(instanciaPracticaRepository.save(any(InstanciaPractica.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(estudianteRepository.save(estudiante)).thenReturn(estudiante);

        Estudiante response = service.marcarApto(7L);

        assertEquals(EstadoAptitud.APTO, response.getEstadoAptitud());
        assertEquals(1, response.getExpediente().getInstanciasPractica().size());
        verify(instanciaPracticaRepository).save(any(InstanciaPractica.class));
    }

    @Test
    void marcarApto_creditosInsuficientes_lanzaNegocioException() {
        Estudiante estudiante = estudianteAptoBase();
        estudiante.setCreditosAprobados(79);
        when(estudianteRepository.findById(7L)).thenReturn(Optional.of(estudiante));

        assertThrows(NegocioException.class, () -> service.marcarApto(7L));
        verifyNoInteractions(catalogoPracticaRepository);
    }

    @Test
    void marcarApto_promedioInsuficiente_lanzaNegocioException() {
        Estudiante estudiante = estudianteAptoBase();
        estudiante.setPromedioAcumulado(3.4);
        when(estudianteRepository.findById(7L)).thenReturn(Optional.of(estudiante));

        assertThrows(NegocioException.class, () -> service.marcarApto(7L));
        verifyNoInteractions(catalogoPracticaRepository);
    }

    @Test
    void marcarApto_sinExpediente_lanzaNegocioException() {
        Estudiante estudiante = estudianteAptoBase();
        estudiante.setExpediente(null);
        when(estudianteRepository.findById(7L)).thenReturn(Optional.of(estudiante));

        assertThrows(NegocioException.class, () -> service.marcarApto(7L));
    }

    @Test
    void marcarApto_conPracticaActiva_noCreaNuevaInstancia() {
        Estudiante estudiante = estudianteAptoBase();
        estudiante.getExpediente().getInstanciasPractica().add(InstanciaPractica.builder()
                .numeroPractica(1)
                .estado(EstadoPractica.EN_CURSO)
                .build());
        when(estudianteRepository.findById(7L)).thenReturn(Optional.of(estudiante));
        when(estudianteRepository.save(estudiante)).thenReturn(estudiante);

        Estudiante response = service.marcarApto(7L);

        assertEquals(EstadoAptitud.APTO, response.getEstadoAptitud());
        verifyNoInteractions(catalogoPracticaRepository, instanciaPracticaRepository);
    }

    @Test
    void marcarApto_sinCatalogoParaPractica_lanzaCatalogoNoEncontrado() {
        Estudiante estudiante = estudianteAptoBase();
        when(estudianteRepository.findById(7L)).thenReturn(Optional.of(estudiante));
        when(catalogoPracticaRepository.findByProgramaIdAndNumeroPractica(1L, 1)).thenReturn(Optional.empty());

        assertThrows(CatalogoPracticaNoEncontradaException.class, () -> service.marcarApto(7L));
    }

    @Test
    void marcarNoApto_estudianteExiste_actualizaEstado() {
        Estudiante estudiante = estudianteAptoBase();
        when(estudianteRepository.findById(7L)).thenReturn(Optional.of(estudiante));
        when(estudianteRepository.save(estudiante)).thenReturn(estudiante);

        Estudiante response = service.marcarNoApto(7L, "No cumple");

        assertEquals(EstadoAptitud.NO_APTO, response.getEstadoAptitud());
        verify(estudianteRepository).save(estudiante);
    }

    @Test
    void importar_listaValida_creaExpedientesParaCadaEstudiante() {
        Estudiante estudiante = estudianteAptoBase();
        when(estudianteRepository.existsByIdentificacion("123")).thenReturn(false);
        when(estudianteRepository.existsByCorreo("est@test.com")).thenReturn(false);
        when(estudianteRepository.save(estudiante)).thenReturn(estudiante);

        service.importar(List.of(estudiante));

        verify(expedienteRepository).save(any(Expediente.class));
    }

    @Test
    void listar_conFiltros_delegaEnRepositorioConPageable() {
        PageRequest pageable = PageRequest.of(0, 5);
        when(estudianteRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(estudianteAptoBase())));

        assertEquals(1, service.listar("Sistemas", null, EstadoAptitud.APTO, null, "est", pageable).getTotalElements());
    }

    private EstudianteDto dto() {
        return EstudianteDto.builder()
                .identificacion("123")
                .nombre("Estudiante")
                .correo("est@test.com")
                .telefono("300")
                .contactoEmergencia("Contacto")
                .programaId(1L)
                .semestre(8)
                .creditosAprobados(90)
                .promedioAcumulado(4.0)
                .build();
    }

    private Estudiante estudianteAptoBase() {
        Programa programa = programa(true);
        Estudiante estudiante = Estudiante.builder()
                .identificacion("123")
                .nombre("Estudiante")
                .correo("est@test.com")
                .programa(programa)
                .creditosAprobados(90)
                .promedioAcumulado(4.0)
                .estadoAptitud(EstadoAptitud.SIN_EVALUAR)
                .build();
        estudiante.setId(7L);
        estudiante.setExpediente(Expediente.builder().estudiante(estudiante).build());
        return estudiante;
    }

    private Programa programa(Boolean activo) {
        return Programa.builder().id(1L).nombre("Sistemas").activo(activo).build();
    }

    private CatalogoPractica plantilla(Boolean activo) {
        return CatalogoPractica.builder()
                .programa(programa(true))
                .numeroPractica(1)
                .nombre("Practica I")
                .materiaNucleo("Nucleo")
                .codigoMateria("PR1")
                .numCortes(3)
                .duracionSemanas(16)
                .activo(activo)
                .build();
    }

    private Usuario usuario(Long id) {
        Usuario usuario = Usuario.builder()
                .nombre("Estudiante")
                .correo("est@test.com")
                .passwordHash("hash")
                .rol(Rol.ESTUDIANTE)
                .scope(Scope.PROGRAMA)
                .activo(true)
                .primeraVez(true)
                .build();
        usuario.setId(id);
        return usuario;
    }
}
