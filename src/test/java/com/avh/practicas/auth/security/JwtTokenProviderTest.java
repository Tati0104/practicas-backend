package com.avh.practicas.auth.security;

import com.avh.practicas.auth.entity.Usuario;
import com.avh.practicas.shared.enums.Rol;
import com.avh.practicas.shared.enums.Scope;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private static final String SECRET = "clave-secreta-de-prueba-jwt-avh-2026";
    private static final long EXPIRACION_24H_MS = 86_400_000L;

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(SECRET, EXPIRACION_24H_MS);
    }

    @Test
    void generarToken_debeCrearTokenValidoConClaims() {
        Usuario usuario = usuarioDePrueba();

        String token = jwtTokenProvider.generarToken(usuario);

        assertNotNull(token);
        assertFalse(token.isBlank());
        assertTrue(jwtTokenProvider.validarToken(token));
        assertEquals("tati@test.com", jwtTokenProvider.obtenerCorreo(token));
        assertEquals(Rol.ADMIN, jwtTokenProvider.obtenerRol(token));
        assertEquals(Scope.GLOBAL, jwtTokenProvider.obtenerScope(token));
    }

    @Test
    void validarToken_debeRechazarTokenInvalido() {
        assertFalse(jwtTokenProvider.validarToken("token.invalido"));
        assertFalse(jwtTokenProvider.validarToken(null));
        assertFalse(jwtTokenProvider.validarToken(""));
    }

    @Test
    void validarToken_debeRechazarTokenFirmadoConOtraClave() {
        Usuario usuario = usuarioDePrueba();
        String token = jwtTokenProvider.generarToken(usuario);

        JwtTokenProvider otroProvider = new JwtTokenProvider("otra-clave-secreta-diferente", EXPIRACION_24H_MS);

        assertFalse(otroProvider.validarToken(token));
    }

    @Test
    void validarToken_debeRechazarTokenExpirado() throws InterruptedException {
        JwtTokenProvider providerCorto = new JwtTokenProvider(SECRET, 1L);
        String token = providerCorto.generarToken(usuarioDePrueba());

        Thread.sleep(50);

        assertFalse(providerCorto.validarToken(token));
    }

    @Test
    void obtenerCorreo_y_obtenerRol_debenLanzarSiTokenInvalido() {
        assertThrows(Exception.class, () -> jwtTokenProvider.obtenerCorreo("malformado"));
        assertThrows(Exception.class, () -> jwtTokenProvider.obtenerRol("malformado"));
    }

    @Test
    void obtenerScope_debeLanzarSiTokenInvalido() {
        assertThrows(Exception.class, () -> jwtTokenProvider.obtenerScope("malformado"));
    }

    @Test
    void generarToken_usuarioEstudiante_conservaRolYScopePrograma() {
        Usuario usuario = Usuario.builder()
                .nombre("Estudiante")
                .correo("estudiante@test.com")
                .passwordHash("hash")
                .rol(Rol.ESTUDIANTE)
                .scope(Scope.PROGRAMA)
                .activo(true)
                .build();

        String token = jwtTokenProvider.generarToken(usuario);

        assertEquals("estudiante@test.com", jwtTokenProvider.obtenerCorreo(token));
        assertEquals(Rol.ESTUDIANTE, jwtTokenProvider.obtenerRol(token));
        assertEquals(Scope.PROGRAMA, jwtTokenProvider.obtenerScope(token));
    }

    private Usuario usuarioDePrueba() {
        return Usuario.builder()
                .nombre("Tatiana")
                .correo("tati@test.com")
                .passwordHash("hash-seguro")
                .rol(Rol.ADMIN)
                .scope(Scope.GLOBAL)
                .activo(true)
                .build();
    }
}
