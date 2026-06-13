// src/modules/vinculacion/services/vinculacionService.js

/**
 * Servicio HTTP del módulo Vinculación y Documentos.
 */
import http from '../../../shared/services/http.js';

const vinculacionService = {
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

  obtenerDocumentos: (practicaId) =>
    http.get(`/practicas/${practicaId}/documentos`),

  descargarDocumento: (documentoId) =>
    http.get(`/documentos/${documentoId}/descargar`, {
      responseType: 'blob',
    }),
};

export default vinculacionService;
