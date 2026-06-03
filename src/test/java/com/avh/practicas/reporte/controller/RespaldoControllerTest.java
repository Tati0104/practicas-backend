package com.avh.practicas.reporte.controller;

import com.avh.practicas.auth.entity.Usuario;
import com.avh.practicas.auth.repository.AuthUsuarioRepository;
import com.avh.practicas.reporte.service.RespaldoService;
import com.avh.practicas.shared.enums.Rol;
import com.avh.practicas.shared.enums.Scope;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas unitarias del controlador RespaldoController utilizando MockMvc Standalone setup.
 */
@ExtendWith(MockitoExtension.class)
class RespaldoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private RespaldoService respaldoService;

    @Mock
    private AuthUsuarioRepository usuarioRepository;

    @InjectMocks
    private RespaldoController controller;

    private Usuario mockUsuario;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockUsuario = Usuario.builder()
                .nombre("Admin User")
                .correo("admin@test.com")
                .rol(Rol.ADMIN)
                .scope(Scope.PROGRAMA)
                .activo(true)
                .build();
        mockUsuario.setId(1L);

        // Configurar el contexto de seguridad simulado
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("admin@test.com");
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void generarRespaldo_CompletesUnderTimeout() throws Exception {
        byte[] expectedBytes = "Dummy Excel Data".getBytes();
        when(usuarioRepository.findByCorreo("admin@test.com")).thenReturn(Optional.of(mockUsuario));
        when(respaldoService.generarRespaldoAsync(mockUsuario)).thenReturn(CompletableFuture.completedFuture(expectedBytes));

        mockMvc.perform(get("/respaldo/generar"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .andExpect(content().bytes(expectedBytes));

        verify(respaldoService, times(1)).generarRespaldoAsync(mockUsuario);
        verify(respaldoService, never()).guardarRespaldoLocal(any(), any(), any());
    }

    @Test
    void generarRespaldo_TimesOut() throws Exception {
        when(usuarioRepository.findByCorreo("admin@test.com")).thenReturn(Optional.of(mockUsuario));

        // Subclase anónima de CompletableFuture para simular el TimeoutException instantáneamente
        CompletableFuture<byte[]> mockTimeoutFuture = new CompletableFuture<byte[]>() {
            @Override
            public byte[] get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                throw new TimeoutException("Simulated Timeout");
            }
        };

        when(respaldoService.generarRespaldoAsync(mockUsuario)).thenReturn(mockTimeoutFuture);

        mockMvc.perform(get("/respaldo/generar"))
                .andExpect(status().isAccepted())
                .andExpect(content().string(containsString("El respaldo está siendo generado en segundo plano")));

        verify(respaldoService, times(1)).generarRespaldoAsync(mockUsuario);
    }
}
