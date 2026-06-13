// src/modules/asignaciones/services/asignacionService.js

/**
 * Servicio que centraliza todas las llamadas HTTP del módulo Asignaciones.
 * Usa el cliente `http` (Axios) ya configurado con baseURL y token JWT.
 */
import http from '../../../shared/services/http.js';

const asignacionService = {
  /** GET /asignaciones?page=&size=&estado=&estudianteId=&vacanteId= */
  listar: (params) => http.get('/asignaciones', { params }),

  /** POST /asignaciones */
  crear: (dto) => http.post('/asignaciones', dto),

  /** PATCH /asignaciones/{id}/cancelar */
  cancelar: (id, motivo) => http.patch(`/asignaciones/${id}/cancelar`, { motivo }),

  /** GET /asignaciones/{id} */
  obtener: (id) => http.get(`/asignaciones/${id}`),

  /** GET /asignaciones/estudiantes-aptos?programaId= */
  estudiantesAptos: (params) => http.get('/asignaciones/estudiantes-aptos', { params }),

  /** GET /asignaciones/vacantes-activas?programaId= */
  vacantesActivas: (params) => http.get('/asignaciones/vacantes-activas', { params }),
};

export default asignacionService;
