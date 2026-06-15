// src/modules/vinculacion/services/vinculacionService.js

import http from '../../../shared/services/http.js';
import { paramsListado } from '../../../shared/utils/paginacion.js';

const vinculacionService = {
  listar: (filtros) => http.get('/vinculaciones', { params: paramsListado(filtros) }),

  obtenerDocumentos: (asignacionId) =>
    http.get(`/vinculaciones/asignaciones/${asignacionId}/documentos`),

  obtenerDocumentosPractica: (practicaId) =>
    http.get(`/practicas/${practicaId}/documentos`),

  obtenerEstudiantePractica: (practicaId) =>
    http.get(`/practicas/${practicaId}/estudiante`),

  subirDocumento: (asignacionId, categoria, archivo) => {
    const form = new FormData();
    form.append('archivo', archivo);
    return http.post(
      `/vinculaciones/asignaciones/${asignacionId}/documentos/${categoria}`,
      form,
      { headers: { 'Content-Type': 'multipart/form-data' } }
    );
  },

  subirCarta: (asignacionId, archivo) => {
    const form = new FormData();
    form.append('archivo', archivo);
    return http.post(`/vinculaciones/asignaciones/${asignacionId}/carta`, form, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
  },

  subirConvenio: (asignacionId, archivo) => {
    const form = new FormData();
    form.append('archivo', archivo);
    return http.post(`/vinculaciones/asignaciones/${asignacionId}/convenio`, form, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
  },

  confirmarFirma: (convenioId, rol) =>
    http.post(`/vinculaciones/convenios/${convenioId}/firmas/${rol}`),

  descargarDocumento: (documentoId) =>
    http.get(`/vinculaciones/documentos/${documentoId}/descargar`, {
      responseType: 'blob',
    }),
};

export default vinculacionService;
