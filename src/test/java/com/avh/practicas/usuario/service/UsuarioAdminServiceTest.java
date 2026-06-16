package com.avh.practicas.usuario.service;

import com.avh.practicas.auth.entity.Usuario;
import com.avh.practicas.auth.repository.AuthUsuarioRepository;
import com.avh.practicas.configuracion.entity.Facultad;
import com.avh.practicas.configuracion.repository.FacultadRepository;
import com.avh.practicas.correo.service.IMailService;
import com.avh.practicas.empresa.entity.Empresa;
import com.avh.practicas.empresa.entity.TutorEmpresarial;
import com.avh.practicas.empresa.repository.EmpresaRepository;
import com.avh.practicas.empresa.repository.TutorEmpresarialRepository;
import com.avh.practicas.shared.enums.Rol;
import com.avh.practicas.shared.enums.Scope;
import com.avh.practicas.shared.exception.NegocioException;
import com.avh.practicas.shared.exception.RecursoNoEncontradoException;
import com.avh.practicas.usuario.dto.CrearUsuarioRequest;
import com.avh.practicas.usuario.dto.EditarUsuarioRequest;
import com.avh.practicas.usuario.dto.UsuarioDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioAdminServiceTest {

    @Mock
    private AuthUsuarioRepository usuarioRepository;
    @Mock
    private FacultadRepository facultadRepository;
    @Mock
    private EmpresaRepository empresaRepository;
    @Mock
    private TutorEmpresarialRepository tutorEmpresarialRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private IMailService mailService;

    @InjectMocks
    private UsuarioAdminService usuarioAdminService;

    @Test
    void crear_adminValido_creaUsuarioGlobalYEnviaCorreo() {
        CrearUsuarioRequest request = crearRequest(Rol.ADMIN);
        when(usuarioRepository.existsByCorreo("admin@test.com")).thenReturn(false);
        when(passwordEncoder.encode(any(String.class))).thenReturn("hash-temporal");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario usuario = invocation.getArgument(0);
            usuario.setId(1L);
            return usuario;
        });

        UsuarioDto response = usuarioAdminService.crear(request);

        assertEquals(1L, response.getId());
        assertEquals(Rol.ADMIN, response.getRol());
        assertEquals(Scope.GLOBAL, response.getScope());
        assertNull(response.getFacultadId());
        verify(mailService).enviar(eq("admin@test.com"), contains("Acceso al Sistema"), contains("contrase"));
    }

    @Test
    void crear_correoDuplicado_lanzaIllegalArgumentException() {
        CrearUsuarioRequest request = crearRequest(Rol.ADMIN);
        when(usuarioRepository.existsByCorreo("admin@test.com")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> usuarioAdminService.crear(request));

        assertEquals("Ya existe un usuario registrado con el correo: admin@test.com", exception.getMessage());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void crear_rolFacultadSinFacultad_lanzaNegocioException() {
        CrearUsuarioRequest request = crearRequest(Rol.COORD_PRACTICA);
        request.setFacultadId(null);
        when(usuarioRepository.existsByCorreo("admin@test.com")).thenReturn(false);
        when(passwordEncoder.encode(any(String.class))).thenReturn("hash-temporal");

        NegocioException exception = assertThrows(NegocioException.class,
                () -> usuarioAdminService.crear(request));

        assertEquals("Debe seleccionar la facultad para este rol.", exception.getMessage());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void crear_rolFacultadConFacultad_asignaFacultad() {
        CrearUsuarioRequest request = crearRequest(Rol.COORD_PRACTICA);
        request.setFacultadId(5L);
        Facultad facultad = Facultad.builder().id(5L).nombre("Ingenieria").activo(true).build();
        when(usuarioRepository.existsByCorreo("admin@test.com")).thenReturn(false);
        when(passwordEncoder.encode(any(String.class))).thenReturn("hash-temporal");
        when(facultadRepository.findById(5L)).thenReturn(Optional.of(facultad));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UsuarioDto response = usuarioAdminService.crear(request);

        assertEquals(Rol.COORD_PRACTICA, response.getRol());
        assertEquals(Scope.FACULTAD, response.getScope());
        assertEquals(5L, response.getFacultadId());
    }

    @Test
    void crear_tutorEmpresarialValido_creaTutorAsociado() {
        CrearUsuarioRequest request = crearRequest(Rol.TUTOR_EMPRESARIAL);
        request.setEmpresaId(7L);
        request.setTelefonoTutor(" 300123 ");
        request.setCargoTutor("");
        Empresa empresa = Empresa.builder().id(7L).razonSocial("Empresa").activo(true).build();
        when(usuarioRepository.existsByCorreo("admin@test.com")).thenReturn(false);
        when(passwordEncoder.encode(any(String.class))).thenReturn("hash-temporal");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario usuario = invocation.getArgument(0);
            usuario.setId(11L);
            return usuario;
        });
        when(empresaRepository.findById(7L)).thenReturn(Optional.of(empresa));
        when(tutorEmpresarialRepository.existsByCorreo("admin@test.com")).thenReturn(false);

        UsuarioDto response = usuarioAdminService.crear(request);

        assertEquals(Scope.ASIGNADO, response.getScope());
        verify(tutorEmpresarialRepository).save(argThat(tutor ->
                tutor.getEmpresa().equals(empresa)
                        && tutor.getUsuarioId().equals(11L)
                        && tutor.getCargo().equals("Tutor empresarial")
                        && tutor.getTelefono().equals("300123")));
    }

    @Test
    void editar_usuarioExiste_actualizaRolYFacultad() {
        Usuario usuario = usuario(Rol.ADMIN, Scope.GLOBAL);
        Facultad facultad = Facultad.builder().id(3L).nombre("Salud").activo(true).build();
        EditarUsuarioRequest request = editarRequest(Rol.SECRETARIA);
        request.setFacultadId(3L);
        when(usuarioRepository.findById(9L)).thenReturn(Optional.of(usuario));
        when(facultadRepository.findById(3L)).thenReturn(Optional.of(facultad));
        when(usuarioRepository.save(usuario)).thenReturn(usuario);

        UsuarioDto response = usuarioAdminService.editar(9L, request);

        assertEquals("Nombre Editado", response.getNombre());
        assertEquals(Rol.SECRETARIA, response.getRol());
        assertEquals(Scope.FACULTAD, response.getScope());
        assertEquals(3L, response.getFacultadId());
    }

    @Test
    void editar_usuarioNoExiste_lanzaRecursoNoEncontrado() {
        when(usuarioRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> usuarioAdminService.editar(404L, editarRequest(Rol.ADMIN)));
    }

    @Test
    void inactivar_unicoAdminActivo_lanzaIllegalStateException() {
        Usuario usuario = usuario(Rol.ADMIN, Scope.GLOBAL);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.countByRolAndActivo(Rol.ADMIN, true)).thenReturn(1L);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> usuarioAdminService.inactivar(1L));

        assertTrue(exception.getMessage().startsWith("No se puede inactivar"));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void activar_usuarioExiste_marcaActivo() {
        Usuario usuario = usuario(Rol.ESTUDIANTE, Scope.PROGRAMA);
        usuario.setActivo(false);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        usuarioAdminService.activar(1L);

        assertTrue(usuario.getActivo());
        verify(usuarioRepository).save(usuario);
    }

    private CrearUsuarioRequest crearRequest(Rol rol) {
        CrearUsuarioRequest request = new CrearUsuarioRequest();
        request.setNombre("Admin Test");
        request.setCorreo("admin@test.com");
        request.setRol(rol);
        return request;
    }

    private EditarUsuarioRequest editarRequest(Rol rol) {
        EditarUsuarioRequest request = new EditarUsuarioRequest();
        request.setNombre("Nombre Editado");
        request.setRol(rol);
        return request;
    }

    private Usuario usuario(Rol rol, Scope scope) {
        Usuario usuario = Usuario.builder()
                .nombre("Usuario")
                .correo("admin@test.com")
                .passwordHash("hash")
                .rol(rol)
                .scope(scope)
                .activo(true)
                .primeraVez(true)
                .build();
        usuario.setId(1L);
        return usuario;
    }
}
