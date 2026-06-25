package com.avh.practicas.empresa.service;

import com.avh.practicas.auth.entity.Usuario;
import com.avh.practicas.empresa.entity.Empresa;
import com.avh.practicas.empresa.entity.TutorEmpresarial;
import com.avh.practicas.empresa.repository.EmpresaRepository;
import com.avh.practicas.empresa.repository.TutorEmpresarialRepository;
import com.avh.practicas.shared.enums.Rol;
import com.avh.practicas.shared.enums.Scope;
import com.avh.practicas.shared.exception.NegocioException;
import com.avh.practicas.shared.exception.RecursoNoEncontradoException;
import com.avh.practicas.usuario.service.CorreoPersonaService;
import com.avh.practicas.usuario.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TutorEmpresarialServiceImplTest {

    @Mock
    private TutorEmpresarialRepository tutorEmpresarialRepository;
    @Mock
    private EmpresaRepository empresaRepository;
    @Mock
    private CorreoPersonaService correoPersonaService;
    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private TutorEmpresarialServiceImpl service;

    @BeforeEach
    void setUp() {
        lenient().when(correoPersonaService.normalizar(anyString()))
                .thenAnswer(invocation -> invocation.getArgument(0, String.class).trim().toLowerCase());
    }

    @Test
    void registrar_tutorValido_asociaEmpresaYActiva() {
        TutorEmpresarial tutor = tutor(false);
        Empresa empresa = empresa(true);
        when(empresaRepository.findById(5L)).thenReturn(Optional.of(empresa));
        when(usuarioService.crearUsuarioTutorEmpresarial("Tutor", "tutor@test.com")).thenReturn(usuario(10L));
        when(tutorEmpresarialRepository.save(tutor)).thenReturn(tutor);

        TutorEmpresarial response = service.registrar(tutor);

        assertSame(empresa, response.getEmpresa());
        assertTrue(response.getActivo());
    }

    @Test
    void registrar_correoDuplicado_lanzaNegocioException() {
        TutorEmpresarial tutor = tutor(true);
        doThrow(new NegocioException("Correo duplicado"))
                .when(correoPersonaService).validarCorreoDisponible(eq("tutor@test.com"), any());

        assertThrows(NegocioException.class, () -> service.registrar(tutor));
        verify(tutorEmpresarialRepository, never()).save(any());
    }

    @Test
    void registrar_empresaNoExiste_lanzaRecursoNoEncontrado() {
        TutorEmpresarial tutor = tutor(true);
        when(empresaRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.registrar(tutor));
    }

    @Test
    void registrar_empresaInactiva_lanzaNegocioException() {
        TutorEmpresarial tutor = tutor(true);
        when(empresaRepository.findById(5L)).thenReturn(Optional.of(empresa(false)));

        assertThrows(NegocioException.class, () -> service.registrar(tutor));
        verify(tutorEmpresarialRepository, never()).save(any());
    }

    @Test
    void editar_tutorExistente_actualizaCamposYEmpresa() {
        TutorEmpresarial existente = tutor(true);
        existente.setUsuarioId(10L);
        TutorEmpresarial actualizado = tutor(true);
        actualizado.setNombre("Nuevo Tutor");
        actualizado.setCorreo("nuevo@test.com");
        actualizado.setCargo("Jefe");
        actualizado.setTelefono("555");
        Empresa empresa = empresa(true);
        when(tutorEmpresarialRepository.findById(8L)).thenReturn(Optional.of(existente));
        when(empresaRepository.findById(5L)).thenReturn(Optional.of(empresa));
        when(usuarioService.obtener(10L)).thenReturn(usuario(10L));
        when(tutorEmpresarialRepository.save(existente)).thenReturn(existente);

        TutorEmpresarial response = service.editar(8L, actualizado);

        assertEquals("Nuevo Tutor", response.getNombre());
        assertEquals("nuevo@test.com", response.getCorreo());
        assertEquals(10L, response.getUsuarioId());
        assertSame(empresa, response.getEmpresa());
    }

    @Test
    void editar_tutorNoExiste_lanzaRecursoNoEncontrado() {
        when(tutorEmpresarialRepository.findById(8L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.editar(8L, tutor(true)));
    }

    @Test
    void editar_correoDuplicado_lanzaNegocioException() {
        TutorEmpresarial existente = tutor(true);
        TutorEmpresarial actualizado = tutor(true);
        when(tutorEmpresarialRepository.findById(8L)).thenReturn(Optional.of(existente));
        doThrow(new NegocioException("Correo duplicado"))
                .when(correoPersonaService).validarCorreoDisponible(eq("tutor@test.com"), any());

        assertThrows(NegocioException.class, () -> service.editar(8L, actualizado));
        verify(tutorEmpresarialRepository, never()).save(any());
    }

    @Test
    void desactivar_tutorExistente_marcaInactivo() {
        TutorEmpresarial tutor = tutor(true);
        when(tutorEmpresarialRepository.findById(8L)).thenReturn(Optional.of(tutor));

        service.desactivar(8L);

        assertFalse(tutor.getActivo());
        verify(tutorEmpresarialRepository).save(tutor);
    }

    @Test
    void activar_empresaActiva_marcaActivo() {
        TutorEmpresarial tutor = tutor(false);
        tutor.setEmpresa(empresa(true));
        when(tutorEmpresarialRepository.findById(8L)).thenReturn(Optional.of(tutor));

        service.activar(8L);

        assertTrue(tutor.getActivo());
        verify(tutorEmpresarialRepository).save(tutor);
    }

    @Test
    void activar_empresaInactiva_lanzaNegocioException() {
        TutorEmpresarial tutor = tutor(false);
        tutor.setEmpresa(empresa(false));
        when(tutorEmpresarialRepository.findById(8L)).thenReturn(Optional.of(tutor));

        assertThrows(NegocioException.class, () -> service.activar(8L));
        verify(tutorEmpresarialRepository, never()).save(any());
    }

    @Test
    void obtenerPorEmpresa_delegaEnRepositorio() {
        List<TutorEmpresarial> tutores = List.of(tutor(true));
        when(tutorEmpresarialRepository.findByEmpresaId(5L)).thenReturn(tutores);

        assertSame(tutores, service.obtenerPorEmpresa(5L));
    }

    @Test
    void obtenerActivosPorEmpresa_delegaEnRepositorio() {
        List<TutorEmpresarial> tutores = List.of(tutor(true));
        when(tutorEmpresarialRepository.findByEmpresaIdAndActivoTrue(5L)).thenReturn(tutores);

        assertSame(tutores, service.obtenerActivosPorEmpresa(5L));
    }

    private TutorEmpresarial tutor(Boolean activo) {
        return TutorEmpresarial.builder()
                .empresa(Empresa.builder().id(5L).build())
                .nombre("Tutor")
                .cargo("Lider")
                .correo("tutor@test.com")
                .telefono("300")
                .activo(activo)
                .build();
    }

    private Empresa empresa(Boolean activo) {
        return Empresa.builder()
                .id(5L)
                .nit("900")
                .razonSocial("Empresa SAS")
                .activo(activo)
                .build();
    }

    private Usuario usuario(Long id) {
        Usuario usuario = Usuario.builder()
                .nombre("Tutor")
                .correo("tutor@test.com")
                .passwordHash("hash")
                .rol(Rol.TUTOR_EMPRESARIAL)
                .scope(Scope.ASIGNADO)
                .activo(true)
                .primeraVez(true)
                .build();
        usuario.setId(id);
        return usuario;
    }
}
