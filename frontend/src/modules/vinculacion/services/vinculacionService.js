// src/modules/vinculacion/services/vinculacionService.js

/**
 * Servicio HTTP del módulo Vinculación y Documentos.
 *
 * Endpoints:
 *   POST  /api/vinculacion/{asignacionId}/carta     → sube carta (multipart)
 *   POST  /api/vinculacion/{asignacionId}/convenio  → sube convenio (multipart)
 *   PATCH /api/convenios/{id}/firma                 → confirma firma de un firmante
 *   GET   /api/practicas/{id}/documentos            → obtiene docs de una práctica
 *   GET   /api/documentos/{id}/descargar            → descarga binario de un doc
 *
 * REGLA: Solo importar http.js. Nunca importar axios directamente aquí.
 */
import http from '../../../shared/services/http.js';

const vinculacionService = {
  /**
   * Sube la carta de presentación para una asignación.
   * El archivo va como multipart/form-data bajo la clave "archivo".
   */
  subirCarta: (asignacionId, archivo) => {
    const form = new FormData();
    form.append('archivo', archivo);
    return http.post(`/api/vinculacion/${asignacionId}/carta`, form, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
  },

  /**
   * Sube el convenio de práctica para una asignación.
   * El archivo va como multipart/form-data bajo la clave "archivo".
   */
  subirConvenio: (asignacionId, archivo) => {
    const form = new FormData();
    form.append('archivo', archivo);
    return http.post(`/api/vinculacion/${asignacionId}/convenio`, form, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
  },

  /**
   * Confirma la firma de un firmante sobre un convenio.
   * tipoFirmante: 'COORDINADOR' | 'TUTOR' | 'ESTUDIANTE'
   */
  confirmarFirma: (convenioId, tipoFirmante) =>
    http.patch(`/api/convenios/${convenioId}/firma`, { tipoFirmante }),

  /**
   * Obtiene la lista de documentos de una práctica.
   * Devuelve array de { id, tipo, nombre, estado, firmas[] }
   */
  obtenerDocumentos: (practicaId) =>
    http.get(`/api/practicas/${practicaId}/documentos`),

  /**
   * Descarga el binario de un documento.
   * responseType 'blob' para manejar el archivo en el navegador.
   */
  descargarDocumento: (documentoId) =>
    http.get(`/api/documentos/${documentoId}/descargar`, {
      responseType: 'blob',
    }),
};

export default vinculacionService;
