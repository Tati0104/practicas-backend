package com.avh.practicas.integration;

import com.avh.practicas.auth.entity.Usuario;
import com.avh.practicas.configuracion.entity.CatalogoPractica;
import com.avh.practicas.configuracion.entity.CatalogoItem;
import com.avh.practicas.configuracion.entity.Facultad;
import com.avh.practicas.configuracion.entity.Programa;
import com.avh.practicas.empresa.entity.Empresa;
import com.avh.practicas.shared.enums.Rol;
import com.avh.practicas.shared.enums.Scope;
import com.avh.practicas.vacante.entity.EstadoVacanteEnum;
import com.avh.practicas.vacante.entity.Vacante;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class VacanteIntegrationTest extends AbstractIntegrationTest {

    @Test
    @WithMockUser(username = "coord-vacantes@test.com", roles = "COORD_PRACTICA")
    void listarVacantes_respondeConVacantesPersistidas() throws Exception {
        usuario("coord-vacantes@test.com", Rol.COORD_PRACTICA, Scope.GLOBAL);
        Facultad facultad = facultad();
        Programa programa = programa(facultad);
        CatalogoItem sector = sector();
        Empresa empresa = empresa("NIT-VAC-" + suffix, sector, null);
        Vacante vacante = vacante(empresa, programa, EstadoVacanteEnum.ACTIVA);

        mockMvc.perform(get("/vacantes")
                        .param("empresaId", empresa.getId().toString())
                        .param("estado", "ACTIVA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].id", hasItem(vacante.getId().intValue())))
                .andExpect(jsonPath("$.content[*].empresaNombre", hasItem(empresa.getRazonSocial())))
                .andExpect(jsonPath("$.content[*].programaNombre", hasItem(programa.getNombre())));
    }

    @Test
    @WithMockUser(username = "empresa-vacantes@test.com", roles = "EMPRESA")
    void listarVacantes_empresaAsociadaSoloVeSusVacantes() throws Exception {
        Usuario usuarioEmpresa = usuario("empresa-vacantes@test.com", Rol.EMPRESA, Scope.ASIGNADO);
        Facultad facultad = facultad();
        Programa programa = programa(facultad);
        CatalogoItem sector = sector();
        Empresa propia = empresa("NIT-PROPIA-" + suffix, sector, usuarioEmpresa.getId());
        Empresa otra = empresa("NIT-OTRA-" + suffix, sector, null);
        Vacante vacantePropia = vacante(propia, programa, EstadoVacanteEnum.ACTIVA);
        Vacante vacanteOtra = vacante(otra, programa, EstadoVacanteEnum.ACTIVA);

        mockMvc.perform(get("/vacantes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].id", hasItem(vacantePropia.getId().intValue())))
                .andExpect(jsonPath("$.content[*].id", not(hasItem(vacanteOtra.getId().intValue()))));
    }

    @Test
    @WithMockUser(username = "empresa-sin-vacantes@test.com", roles = "EMPRESA")
    void listarVacantes_empresaSinVacantesNoEsError() throws Exception {
        Usuario usuarioEmpresa = usuario("empresa-sin-vacantes@test.com", Rol.EMPRESA, Scope.ASIGNADO);
        CatalogoItem sector = sector();
        empresa("NIT-SIN-VAC-" + suffix, sector, usuarioEmpresa.getId());

        mockMvc.perform(get("/vacantes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void listarVacantes_sinAutenticacionRespondeForbidden() throws Exception {
        mockMvc.perform(get("/vacantes"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin-vacantes@test.com", roles = "ADMIN")
    void adminPuedeListarVacantesDeVariasEmpresas() throws Exception {
        usuario("admin-vacantes@test.com", Rol.ADMIN, Scope.GLOBAL);
        Facultad facultad = facultad();
        Programa programa = programa(facultad);
        CatalogoItem sector = sector();
        Empresa empresaUno = empresa("NIT-ADM-1-" + suffix, sector, null);
        Empresa empresaDos = empresa("NIT-ADM-2-" + suffix, sector, null);
        Vacante vacanteUno = vacante(empresaUno, programa, EstadoVacanteEnum.ACTIVA);
        Vacante vacanteDos = vacante(empresaDos, programa, EstadoVacanteEnum.PENDIENTE_APROBACION);

        mockMvc.perform(get("/vacantes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].id", hasItem(vacanteUno.getId().intValue())))
                .andExpect(jsonPath("$.content[*].id", hasItem(vacanteDos.getId().intValue())));
    }

    @Test
    @WithMockUser(username = "coord-filtro-programa@test.com", roles = "COORD_PRACTICA")
    void listarVacantes_filtraPorPrograma() throws Exception {
        usuario("coord-filtro-programa@test.com", Rol.COORD_PRACTICA, Scope.GLOBAL);
        Facultad facultad = facultad();
        Programa programaUno = programa(facultad);
        Programa programaDos = programa(facultad);
        CatalogoItem sector = sector();
        Empresa empresa = empresa("NIT-FIL-PROG-" + suffix, sector, null);
        Vacante esperada = vacante(empresa, programaUno, EstadoVacanteEnum.ACTIVA);
        Vacante excluida = vacante(empresa, programaDos, EstadoVacanteEnum.ACTIVA);

        mockMvc.perform(get("/vacantes").param("programaId", programaUno.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].id", hasItem(esperada.getId().intValue())))
                .andExpect(jsonPath("$.content[*].id", not(hasItem(excluida.getId().intValue()))));
    }

    @Test
    @WithMockUser(username = "coord-filtro-modalidad@test.com", roles = "COORD_PRACTICA")
    void listarVacantes_filtraPorModalidad() throws Exception {
        usuario("coord-filtro-modalidad@test.com", Rol.COORD_PRACTICA, Scope.GLOBAL);
        Facultad facultad = facultad();
        Programa programa = programa(facultad);
        CatalogoItem sector = sector();
        Empresa empresa = empresa("NIT-FIL-MOD-" + suffix, sector, null);
        Vacante remota = vacante(empresa, programa, EstadoVacanteEnum.ACTIVA);
        Vacante presencial = vacante(empresa, programa, EstadoVacanteEnum.ACTIVA);
        presencial.setModalidad("PRESENCIAL");
        vacanteRepository.save(presencial);

        mockMvc.perform(get("/vacantes").param("modalidad", "remoto"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].id", hasItem(remota.getId().intValue())))
                .andExpect(jsonPath("$.content[*].id", not(hasItem(presencial.getId().intValue()))));
    }

    @Test
    @WithMockUser(username = "coord-filtro-area@test.com", roles = "COORD_PRACTICA")
    void listarVacantes_filtraPorArea() throws Exception {
        usuario("coord-filtro-area@test.com", Rol.COORD_PRACTICA, Scope.GLOBAL);
        Facultad facultad = facultad();
        Programa programa = programa(facultad);
        CatalogoItem sector = sector();
        Empresa empresa = empresa("NIT-FIL-AREA-" + suffix, sector, null);
        Vacante qa = vacante(empresa, programa, EstadoVacanteEnum.ACTIVA);
        Vacante dev = vacante(empresa, programa, EstadoVacanteEnum.ACTIVA);
        dev.setArea("DEV");
        vacanteRepository.save(dev);

        mockMvc.perform(get("/vacantes").param("area", "qa"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].id", hasItem(qa.getId().intValue())))
                .andExpect(jsonPath("$.content[*].id", not(hasItem(dev.getId().intValue()))));
    }

    @Test
    @WithMockUser(username = "est-vacantes-disponibles@test.com", roles = "ESTUDIANTE")
    void listarDisponiblesSoloRetornaActivasConCupos() throws Exception {
        Facultad facultad = facultad();
        Programa programa = programa(facultad);
        CatalogoItem sector = sector();
        Empresa empresa = empresa("NIT-DISP-" + suffix, sector, null);
        Vacante activa = vacante(empresa, programa, EstadoVacanteEnum.ACTIVA);
        Vacante pendiente = vacante(empresa, programa, EstadoVacanteEnum.PENDIENTE_APROBACION);
        Vacante sinCupos = vacante(empresa, programa, EstadoVacanteEnum.CUPOS_COMPLETOS);
        sinCupos.setCuposDisponibles(0);
        vacanteRepository.save(sinCupos);

        mockMvc.perform(get("/vacantes/disponibles").param("programaId", programa.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItem(activa.getId().intValue())))
                .andExpect(jsonPath("$[*].id", not(hasItem(pendiente.getId().intValue()))))
                .andExpect(jsonPath("$[*].id", not(hasItem(sinCupos.getId().intValue()))));
    }

    @Test
    @WithMockUser(username = "admin-obtener-vacante@test.com", roles = "ADMIN")
    void obtenerVacanteExistenteRetornaDetalle() throws Exception {
        usuario("admin-obtener-vacante@test.com", Rol.ADMIN, Scope.GLOBAL);
        Facultad facultad = facultad();
        Programa programa = programa(facultad);
        CatalogoItem sector = sector();
        Empresa empresa = empresa("NIT-GET-VAC-" + suffix, sector, null);
        Vacante vacante = vacante(empresa, programa, EstadoVacanteEnum.ACTIVA);

        mockMvc.perform(get("/vacantes/{id}", vacante.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(vacante.getId()))
                .andExpect(jsonPath("$.empresaNombre").value(empresa.getRazonSocial()));
    }

    @Test
    @WithMockUser(username = "empresa-obtener-ajena@test.com", roles = "EMPRESA")
    void empresaNoPuedeObtenerVacanteDeOtraEmpresa() throws Exception {
        Usuario usuarioEmpresa = usuario("empresa-obtener-ajena@test.com", Rol.EMPRESA, Scope.ASIGNADO);
        Facultad facultad = facultad();
        Programa programa = programa(facultad);
        CatalogoItem sector = sector();
        empresa("NIT-GET-PROPIA-" + suffix, sector, usuarioEmpresa.getId());
        Empresa otra = empresa("NIT-GET-AJENA-" + suffix, sector, null);
        Vacante vacanteAjena = vacante(otra, programa, EstadoVacanteEnum.ACTIVA);

        mockMvc.perform(get("/vacantes/{id}", vacanteAjena.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "coord-crear-vacante@test.com", roles = "COORD_PRACTICA")
    void crearVacanteConCatalogoPracticaNullPersistePendiente() throws Exception {
        Usuario creador = usuario("coord-crear-vacante@test.com", Rol.COORD_PRACTICA, Scope.GLOBAL);
        Facultad facultad = facultad();
        Programa programa = programa(facultad);
        CatalogoItem sector = sector();
        Empresa empresa = empresa("NIT-CREAR-NULL-" + suffix, sector, null);

        mockMvc.perform(post("/vacantes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vacanteRequest(
                                empresa.getId(), programa.getId(), null, creador.getId()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("PENDIENTE_APROBACION"))
                .andExpect(jsonPath("$.empresaNombre").value(empresa.getRazonSocial()));
    }

    @Test
    @WithMockUser(username = "coord-crear-catalogo@test.com", roles = "COORD_PRACTICA")
    void crearVacanteConCatalogoPracticaValidoRetornaDatosCatalogo() throws Exception {
        Usuario creador = usuario("coord-crear-catalogo@test.com", Rol.COORD_PRACTICA, Scope.GLOBAL);
        Facultad facultad = facultad();
        Programa programa = programa(facultad);
        CatalogoPractica catalogo = catalogoPractica(programa);
        CatalogoItem sector = sector();
        Empresa empresa = empresa("NIT-CREAR-CAT-" + suffix, sector, null);

        mockMvc.perform(post("/vacantes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vacanteRequest(
                                empresa.getId(), programa.getId(), catalogo.getId(), creador.getId()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.catalogoPracticaNombre").value(catalogo.getNombre()))
                .andExpect(jsonPath("$.numeroPractica").value(catalogo.getNumeroPractica()));
    }

    @Test
    @WithMockUser(username = "empresa-crear-vacante@test.com", roles = "EMPRESA")
    void empresaAsociadaPuedeCrearVacanteSinEnviarEmpresaId() throws Exception {
        Usuario usuarioEmpresa = usuario("empresa-crear-vacante@test.com", Rol.EMPRESA, Scope.ASIGNADO);
        Facultad facultad = facultad();
        Programa programa = programa(facultad);
        CatalogoItem sector = sector();
        Empresa empresa = empresa("NIT-CREAR-EMP-" + suffix, sector, usuarioEmpresa.getId());

        mockMvc.perform(post("/vacantes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vacanteRequest(
                                null, programa.getId(), null, usuarioEmpresa.getId()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.empresaId").value(empresa.getId()));
    }

    @Test
    @WithMockUser(username = "empresa-sin-asociar@test.com", roles = "EMPRESA")
    void crearVacanteEmpresaSinAsociarRespondeForbiddenControlado() throws Exception {
        Usuario usuarioEmpresa = usuario("empresa-sin-asociar@test.com", Rol.EMPRESA, Scope.ASIGNADO);
        Facultad facultad = facultad();
        Programa programa = programa(facultad);

        mockMvc.perform(post("/vacantes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vacanteRequest(
                                null, programa.getId(), null, usuarioEmpresa.getId()))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(username = "empresa-crear-ajena@test.com", roles = "EMPRESA")
    void crearVacanteParaOtraEmpresaRespondeForbidden() throws Exception {
        Usuario usuarioEmpresa = usuario("empresa-crear-ajena@test.com", Rol.EMPRESA, Scope.ASIGNADO);
        Facultad facultad = facultad();
        Programa programa = programa(facultad);
        CatalogoItem sector = sector();
        empresa("NIT-CREAR-PROPIA-" + suffix, sector, usuarioEmpresa.getId());
        Empresa otra = empresa("NIT-CREAR-AJENA-" + suffix, sector, null);

        mockMvc.perform(post("/vacantes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vacanteRequest(
                                otra.getId(), programa.getId(), null, usuarioEmpresa.getId()))))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "coord-aprobar-vacante@test.com", roles = "COORD_PRACTICA")
    void aprobarVacanteCambiaEstadoAActiva() throws Exception {
        Usuario coordinador = usuario("coord-aprobar-vacante@test.com", Rol.COORD_PRACTICA, Scope.GLOBAL);
        Facultad facultad = facultad();
        Programa programa = programa(facultad);
        CatalogoItem sector = sector();
        Empresa empresa = empresa("NIT-APR-" + suffix, sector, null);
        Vacante vacante = vacante(empresa, programa, EstadoVacanteEnum.PENDIENTE_APROBACION);

        mockMvc.perform(patch("/vacantes/{id}/aprobar", vacante.getId())
                        .param("aprobadoPorId", coordinador.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ACTIVA"));

        assertEquals(EstadoVacanteEnum.ACTIVA, vacanteRepository.findById(vacante.getId()).orElseThrow().getEstado());
    }

    @Test
    @WithMockUser(username = "coord-rechazar-vacante@test.com", roles = "COORD_PRACTICA")
    void rechazarVacanteRegistraMotivo() throws Exception {
        usuario("coord-rechazar-vacante@test.com", Rol.COORD_PRACTICA, Scope.GLOBAL);
        Facultad facultad = facultad();
        Programa programa = programa(facultad);
        CatalogoItem sector = sector();
        Empresa empresa = empresa("NIT-RECH-" + suffix, sector, null);
        Vacante vacante = vacante(empresa, programa, EstadoVacanteEnum.PENDIENTE_APROBACION);

        mockMvc.perform(patch("/vacantes/{id}/rechazar", vacante.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("motivo", "Perfil incompleto"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("RECHAZADA"));

        Vacante actualizada = vacanteRepository.findById(vacante.getId()).orElseThrow();
        assertEquals("Perfil incompleto", actualizada.getMotivoRechazo());
    }

    @Test
    @WithMockUser(username = "coord-pausar-reactivar@test.com", roles = "COORD_PRACTICA")
    void pausarYReactivarVacanteActualizaEstado() throws Exception {
        usuario("coord-pausar-reactivar@test.com", Rol.COORD_PRACTICA, Scope.GLOBAL);
        Facultad facultad = facultad();
        Programa programa = programa(facultad);
        CatalogoItem sector = sector();
        Empresa empresa = empresa("NIT-PAUSA-" + suffix, sector, null);
        Vacante vacante = vacante(empresa, programa, EstadoVacanteEnum.ACTIVA);

        mockMvc.perform(patch("/vacantes/{id}/pausar", vacante.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("PAUSADA"));

        mockMvc.perform(patch("/vacantes/{id}/reactivar", vacante.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ACTIVA"));
    }

    @Test
    @WithMockUser(username = "coord-cupos-vacante@test.com", roles = "COORD_PRACTICA")
    void descontarCupoCompletaVacanteCuandoLlegaACero() throws Exception {
        usuario("coord-cupos-vacante@test.com", Rol.COORD_PRACTICA, Scope.GLOBAL);
        Facultad facultad = facultad();
        Programa programa = programa(facultad);
        CatalogoItem sector = sector();
        Empresa empresa = empresa("NIT-CUPOS-" + suffix, sector, null);
        Vacante vacante = vacante(empresa, programa, EstadoVacanteEnum.ACTIVA);
        vacante.setCuposTotales(1);
        vacante.setCuposDisponibles(1);
        vacanteRepository.save(vacante);

        mockMvc.perform(patch("/vacantes/{id}/descontar-cupo", vacante.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CUPOS_COMPLETOS"))
                .andExpect(jsonPath("$.cuposDisponibles").value(0));
    }

    private Map<String, Object> vacanteRequest(Long empresaId, Long programaId, Long catalogoPracticaId, Long creadoPorId) {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("empresaId", empresaId);
        request.put("programaId", programaId);
        request.put("catalogoPracticaId", catalogoPracticaId);
        request.put("creadoPorId", creadoPorId);
        request.put("correoEmpresa", "empresa-" + suffix + "@test.com");
        request.put("cargo", "Practicante Backend");
        request.put("descripcionPerfil", "Apoyo backend");
        request.put("requisitos", "Java");
        request.put("cuposTotales", 2);
        request.put("area", "QA");
        request.put("modalidad", "REMOTO");
        request.put("fechaInicioDisponibilidad", LocalDate.now().toString());
        request.put("fechaFinDisponibilidad", LocalDate.now().plusMonths(1).toString());
        return request;
    }
}
