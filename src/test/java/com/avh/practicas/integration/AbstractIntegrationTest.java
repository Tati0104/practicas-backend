package com.avh.practicas.integration;

import com.avh.practicas.auth.entity.Usuario;
import com.avh.practicas.auth.repository.AuthUsuarioRepository;
import com.avh.practicas.cierre.entity.Encuesta;
import com.avh.practicas.cierre.entity.EstadoEncuesta;
import com.avh.practicas.cierre.entity.TipoEncuesta;
import com.avh.practicas.cierre.repository.EncuestaRepository;
import com.avh.practicas.configuracion.entity.CatalogoItem;
import com.avh.practicas.configuracion.entity.CatalogoPractica;
import com.avh.practicas.configuracion.entity.Facultad;
import com.avh.practicas.configuracion.entity.Programa;
import com.avh.practicas.configuracion.entity.TipoCatalogo;
import com.avh.practicas.configuracion.repository.CatalogoItemRepository;
import com.avh.practicas.configuracion.repository.CatalogoPracticaRepository;
import com.avh.practicas.configuracion.repository.FacultadRepository;
import com.avh.practicas.configuracion.repository.ProgramaRepository;
import com.avh.practicas.empresa.entity.Empresa;
import com.avh.practicas.empresa.entity.TutorEmpresarial;
import com.avh.practicas.empresa.repository.EmpresaRepository;
import com.avh.practicas.empresa.repository.TutorEmpresarialRepository;
import com.avh.practicas.estudiante.entity.DocenteAsesor;
import com.avh.practicas.estudiante.entity.EstadoAptitud;
import com.avh.practicas.estudiante.entity.EstadoPractica;
import com.avh.practicas.estudiante.entity.Estudiante;
import com.avh.practicas.estudiante.entity.Expediente;
import com.avh.practicas.estudiante.entity.InstanciaPractica;
import com.avh.practicas.estudiante.repository.DocenteAsesorRepository;
import com.avh.practicas.estudiante.repository.EstudianteRepository;
import com.avh.practicas.estudiante.repository.ExpedienteRepository;
import com.avh.practicas.estudiante.repository.InstanciaPracticaRepository;
import com.avh.practicas.shared.enums.Rol;
import com.avh.practicas.shared.enums.Scope;
import com.avh.practicas.vacante.entity.EstadoVacanteEnum;
import com.avh.practicas.vacante.entity.Vacante;
import com.avh.practicas.vacante.repository.VacanteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
abstract class AbstractIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected AuthUsuarioRepository usuarioRepository;

    @Autowired
    protected CatalogoItemRepository catalogoItemRepository;

    @Autowired
    protected CatalogoPracticaRepository catalogoPracticaRepository;

    @Autowired
    protected FacultadRepository facultadRepository;

    @Autowired
    protected ProgramaRepository programaRepository;

    @Autowired
    protected EmpresaRepository empresaRepository;

    @Autowired
    protected TutorEmpresarialRepository tutorEmpresarialRepository;

    @Autowired
    protected DocenteAsesorRepository docenteAsesorRepository;

    @Autowired
    protected VacanteRepository vacanteRepository;

    @Autowired
    protected EstudianteRepository estudianteRepository;

    @Autowired
    protected ExpedienteRepository expedienteRepository;

    @Autowired
    protected InstanciaPracticaRepository instanciaPracticaRepository;

    @Autowired
    protected EncuestaRepository encuestaRepository;

    protected String suffix;

    @BeforeEach
    void setUpIntegrationData() {
        suffix = UUID.randomUUID().toString().substring(0, 8);
    }

    protected Usuario usuario(String correo, Rol rol, Scope scope) {
        return usuarioRepository.save(Usuario.builder()
                .nombre("Usuario " + suffix)
                .correo(correo)
                .passwordHash("hash")
                .rol(rol)
                .scope(scope)
                .activo(true)
                .primeraVez(false)
                .build());
    }

    protected Usuario usuario(String correo, Rol rol, Scope scope, Facultad facultad) {
        Usuario usuario = usuario(correo, rol, scope);
        usuario.setFacultad(facultad);
        return usuarioRepository.save(usuario);
    }

    protected Facultad facultad() {
        return facultadRepository.save(Facultad.builder()
                .nombre("Facultad IT " + suffix)
                .activo(true)
                .build());
    }

    protected Programa programa(Facultad facultad) {
        return programaRepository.save(Programa.builder()
                .nombre("Programa IT " + suffix)
                .facultad(facultad)
                .totalPracticas(1)
                .activo(true)
                .build());
    }

    protected CatalogoPractica catalogoPractica(Programa programa) {
        return catalogoPracticaRepository.save(CatalogoPractica.builder()
                .programa(programa)
                .numeroPractica(1)
                .nombre("Practica catalogo IT " + unique())
                .materiaNucleo("Nucleo profesional")
                .codigoMateria("CAT-" + unique().substring(0, 8))
                .numCortes(3)
                .duracionSemanas(16)
                .activo(true)
                .build());
    }

    protected CatalogoItem sector() {
        return catalogoItemRepository.save(CatalogoItem.builder()
                .tipo(TipoCatalogo.SECTOR_ECONOMICO)
                .nombre("Sector IT " + suffix)
                .activo(true)
                .build());
    }

    protected Empresa empresa(String nit, CatalogoItem sector, Long usuarioId) {
        return empresaRepository.save(Empresa.builder()
                .nit(nit)
                .razonSocial("Empresa IT " + suffix)
                .sector(sector)
                .direccion("Calle 1")
                .municipio("Bogota")
                .telefono("3000000000")
                .usuarioId(usuarioId)
                .activo(true)
                .build());
    }

    protected Vacante vacante(Empresa empresa, Programa programa, EstadoVacanteEnum estado) {
        return vacanteRepository.save(Vacante.builder()
                .empresaId(empresa.getId())
                .programaId(programa.getId())
                .cargo("Practicante QA " + suffix)
                .descripcionPerfil("Apoyo en pruebas de integracion")
                .requisitos("Java basico")
                .modalidad("REMOTO")
                .area("QA")
                .cuposTotales(2)
                .cuposDisponibles(2)
                .estado(estado)
                .fechaInicioDisponibilidad(LocalDate.now())
                .fechaFinDisponibilidad(LocalDate.now().plusMonths(1))
                .build());
    }

    protected InstanciaPractica practica(Programa programa) {
        return practica(programa, null, null, null, null);
    }

    protected InstanciaPractica practica(Programa programa, Usuario estudianteUsuario) {
        return practica(programa, estudianteUsuario, null, null, null);
    }

    protected InstanciaPractica practica(
            Programa programa,
            Usuario estudianteUsuario,
            Empresa empresa,
            TutorEmpresarial tutor,
            DocenteAsesor docente) {
        String marker = unique();
        Estudiante estudiante = estudianteRepository.save(Estudiante.builder()
                .usuario(estudianteUsuario)
                .identificacion("IT-" + marker)
                .nombre("Estudiante IT " + marker)
                .correo(estudianteUsuario != null ? estudianteUsuario.getCorreo() : "estudiante-" + marker + "@test.com")
                .programa(programa)
                .semestre(8)
                .creditosAprobados(90)
                .promedioAcumulado(4.0)
                .estadoAptitud(EstadoAptitud.APTO)
                .build());

        Expediente expediente = expedienteRepository.save(Expediente.builder()
                .estudiante(estudiante)
                .build());

        return instanciaPracticaRepository.save(InstanciaPractica.builder()
                .expediente(expediente)
                .numeroPractica(1)
                .nombre("Practica I")
                .materiaNucleo("Nucleo profesional")
                .codigoMateria("PR-1")
                .numCortes(3)
                .duracionSemanas(16)
                .estado(EstadoPractica.EN_CURSO)
                .empresaId(empresa != null ? empresa.getId() : null)
                .tutorId(tutor != null ? tutor.getId() : null)
                .docenteAsesorId(docente != null ? docente.getId() : null)
                .fechaInicio(LocalDate.now().minusWeeks(2))
                .fechaFin(LocalDate.now().plusWeeks(14))
                .inmutable(false)
                .build());
    }

    protected TutorEmpresarial tutor(Empresa empresa, Usuario usuario) {
        String marker = unique();
        return tutorEmpresarialRepository.save(TutorEmpresarial.builder()
                .empresa(empresa)
                .nombre("Tutor IT " + marker)
                .cargo("Tutor")
                .correo(usuario.getCorreo())
                .telefono("3000000001")
                .usuarioId(usuario.getId())
                .activo(true)
                .build());
    }

    protected DocenteAsesor docente(Programa programa, Usuario usuario) {
        String marker = unique();
        return docenteAsesorRepository.save(DocenteAsesor.builder()
                .usuario(usuario)
                .nombre("Docente IT " + marker)
                .correo(usuario.getCorreo())
                .telefono("3000000002")
                .programaId(programa.getId())
                .areaConocimiento("QA")
                .activo(true)
                .build());
    }

    protected Encuesta encuesta(InstanciaPractica practica, TipoEncuesta tipo, EstadoEncuesta estado) {
        return encuestaRepository.save(Encuesta.builder()
                .instanciaPractica(practica)
                .tipo(tipo)
                .estado(estado)
                .fechaEnvioInvitacion(LocalDateTime.now())
                .respuestasJson("{}")
                .build());
    }

    protected String unique() {
        return suffix + "-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
