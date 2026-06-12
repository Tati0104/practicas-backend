// src/modules/seguimiento/services/seguimientoService.js

/**
 * Servicio que centraliza todas las llamadas HTTP del módulo Seguimiento.
 * Usa el cliente `http` (Axios) configurado con baseURL y token JWT.
 */
import http from '../../../shared/services/http.js';

const seguimientoService = {
  /** GET /api/seguimiento/tablero?programaId=&docenteId=&estado= */
  tablero: (params) => http.get('/api/seguimiento/tablero', { params }),

  /** POST /api/seguimiento/observaciones — { practicaId, texto } */
  registrarObservacion: (dto) => http.post('/api/seguimiento/observaciones', dto),

  /** POST /api/seguimiento/avances-tutor — { practicaId, descripcion, porcentaje } */
  registrarAvance: (dto) => http.post('/api/seguimiento/avances-tutor', dto),

  /** POST /api/seguimiento/bitacora — { practicaId, actividades, aprendizajes } */
  registrarBitacora: (dto) => http.post('/api/seguimiento/bitacora', dto),

  /** GET /api/alertas?practicaId=&leida= */
  alertas: (params) => http.get('/api/alertas', { params }),

  /** PATCH /api/alertas/{id}/leer */
  marcarAlertaLeida: (id) => http.patch(`/api/alertas/${id}/leer`),
};

export default seguimientoService;
