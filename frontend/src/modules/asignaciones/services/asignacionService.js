// src/modules/asignaciones/services/asignacionService.js

/**
 * Servicio que centraliza todas las llamadas HTTP del módulo Asignaciones.
 * Usa el cliente `http` (Axios) ya configurado con baseURL y token JWT.
 */
import http from '../../../shared/services/http.js';
import { paramsListado } from '../../../shared/utils/paginacion.js';
import { normalizarAsignacion, normalizarPaginaAsignaciones } from '../utils/asignacionMapper.js';

const asignacionService = {
  /** GET /asignaciones?page=&size=&estado=&estudianteId=&vacanteId= */
  listar: async (filtros) => {
    const resp = await http.get('/asignaciones', { params: paramsListado(filtros) });
    const pagina = resp.data?.data ?? resp.data;
    return { ...resp, data: normalizarPaginaAsignaciones(pagina) };
  },

  /** POST /asignaciones */
  crear: (dto) => http.post('/asignaciones', dto),

  /** PATCH /asignaciones/{id}/cancelar */
  cancelar: (id, body) => http.patch(`/asignaciones/${id}/cancelar`, body),

  /** GET /asignaciones/{id} */
  obtener: async (id) => {
    const resp = await http.get(`/asignaciones/${id}`);
    const payload = resp.data?.data ?? resp.data;
    return { ...resp, data: normalizarAsignacion(payload) };
  },

  /** GET /asignaciones/estudiantes-aptos?programaId= */
  estudiantesAptos: (params) => http.get('/asignaciones/estudiantes-aptos', { params }),

  /** GET /asignaciones/vacantes-activas?programaId= */
  vacantesActivas: (params) => http.get('/asignaciones/vacantes-activas', { params }),
};

export default asignacionService;
