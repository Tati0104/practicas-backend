package com.avh.practicas.integration;

import com.avh.practicas.cierre.entity.EstadoEncuesta;
import com.avh.practicas.cierre.entity.TipoEncuesta;
import com.avh.practicas.configuracion.entity.Facultad;
import com.avh.practicas.configuracion.entity.Programa;
import com.avh.practicas.estudiante.entity.InstanciaPractica;
import com.avh.practicas.shared.enums.Rol;
import com.avh.practicas.shared.enums.Scope;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.Map;

import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CierreIntegrationTest extends AbstractIntegrationTest {

    @Test
    @WithMockUser(username = "coord-cierre@test.com", roles = "COORD_PRACTICA")
    void checklist_rolPermitidoConsultaSinCrearEncuestas() throws Exception {
        usuario("coord-cierre@test.com", Rol.COORD_PRACTICA, Scope.GLOBAL);
        Facultad facultad = facultad();
        Programa programa = programa(facultad);
        InstanciaPractica practica = practica(programa);

        mockMvc.perform(get("/cierres/{practicaId}/checklist", practica.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.practicaId").value(practica.getId()))
                .andExpect(jsonPath("$.data.grupos[*].items[*].tipoEncuesta", hasItem("TUTOR")))
                .andExpect(jsonPath("$.data.grupos[*].items[*].tipoEncuesta", hasItem("ESTUDIANTE")));

        assertEquals(0, encuestaRepository.findByInstanciaPracticaId(practica.getId()).size());
    }

    @Test
    @WithMockUser(username = "estudiante-cierre@test.com", roles = "ESTUDIANTE")
    void checklist_rolNoPermitidoRespondeForbidden() throws Exception {
        Facultad facultad = facultad();
        Programa programa = programa(facultad);
        InstanciaPractica practica = practica(programa);

        mockMvc.perform(get("/cierres/{practicaId}/checklist", practica.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "secretaria-cierre@test.com", roles = "SECRETARIA")
    void checklistMuestraEstadoDeEncuestasExistentes() throws Exception {
        usuario("secretaria-cierre@test.com", Rol.SECRETARIA, Scope.GLOBAL);
        Facultad facultad = facultad();
        Programa programa = programa(facultad);
        InstanciaPractica practica = practica(programa);
        encuesta(practica, TipoEncuesta.TUTOR, EstadoEncuesta.COMPLETADA);

        mockMvc.perform(get("/cierres/{practicaId}/checklist", practica.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.grupos[*].items[?(@.tipoEncuesta == 'TUTOR')].estadoEncuesta",
                        hasItem("COMPLETADA")));

        assertEquals(1, encuestaRepository.findByInstanciaPracticaId(practica.getId()).size());
    }

    @Test
    @WithMockUser(username = "admin-checklist@test.com", roles = "ADMIN")
    void adminPuedeConsultarChecklist() throws Exception {
        usuario("admin-checklist@test.com", Rol.ADMIN, Scope.GLOBAL);
        Facultad facultad = facultad();
        Programa programa = programa(facultad);
        InstanciaPractica practica = practica(programa);

        mockMvc.perform(get("/cierres/{practicaId}/checklist", practica.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.practicaId").value(practica.getId()));
    }

    @Test
    @WithMockUser(username = "secretaria-checklist@test.com", roles = "SECRETARIA")
    void secretariaPuedeConsultarChecklist() throws Exception {
        usuario("secretaria-checklist@test.com", Rol.SECRETARIA, Scope.GLOBAL);
        Facultad facultad = facultad();
        Programa programa = programa(facultad);
        InstanciaPractica practica = practica(programa);

        mockMvc.perform(get("/cierres/{practicaId}/checklist", practica.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.habilitarBotonCierre").value(false));
    }

    @Test
    @WithMockUser(username = "empresa-checklist@test.com", roles = "EMPRESA")
    void empresaNoPuedeConsultarChecklist() throws Exception {
        Facultad facultad = facultad();
        Programa programa = programa(facultad);
        InstanciaPractica practica = practica(programa);

        mockMvc.perform(get("/cierres/{practicaId}/checklist", practica.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "tutor-checklist@test.com", roles = "TUTOR_EMPRESARIAL")
    void tutorNoPuedeConsultarChecklist() throws Exception {
        Facultad facultad = facultad();
        Programa programa = programa(facultad);
        InstanciaPractica practica = practica(programa);

        mockMvc.perform(get("/cierres/{practicaId}/checklist", practica.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "docente-checklist@test.com", roles = "DOCENTE_ASESOR")
    void docenteNoPuedeConsultarChecklist() throws Exception {
        Facultad facultad = facultad();
        Programa programa = programa(facultad);
        InstanciaPractica practica = practica(programa);

        mockMvc.perform(get("/cierres/{practicaId}/checklist", practica.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    void checklistSinAutenticacionRespondeForbidden() throws Exception {
        Facultad facultad = facultad();
        Programa programa = programa(facultad);
        InstanciaPractica practica = practica(programa);

        mockMvc.perform(get("/cierres/{practicaId}/checklist", practica.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin-checklist-pendiente@test.com", roles = "ADMIN")
    void checklistMuestraEncuestaPendienteSiExiste() throws Exception {
        usuario("admin-checklist-pendiente@test.com", Rol.ADMIN, Scope.GLOBAL);
        Facultad facultad = facultad();
        Programa programa = programa(facultad);
        InstanciaPractica practica = practica(programa);
        encuesta(practica, TipoEncuesta.ESTUDIANTE, EstadoEncuesta.PENDIENTE);

        mockMvc.perform(get("/cierres/{practicaId}/checklist", practica.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.grupos[*].items[?(@.tipoEncuesta == 'ESTUDIANTE')].estadoEncuesta",
                        hasItem("PENDIENTE")));

        assertEquals(1, encuestaRepository.findByInstanciaPracticaId(practica.getId()).size());
    }

    @Test
    @WithMockUser(username = "coord-ejecutar-incompleto@test.com", roles = "COORD_PRACTICA")
    void ejecutarCierreConChecklistIncompletoRespondeConflict() throws Exception {
        usuario("coord-ejecutar-incompleto@test.com", Rol.COORD_PRACTICA, Scope.GLOBAL);
        Facultad facultad = facultad();
        Programa programa = programa(facultad);
        InstanciaPractica practica = practica(programa);

        mockMvc.perform(post("/cierres/{practicaId}/ejecutar", practica.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("confirmacion", true))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(username = "coord-ejecutar-sin-confirmar@test.com", roles = "COORD_PRACTICA")
    void ejecutarCierreSinConfirmacionValidaRespondeBadRequest() throws Exception {
        usuario("coord-ejecutar-sin-confirmar@test.com", Rol.COORD_PRACTICA, Scope.GLOBAL);
        Facultad facultad = facultad();
        Programa programa = programa(facultad);
        InstanciaPractica practica = practica(programa);

        mockMvc.perform(post("/cierres/{practicaId}/ejecutar", practica.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("confirmacion", false))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "admin-ejecutar-cierre@test.com", roles = "ADMIN")
    void adminNoPuedeEjecutarCierrePorReglaActual() throws Exception {
        usuario("admin-ejecutar-cierre@test.com", Rol.ADMIN, Scope.GLOBAL);
        Facultad facultad = facultad();
        Programa programa = programa(facultad);
        InstanciaPractica practica = practica(programa);

        mockMvc.perform(post("/cierres/{practicaId}/ejecutar", practica.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("confirmacion", true))))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "secretaria-ejecutar-cierre@test.com", roles = "SECRETARIA")
    void secretariaNoPuedeEjecutarCierre() throws Exception {
        usuario("secretaria-ejecutar-cierre@test.com", Rol.SECRETARIA, Scope.GLOBAL);
        Facultad facultad = facultad();
        Programa programa = programa(facultad);
        InstanciaPractica practica = practica(programa);

        mockMvc.perform(post("/cierres/{practicaId}/ejecutar", practica.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("confirmacion", true))))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin-recordatorio-cierre@test.com", roles = "ADMIN")
    void recordatorioDesdeCierreSinEncuestaRetornaBadRequest() throws Exception {
        usuario("admin-recordatorio-cierre@test.com", Rol.ADMIN, Scope.GLOBAL);
        Facultad facultad = facultad();
        Programa programa = programa(facultad);
        InstanciaPractica practica = practica(programa);

        mockMvc.perform(post("/cierres/{practicaId}/recordatorio/{tipo}", practica.getId(), TipoEncuesta.ESTUDIANTE))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(username = "estudiante-recordatorio-cierre@test.com", roles = "ESTUDIANTE")
    void estudianteNoPuedeEnviarRecordatorioDesdeCierre() throws Exception {
        Facultad facultad = facultad();
        Programa programa = programa(facultad);
        InstanciaPractica practica = practica(programa);

        mockMvc.perform(post("/cierres/{practicaId}/recordatorio/{tipo}", practica.getId(), TipoEncuesta.ESTUDIANTE))
                .andExpect(status().isForbidden());
    }
}
