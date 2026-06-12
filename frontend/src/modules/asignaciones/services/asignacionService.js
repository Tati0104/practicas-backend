// src/modules/asignaciones/services/asignacionService.js

/**
 * Servicio que centraliza todas las llamadas HTTP del módulo Asignaciones.
 * Usa el cliente `http` (Axios) ya configurado con baseURL y token JWT.
 * NUNCA importar axios directamente; siempre usar http.js.
 */
import http from '../../../shared/services/http.js';

const asignacionService = {
  /**
   * Lista asignaciones con paginación y filtros.
   * GET /api/asignaciones?page=&size=&programaId=&estado=&busqueda=
   */
  listar: (params) => http.get('/api/asignaciones', { params }),

  /**
   * Crea una nueva asignación.
   * POST /api/asignaciones
   * Body: { estudianteId, vacanteId }
   */
  crear: (dto) => http.post('/api/asignaciones', dto),

  /**
   * Cancela una asignación por su ID.
   * PATCH /api/asignaciones/{id}/cancelar
   * Body: { motivo }
   */
  cancelar: (id, motivo) => http.patch(`/api/asignaciones/${id}/cancelar`, { motivo }),

  /**
   * Obtiene el detalle completo de una asignación.
   * GET /api/asignaciones/{id}
   */
  obtener: (id) => http.get(`/api/asignaciones/${id}`),

  /**
   * Lista estudiantes aptos para una vacante y programa.
   * GET /api/estudiantes-aptos?programaId=&vacanteId=
   */
  estudiantesAptos: (params) => http.get('/api/estudiantes-aptos', { params }),

  /**
   * Lista vacantes activas filtradas por programa.
   * GET /api/vacantes-activas?programaId=
   */
  vacantesActivas: (params) => http.get('/api/vacantes-activas', { params }),
};

export default asignacionService;
