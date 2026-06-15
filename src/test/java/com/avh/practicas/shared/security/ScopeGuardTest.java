package com.avh.practicas.shared.security;

import com.avh.practicas.auth.entity.Usuario;
import com.avh.practicas.configuracion.entity.Facultad;
import com.avh.practicas.configuracion.entity.Programa;
import com.avh.practicas.configuracion.repository.ProgramaRepository;
import com.avh.practicas.empresa.entity.Empresa;
import com.avh.practicas.empresa.entity.TutorEmpresarial;
import com.avh.practicas.estudiante.entity.Estudiante;
import com.avh.practicas.estudiante.repository.EstudianteRepository;
import com.avh.practicas.seguimiento.service.BitacoraService;
import com.avh.practicas.shared.enums.Rol;
import com.avh.practicas.shared.enums.Scope;
import com.avh.practicas.shared.exception.AccesoNoAutorizadoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScopeGuardTest {

    @Mock
    private EstudianteRepository estudianteRepository;

    @Mock
    private BitacoraService bitacoraService;

    @Mock
    private ProgramaRepository programaRepository;

    private ScopeGuard scopeGuard;

    @BeforeEach
    void setUp() {
        scopeGuard = new ScopeGuard(estudianteRepository, programaRepository, bitacoraService);
    }

    @Test
    void verificarScope_AdminSiemprePasa() {
        Usuario admin = Usuario.builder()
                .rol(Rol.ADMIN)
                .scope(Scope.PROGRAMA)
                .build();
        
        Object recurso = new Object();
        
        assertTrue(scopeGuard.verificarScope(admin, recurso, "CREAR"));
        verifyNoInteractions(estudianteRepository, bitacoraService);
    }

    @Test
    void verificarScope_GlobalSiemprePasa() {
        Usuario usuario = Usuario.builder()
                .rol(Rol.COORD_PRACTICA)
                .scope(Scope.GLOBAL)
                .build();
        
        Object recurso = new Object();
        
        assertTrue(scopeGuard.verificarScope(usuario, recurso, "CREAR"));
        verifyNoInteractions(estudianteRepository, bitacoraService);
    }

    @Test
    void verificarScope_ProgramaCoincide_Pasa() {
        Usuario usuario = Usuario.builder()
                .correo("estudiante@test.com")
                .rol(Rol.ESTUDIANTE)
                .scope(Scope.PROGRAMA)
                .build();

        Programa programa = Programa.builder().id(1L).nombre("Sistemas").build();
        
        Estudiante estudianteUsuario = Estudiante.builder()
                .correo("estudiante@test.com")
                .programa(programa)
                .build();

        Estudiante recursoEstudiante = Estudiante.builder()
                .identificacion("123")
                .programa(programa)
                .build();

        when(estudianteRepository.findByCorreo("estudiante@test.com")).thenReturn(Optional.of(estudianteUsuario));

        assertTrue(scopeGuard.verificarScope(usuario, recursoEstudiante, "LEER"));
        verify(estudianteRepository).findByCorreo("estudiante@test.com");
        verifyNoInteractions(bitacoraService);
    }

    @Test
    void verificarScope_ProgramaNoCoincide_LanzaExcepcionYRegistraBitacora() {
        Usuario usuario = Usuario.builder()
                .correo("estudiante@test.com")
                .rol(Rol.ESTUDIANTE)
                .scope(Scope.PROGRAMA)
                .build();

        Programa programaUsuario = Programa.builder().id(1L).nombre("Sistemas").build();
        Programa programaRecurso = Programa.builder().id(2L).nombre("Civil").build();
        
        Estudiante estudianteUsuario = Estudiante.builder()
                .correo("estudiante@test.com")
                .programa(programaUsuario)
                .build();

        Estudiante recursoEstudiante = Estudiante.builder()
                .id(10L)
                .identificacion("123")
                .programa(programaRecurso)
                .build();

        when(estudianteRepository.findByCorreo("estudiante@test.com")).thenReturn(Optional.of(estudianteUsuario));

        AccesoNoAutorizadoException exception = assertThrows(AccesoNoAutorizadoException.class, () -> 
            scopeGuard.verificarScope(usuario, recursoEstudiante, "LEER")
        );

        assertEquals("Acceso denegado: recurso fuera del scope", exception.getMessage());
        verify(estudianteRepository).findByCorreo("estudiante@test.com");
        verify(bitacoraService).registrar(
                eq("estudiantes"),
                eq("LEER"),
                eq(usuario),
                contains("Acceso denegado: recurso fuera del scope del programa")
        );
    }

    @Test
    void verificarScope_ProgramaEmpresa_PasaSinValidarPrograma() {
        Usuario usuario = Usuario.builder()
                .correo("coordinador@test.com")
                .rol(Rol.COORD_PRACTICA)
                .scope(Scope.PROGRAMA)
                .build();

        Empresa empresa = Empresa.builder().id(1L).nit("900").razonSocial("Test S.A.").build();

        assertTrue(scopeGuard.verificarScope(usuario, empresa, "LEER"));
        verifyNoInteractions(estudianteRepository, bitacoraService);
    }

    @Test
    void verificarScope_ProgramaTutorEmpresarial_PasaSinValidarPrograma() {
        Usuario usuario = Usuario.builder()
                .correo("coordinador@test.com")
                .rol(Rol.COORD_PRACTICA)
                .scope(Scope.PROGRAMA)
                .build();

        TutorEmpresarial tutor = TutorEmpresarial.builder().id(1L).nombre("John Doe").build();

        assertTrue(scopeGuard.verificarScope(usuario, tutor, "LEER"));
        verifyNoInteractions(estudianteRepository, bitacoraService);
    }
}
