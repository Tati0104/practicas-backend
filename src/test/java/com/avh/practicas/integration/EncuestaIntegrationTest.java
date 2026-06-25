package com.avh.practicas.integration;

import com.avh.practicas.cierre.entity.EstadoEncuesta;
import com.avh.practicas.cierre.entity.TipoEncuesta;
import com.avh.practicas.configuracion.entity.Facultad;
import com.avh.practicas.configuracion.entity.Programa;
import com.avh.practicas.configuracion.entity.CatalogoItem;
import com.avh.practicas.empresa.entity.Empresa;
import com.avh.practicas.empresa.entity.TutorEmpresarial;
import com.avh.practicas.estudiante.entity.InstanciaPractica;
import com.avh.practicas.shared.enums.Rol;
import com.avh.practicas.shared.enums.Scope;
import com.avh.practicas.auth.entity.Usuario;
import com.avh.practicas.estudiante.entity.DocenteAsesor;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EncuestaIntegrationTest extends AbstractIntegrationTest {

    @Test
    @WithMockUser(username = "encuesta-get@test.com", roles = "ADMIN")
    void obtenerEncuestaInexistenteRetornaNoContentYSinCrearRegistro() throws Exception {
        usuario("encuesta-get@test.com", Rol.ADMIN, Scope.GLOBAL);
        Facultad facultad = facultad();
        Programa programa = programa(facultad);
        InstanciaPractica practica = practica(programa);

        mockMvc.perform(get("/encuestas/{practicaId}/{tipo}", practica.getId(), TipoEncuesta.ESTUDIANTE))
                .andExpect(status().isNoContent());

        assertEquals(0, encuestaRepository.findByInstanciaPracticaId(practica.getId()).size());
    }

    @Test
    @WithMockUser(username = "encuesta-existente@test.com", roles = "ADMIN")
    void obtenerEncuestaExistenteRetornaEstadoPersistidoSinCrearDuplicados() throws Exception {
        usuario("encuesta-existente@test.com", Rol.ADMIN, Scope.GLOBAL);
        Facultad facultad = facultad();
        Programa programa = programa(facultad);
        InstanciaPractica practica = practica(programa);
        encuesta(practica, TipoEncuesta.ESTUDIANTE, EstadoEncuesta.EN_BORRADOR);

        mockMvc.perform(get("/encuestas/{practicaId}/{tipo}", practica.getId(), TipoEncuesta.ESTUDIANTE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("ESTUDIANTE"))
                .andExpect(jsonPath("$.estado").value("EN_BORRADOR"));

        assertEquals(1, encuestaRepository.findByInstanciaPracticaId(practica.getId()).size());
    }

    @Test
    @WithMockUser(username = "encuesta-repetida@test.com", roles = "ADMIN")
    void obtenerEncuestaVariasVecesNoDuplicaRegistros() throws Exception {
        usuario("encuesta-repetida@test.com", Rol.ADMIN, Scope.GLOBAL);
        Facultad facultad = facultad();
        Programa programa = programa(facultad);
        InstanciaPractica practica = practica(programa);
        encuesta(practica, TipoEncuesta.TUTOR, EstadoEncuesta.PENDIENTE);

        mockMvc.perform(get("/encuestas/{practicaId}/{tipo}", practica.getId(), TipoEncuesta.TUTOR))
                .andExpect(status().isOk());
        mockMvc.perform(get("/encuestas/{practicaId}/{tipo}", practica.getId(), TipoEncuesta.TUTOR))
                .andExpect(status().isOk());

        assertEquals(1, encuestaRepository.findByInstanciaPracticaId(practica.getId()).size());
    }

    @Test
    @WithMockUser(username = "estudiante-propio-encuesta@test.com", roles = "ESTUDIANTE")
    void estudiantePropioPuedeConsultarSuEncuesta() throws Exception {
        Usuario estudiante = usuario("estudiante-propio-encuesta@test.com", Rol.ESTUDIANTE, Scope.PROGRAMA);
        Facultad facultad = facultad();
        Programa programa = programa(facultad);
        InstanciaPractica practica = practica(programa, estudiante);
        encuesta(practica, TipoEncuesta.ESTUDIANTE, EstadoEncuesta.PENDIENTE);

        mockMvc.perform(get("/encuestas/{practicaId}/{tipo}", practica.getId(), TipoEncuesta.ESTUDIANTE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("ESTUDIANTE"));
    }

    @Test
    @WithMockUser(username = "estudiante-ajeno-encuesta@test.com", roles = "ESTUDIANTE")
    void estudianteNoPuedeConsultarEncuestaAjena() throws Exception {
        usuario("estudiante-ajeno-encuesta@test.com", Rol.ESTUDIANTE, Scope.PROGRAMA);
        Usuario propietario = usuario("propietario-" + suffix + "@test.com", Rol.ESTUDIANTE, Scope.PROGRAMA);
        Facultad facultad = facultad();
        Programa programa = programa(facultad);
        InstanciaPractica practica = practica(programa, propietario);
        encuesta(practica, TipoEncuesta.ESTUDIANTE, EstadoEncuesta.PENDIENTE);

        mockMvc.perform(get("/encuestas/{practicaId}/{tipo}", practica.getId(), TipoEncuesta.ESTUDIANTE))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "tutor-propio-encuesta@test.com", roles = "TUTOR_EMPRESARIAL")
    void tutorAsignadoPuedeConsultarEncuestaTutor() throws Exception {
        Usuario tutorUsuario = usuario("tutor-propio-encuesta@test.com", Rol.TUTOR_EMPRESARIAL, Scope.ASIGNADO);
        Facultad facultad = facultad();
        Programa programa = programa(facultad);
        CatalogoItem sector = sector();
        Empresa empresa = empresa("NIT-TUTOR-ENC-" + suffix, sector, null);
        TutorEmpresarial tutor = tutor(empresa, tutorUsuario);
        InstanciaPractica practica = practica(programa, null, empresa, tutor, null);
        encuesta(practica, TipoEncuesta.TUTOR, EstadoEncuesta.PENDIENTE);

        mockMvc.perform(get("/encuestas/{practicaId}/{tipo}", practica.getId(), TipoEncuesta.TUTOR))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("TUTOR"));
    }

    @Test
    @WithMockUser(username = "tutor-ajeno-encuesta@test.com", roles = "TUTOR_EMPRESARIAL")
    void tutorNoAsignadoNoPuedeConsultarEncuestaTutor() throws Exception {
        Usuario tutorUsuario = usuario("tutor-ajeno-encuesta@test.com", Rol.TUTOR_EMPRESARIAL, Scope.ASIGNADO);
        Facultad facultad = facultad();
        Programa programa = programa(facultad);
        CatalogoItem sector = sector();
        Empresa empresa = empresa("NIT-TUTOR-AJENO-" + suffix, sector, null);
        tutor(empresa, tutorUsuario);
        InstanciaPractica practica = practica(programa);
        encuesta(practica, TipoEncuesta.TUTOR, EstadoEncuesta.PENDIENTE);

        mockMvc.perform(get("/encuestas/{practicaId}/{tipo}", practica.getId(), TipoEncuesta.TUTOR))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "docente-encuesta@test.com", roles = "DOCENTE_ASESOR")
    void docenteAsignadoPuedeConsultarEncuestaDePractica() throws Exception {
        Usuario docenteUsuario = usuario("docente-encuesta@test.com", Rol.DOCENTE_ASESOR, Scope.PROGRAMA);
        Facultad facultad = facultad();
        Programa programa = programa(facultad);
        DocenteAsesor docente = docente(programa, docenteUsuario);
        InstanciaPractica practica = practica(programa, null, null, null, docente);
        encuesta(practica, TipoEncuesta.ESTUDIANTE, EstadoEncuesta.PENDIENTE);

        mockMvc.perform(get("/encuestas/{practicaId}/{tipo}", practica.getId(), TipoEncuesta.ESTUDIANTE))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "estudiante-borrador@test.com", roles = "ESTUDIANTE")
    void guardarBorradorEstudiantePropioActualizaEstadoYRespuestas() throws Exception {
        Usuario estudiante = usuario("estudiante-borrador@test.com", Rol.ESTUDIANTE, Scope.PROGRAMA);
        Facultad facultad = facultad();
        Programa programa = programa(facultad);
        InstanciaPractica practica = practica(programa, estudiante);
        var encuesta = encuesta(practica, TipoEncuesta.ESTUDIANTE, EstadoEncuesta.PENDIENTE);

        mockMvc.perform(post("/encuestas/{id}/borrador", encuesta.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("respuestasJson", "{\"satisfaccion\":5}"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EN_BORRADOR"));

        var actualizada = encuestaRepository.findById(encuesta.getId()).orElseThrow();
        assertEquals(EstadoEncuesta.EN_BORRADOR, actualizada.getEstado());
        assertEquals("{\"satisfaccion\":5}", actualizada.getRespuestasJson());
    }

    @Test
    @WithMockUser(username = "estudiante-enviar@test.com", roles = "ESTUDIANTE")
    void enviarEncuestaEstudiantePropioMarcaCompletada() throws Exception {
        Usuario estudiante = usuario("estudiante-enviar@test.com", Rol.ESTUDIANTE, Scope.PROGRAMA);
        Facultad facultad = facultad();
        Programa programa = programa(facultad);
        InstanciaPractica practica = practica(programa, estudiante);
        var encuesta = encuesta(practica, TipoEncuesta.ESTUDIANTE, EstadoEncuesta.EN_BORRADOR);
        encuesta.setRespuestasJson("{\"satisfaccion\":5}");
        encuestaRepository.save(encuesta);

        mockMvc.perform(post("/encuestas/{id}/enviar", encuesta.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("COMPLETADA"));

        assertEquals(EstadoEncuesta.COMPLETADA, encuestaRepository.findById(encuesta.getId()).orElseThrow().getEstado());
    }

    @Test
    @WithMockUser(username = "estudiante-enviar-vacia@test.com", roles = "ESTUDIANTE")
    void enviarEncuestaSinRespuestasRetornaBadRequest() throws Exception {
        Usuario estudiante = usuario("estudiante-enviar-vacia@test.com", Rol.ESTUDIANTE, Scope.PROGRAMA);
        Facultad facultad = facultad();
        Programa programa = programa(facultad);
        InstanciaPractica practica = practica(programa, estudiante);
        var encuesta = encuesta(practica, TipoEncuesta.ESTUDIANTE, EstadoEncuesta.PENDIENTE);
        encuesta.setRespuestasJson("{}");
        encuestaRepository.save(encuesta);

        mockMvc.perform(post("/encuestas/{id}/enviar", encuesta.getId()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "admin-invitar-encuesta@test.com", roles = "ADMIN")
    void adminPuedeInvitarYCrearEncuestaPendiente() throws Exception {
        usuario("admin-invitar-encuesta@test.com", Rol.ADMIN, Scope.GLOBAL);
        Facultad facultad = facultad();
        Programa programa = programa(facultad);
        InstanciaPractica practica = practica(programa);

        mockMvc.perform(post("/encuestas/{practicaId}/{tipo}/invitar", practica.getId(), TipoEncuesta.ESTUDIANTE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.tipo").value("ESTUDIANTE"));

        assertEquals(1, encuestaRepository.findByInstanciaPracticaId(practica.getId()).size());
    }

    @Test
    @WithMockUser(username = "estudiante-invitar-encuesta@test.com", roles = "ESTUDIANTE")
    void estudianteNoPuedeInvitarEncuesta() throws Exception {
        Usuario estudiante = usuario("estudiante-invitar-encuesta@test.com", Rol.ESTUDIANTE, Scope.PROGRAMA);
        Facultad facultad = facultad();
        Programa programa = programa(facultad);
        InstanciaPractica practica = practica(programa, estudiante);

        mockMvc.perform(post("/encuestas/{practicaId}/{tipo}/invitar", practica.getId(), TipoEncuesta.ESTUDIANTE))
                .andExpect(status().isForbidden());

        assertEquals(0, encuestaRepository.findByInstanciaPracticaId(practica.getId()).size());
    }

    @Test
    @WithMockUser(username = "estudiante-recordatorio-encuesta@test.com", roles = "ESTUDIANTE")
    void estudianteNoPuedeEnviarRecordatorioDeEncuesta() throws Exception {
        Usuario estudiante = usuario("estudiante-recordatorio-encuesta@test.com", Rol.ESTUDIANTE, Scope.PROGRAMA);
        Facultad facultad = facultad();
        Programa programa = programa(facultad);
        InstanciaPractica practica = practica(programa, estudiante);
        encuesta(practica, TipoEncuesta.ESTUDIANTE, EstadoEncuesta.PENDIENTE);

        mockMvc.perform(post("/encuestas/{practicaId}/{tipo}/recordatorio", practica.getId(), TipoEncuesta.ESTUDIANTE))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin-recordatorio-sin-encuesta@test.com", roles = "ADMIN")
    void recordatorioSinEncuestaRetornaBadRequest() throws Exception {
        usuario("admin-recordatorio-sin-encuesta@test.com", Rol.ADMIN, Scope.GLOBAL);
        Facultad facultad = facultad();
        Programa programa = programa(facultad);
        InstanciaPractica practica = practica(programa);

        mockMvc.perform(post("/encuestas/{practicaId}/{tipo}/recordatorio", practica.getId(), TipoEncuesta.ESTUDIANTE))
                .andExpect(status().isBadRequest());
    }
}
