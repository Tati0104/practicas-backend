package com.avh.practicas.auth.security;

import com.avh.practicas.auth.entity.Usuario;
import com.avh.practicas.shared.enums.Rol;
import com.avh.practicas.shared.enums.Scope;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    private static final String SECRET = "clave-secreta-de-prueba-jwt-avh-2026";

    @Mock
    private FilterChain filterChain;

    private JwtAuthFilter jwtAuthFilter;

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(SECRET, 86_400_000L);
        jwtAuthFilter = new JwtAuthFilter(jwtTokenProvider);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_conBearerValido_debeAutenticarEnSecurityContext() throws Exception {
        String token = jwtTokenProvider.generarToken(usuarioDePrueba());
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);

        jwtAuthFilter.doFilterInternal(request, new MockHttpServletResponse(), filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals("tati@test.com", auth.getPrincipal());
        assertTrue(auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
        assertTrue(auth.getAuthorities().contains(new SimpleGrantedAuthority("GLOBAL")));
        verify(filterChain).doFilter(any(), any());
    }

    @Test
    void doFilterInternal_sinHeader_noDebeAutenticar() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();

        jwtAuthFilter.doFilterInternal(request, new MockHttpServletResponse(), filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(any(), any());
    }

    @Test
    void doFilterInternal_conTokenInvalido_noDebeAutenticar() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer token-invalido");

        jwtAuthFilter.doFilterInternal(request, new MockHttpServletResponse(), filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(any(), any());
    }

    @Test
    void doFilterInternal_conFormatoIncorrecto_noDebeAutenticar() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Basic dXNlcjpwYXNz");

        jwtAuthFilter.doFilterInternal(request, new MockHttpServletResponse(), filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(any(), any());
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
