package com.avh.practicas.shared.security;

import com.avh.practicas.auth.entity.Usuario;
import com.avh.practicas.configuracion.entity.Facultad;
import com.avh.practicas.configuracion.entity.Programa;
import com.avh.practicas.configuracion.repository.ProgramaRepository;
import com.avh.practicas.estudiante.entity.Expediente;
import com.avh.practicas.estudiante.entity.InstanciaPractica;
import com.avh.practicas.empresa.entity.Empresa;
import com.avh.practicas.empresa.entity.TutorEmpresarial;
import com.avh.practicas.estudiante.entity.Estudiante;
import com.avh.practicas.empresa.repository.EmpresaRepository;
import com.avh.practicas.empresa.repository.TutorEmpresarialRepository;
import com.avh.practicas.estudiante.repository.DocenteAsesorRepository;
import com.avh.practicas.estudiante.repository.EstudianteRepository;
import com.avh.practicas.estudiante.repository.InstanciaPracticaRepository;
import com.avh.practicas.seguimiento.service.BitacoraService;
import com.avh.practicas.shared.enums.Rol;
import com.avh.practicas.shared.enums.Scope;
import com.avh.practicas.shared.exception.AccesoNoAutorizadoException;
import com.avh.practicas.vacante.repository.VacanteRepository;
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
    @Mock
    private DocenteAsesorRepository docenteAsesorRepository;
    @Mock
    private EmpresaRepository empresaRepository;
    @Mock
    private TutorEmpresarialRepository tutorEmpresarialRepository;
    @Mock
    private InstanciaPracticaRepository instanciaPracticaRepository;
    @Mock
    private VacanteRepository vacanteRepository;

    private ScopeGuard scopeGuard;

    @BeforeEach
    void setUp() {
        scopeGuard = new ScopeGuard(
                estudianteRepository,
                programaRepository,
                docenteAsesorRepository,
                empresaRepository,
                tutorEmpresarialRepository,
                instanciaPracticaRepository,
                vacanteRepository,
                bitacoraService
        );
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
                .id(1L)
                .correo("estudiante@test.com")
                .programa(programa)
                .build();

        Estudiante recursoEstudiante = Estudiante.builder()
                .id(1L)
                .identificacion("123")
                .programa(programa)
                .build();

        when(estudianteRepository.findByCorreoIgnoreCase("estudiante@test.com")).thenReturn(Optional.of(estudianteUsuario));

        assertTrue(scopeGuard.verificarScope(usuario, recursoEstudiante, "LEER"));
        verify(estudianteRepository).findByCorreoIgnoreCase("estudiante@test.com");
        verifyNoInteractions(bitacoraService);
    }

    @Test
    void verificarScope_EstudianteOtroEstudianteMismoPrograma_LanzaExcepcion() {
        Usuario usuario = Usuario.builder()
                .correo("estudiante@test.com")
                .rol(Rol.ESTUDIANTE)
                .scope(Scope.PROGRAMA)
                .build();

        Programa programa = Programa.builder().id(1L).nombre("Sistemas").build();

        Estudiante estudianteUsuario = Estudiante.builder()
                .id(1L)
                .correo("estudiante@test.com")
                .programa(programa)
                .build();

        Estudiante recursoEstudiante = Estudiante.builder()
                .id(2L)
                .identificacion("456")
                .programa(programa)
                .build();

        when(estudianteRepository.findByCorreoIgnoreCase("estudiante@test.com")).thenReturn(Optional.of(estudianteUsuario));

        AccesoNoAutorizadoException exception = assertThrows(AccesoNoAutorizadoException.class, () ->
                scopeGuard.verificarScope(usuario, recursoEstudiante, "LEER")
        );

        assertEquals("Acceso denegado: solo puede consultar su propia información", exception.getMessage());
        verify(bitacoraService).registrar(
                eq("estudiantes"),
                eq("LEER"),
                eq(usuario),
                contains("Acceso denegado: recurso fuera del scope del estudiante")
        );
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
                .id(10L)
                .correo("estudiante@test.com")
                .programa(programaUsuario)
                .build();

        when(estudianteRepository.findByCorreoIgnoreCase("estudiante@test.com")).thenReturn(Optional.of(estudianteUsuario));

        AccesoNoAutorizadoException exception = assertThrows(AccesoNoAutorizadoException.class, () ->
            scopeGuard.verificarScope(usuario, programaRecurso, "LEER")
        );

        assertEquals("Acceso denegado: recurso fuera del scope", exception.getMessage());
        verify(estudianteRepository).findByCorreoIgnoreCase("estudiante@test.com");
        verify(bitacoraService).registrar(
                eq("programas"),
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

    @Test
    void verificarScope_usuarioNulo_lanzaAccesoNoAutorizado() {
        AccesoNoAutorizadoException exception = assertThrows(AccesoNoAutorizadoException.class,
                () -> scopeGuard.verificarScope(null, new Object(), "LEER"));

        assertEquals("Acceso denegado: usuario no autenticado", exception.getMessage());
        verifyNoInteractions(estudianteRepository, programaRepository, bitacoraService);
    }

    @Test
    void verificarScope_FacultadCoincideConPrograma_Pasa() {
        Facultad facultad = Facultad.builder().id(1L).nombre("Ingenieria").build();
        Usuario usuario = Usuario.builder()
                .correo("coord@test.com")
                .rol(Rol.COORD_ACADEMICA)
                .scope(Scope.FACULTAD)
                .facultad(facultad)
                .build();
        Programa programa = Programa.builder().id(2L).nombre("Sistemas").facultad(facultad).build();

        assertTrue(scopeGuard.verificarScope(usuario, programa, "LEER"));
        verifyNoInteractions(bitacoraService);
    }

    @Test
    void verificarScope_FacultadNoCoincideConInstanciaPractica_lanzaYRegistra() {
        Facultad facultadUsuario = Facultad.builder().id(1L).nombre("Ingenieria").build();
        Facultad facultadRecurso = Facultad.builder().id(2L).nombre("Salud").build();
        Programa programa = Programa.builder().id(3L).nombre("Medicina").facultad(facultadRecurso).build();
        Estudiante estudiante = Estudiante.builder().programa(programa).build();
        InstanciaPractica practica = InstanciaPractica.builder()
                .id(8L)
                .expediente(Expediente.builder().estudiante(estudiante).build())
                .build();
        Usuario usuario = Usuario.builder()
                .correo("coord@test.com")
                .rol(Rol.COORD_ACADEMICA)
                .scope(Scope.FACULTAD)
                .facultad(facultadUsuario)
                .build();

        assertThrows(AccesoNoAutorizadoException.class,
                () -> scopeGuard.verificarScope(usuario, practica, "LEER"));
        verify(bitacoraService).registrar(
                eq("instancias_practica"),
                eq("LEER"),
                eq(usuario),
                contains("Acceso denegado: recurso fuera del scope del facultad")
        );
    }
}
