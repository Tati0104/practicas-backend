// src/modules/seguimiento/services/seguimientoService.js

/**
 * Servicio que centraliza todas las llamadas HTTP del módulo Seguimiento.
 */
import http from '../../../shared/services/http.js';

const seguimientoService = {
  /**
   * GET /seguimiento/tablero
   * Mapea filtros del frontend a los parámetros del backend.
   */
  tablero: (params = {}) => {
    const { docenteId, estado, busqueda, ...rest } = params;
    return http.get('/seguimiento/tablero', {
      params: {
        ...rest,
        docente: docenteId || undefined,
        estadoSeguimiento: estado || undefined,
        empresa: busqueda || undefined,
      },
    });
  },

  /** POST /seguimiento/{practicaId}/observaciones */
  registrarObservacion: (practicaId, dto) =>
    http.post(`/seguimiento/${practicaId}/observaciones`, dto),

  /** POST /seguimiento/{practicaId}/avances-tutor */
  registrarAvance: (practicaId, dto) =>
    http.post(`/seguimiento/${practicaId}/avances-tutor`, dto),

  /** POST /seguimiento/{practicaId}/bitacora */
  registrarBitacora: (practicaId, dto, corte = 1) =>
    http.post(`/seguimiento/${practicaId}/bitacora`, dto, { params: { corte } }),

  /** GET /seguimiento/alertas */
  alertas: () => http.get('/seguimiento/alertas'),

  /** PATCH /vinculaciones/alertas/{id}/resolver */
  marcarAlertaLeida: (id) => http.patch(`/vinculaciones/alertas/${id}/resolver`),

  /** GET /seguimiento/{practicaId} */
  obtenerDetallePractica: (id) => http.get(`/seguimiento/${id}`),
};

export default seguimientoService;
