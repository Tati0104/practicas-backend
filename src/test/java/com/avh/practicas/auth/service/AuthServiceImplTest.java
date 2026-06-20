package com.avh.practicas.auth.service;

import com.avh.practicas.auth.dto.LoginResponse;
import com.avh.practicas.auth.entity.Usuario;
import com.avh.practicas.auth.repository.AuthUsuarioRepository;
import com.avh.practicas.auth.security.JwtTokenProvider;
import com.avh.practicas.shared.enums.Rol;
import com.avh.practicas.shared.enums.Scope;
import com.avh.practicas.shared.exception.NegocioException;
import com.avh.practicas.shared.exception.RecursoNoEncontradoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private AuthUsuarioRepository usuarioRepository;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private IMailService mailService;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void login_credencialesValidas_retornaDatosUsuarioYToken() {
        Usuario usuario = usuario();
        when(usuarioRepository.findByCorreo("user@test.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("secreto", "hash")).thenReturn(true);
        when(jwtTokenProvider.generarToken(usuario)).thenReturn("jwt-token");

        LoginResponse response = authService.login(" user@test.com ", "secreto");

        assertEquals("jwt-token", response.getToken());
        assertEquals(10L, response.getId());
        assertEquals(Rol.ADMIN, response.getRol());
        assertEquals(Scope.GLOBAL, response.getScope());
        assertEquals("Usuario Prueba", response.getNombre());
        assertFalse(response.getPrimeraVez());
        verify(usuarioRepository).findByCorreo("user@test.com");
    }

    @Test
    void login_correoNoExiste_lanzaNegocioException() {
        when(usuarioRepository.findByCorreo("nadie@test.com")).thenReturn(Optional.empty());

        NegocioException exception = assertThrows(NegocioException.class,
                () -> authService.login("nadie@test.com", "secreto"));

        assertTrue(exception.getMessage().startsWith("Credenciales"));
        verifyNoInteractions(passwordEncoder, jwtTokenProvider);
    }

    @Test
    void login_usuarioInactivo_lanzaNegocioException() {
        Usuario usuario = usuario();
        usuario.setActivo(false);
        when(usuarioRepository.findByCorreo("user@test.com")).thenReturn(Optional.of(usuario));

        NegocioException exception = assertThrows(NegocioException.class,
                () -> authService.login("user@test.com", "secreto"));

        assertEquals("El usuario se encuentra inactivo", exception.getMessage());
        verifyNoInteractions(passwordEncoder, jwtTokenProvider);
    }

    @Test
    void login_passwordIncorrecta_lanzaNegocioException() {
        Usuario usuario = usuario();
        when(usuarioRepository.findByCorreo("user@test.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("mala", "hash")).thenReturn(false);

        NegocioException exception = assertThrows(NegocioException.class,
                () -> authService.login("user@test.com", "mala"));

        assertTrue(exception.getMessage().startsWith("Credenciales"));
        verifyNoInteractions(jwtTokenProvider);
    }

    @Test
    void cambiarPassword_usuarioExiste_actualizaHashYPrimeraVez() {
        Usuario usuario = usuario();
        usuario.setPrimeraVez(true);
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.encode("nueva")).thenReturn("hash-nuevo");

        authService.cambiarPassword(10L, "nueva");

        assertEquals("hash-nuevo", usuario.getPasswordHash());
        assertFalse(usuario.getPrimeraVez());
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void cambiarPassword_usuarioNoExiste_lanzaRecursoNoEncontrado() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        RecursoNoEncontradoException exception = assertThrows(RecursoNoEncontradoException.class,
                () -> authService.cambiarPassword(99L, "nueva"));

        assertTrue(exception.getMessage().contains("99"));
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void recuperar_usuarioExiste_guardaTokenYEnviaCorreo() {
        Usuario usuario = usuario();
        when(usuarioRepository.findByCorreo("user@test.com")).thenReturn(Optional.of(usuario));

        authService.recuperar(" user@test.com ");

        assertNotNull(usuario.getTokenRecuperacion());
        assertTrue(usuario.getTokenExpiracion().isAfter(LocalDateTime.now()));
        verify(usuarioRepository).save(usuario);
        verify(mailService).enviarRecuperacionPassword(eq("user@test.com"), any(String.class));
    }

    @Test
    void recuperar_usuarioNoExiste_noHaceNada() {
        when(usuarioRepository.findByCorreo("nadie@test.com")).thenReturn(Optional.empty());

        authService.recuperar("nadie@test.com");

        verify(usuarioRepository, never()).save(any());
        verifyNoInteractions(mailService);
    }

    @Test
    void resetear_tokenValido_actualizaPasswordYLimpiaToken() {
        Usuario usuario = usuario();
        usuario.setTokenRecuperacion("token");
        usuario.setTokenExpiracion(LocalDateTime.now().plusHours(1));
        when(usuarioRepository.findByTokenRecuperacion("token")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.encode("nueva")).thenReturn("hash-nuevo");

        authService.resetear("token", "nueva");

        assertEquals("hash-nuevo", usuario.getPasswordHash());
        assertFalse(usuario.getPrimeraVez());
        assertNull(usuario.getTokenRecuperacion());
        assertNull(usuario.getTokenExpiracion());
        verify(usuarioRepository).save(usuario);
    }

    private Usuario usuario() {
        Usuario usuario = Usuario.builder()
                .nombre("Usuario Prueba")
                .correo("user@test.com")
                .passwordHash("hash")
                .rol(Rol.ADMIN)
                .scope(Scope.GLOBAL)
                .activo(true)
                .primeraVez(false)
                .build();
        usuario.setId(10L);
        return usuario;
    }
}
