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

  /**
   * GET /seguimiento/practicas
   * Endpoint desacoplado: no exige programaId, resuelve el scope del usuario
   * autenticado en el backend (ESTUDIANTE ve solo la suya, coordinadores su facultad).
   */
  practicas: ({ programaId, page, size, estado, busqueda, estadoPractica } = {}) =>
    http.get('/seguimiento/practicas', {
      params: {
        programaId: programaId || undefined,
        page,
        size,
        busqueda: busqueda || undefined,
        estadoSeguimiento: estado || undefined,
        estadoPractica: estadoPractica || undefined,
      },
    }),

  /** POST /seguimiento/{practicaId}/observaciones */
  registrarObservacion: (practicaId, { corte, ...body }) =>
    http.post(`/seguimiento/${practicaId}/observaciones`, body, { params: { corte } }),

  /** POST /seguimiento/{practicaId}/avances-tutor */
  registrarAvance: (practicaId, { corte, ...body }) =>
    http.post(`/seguimiento/${practicaId}/avances-tutor`, body, { params: { corte } }),

  /** POST /seguimiento/{practicaId}/bitacora (multipart: descripcion + archivo opcional) */
  registrarBitacora: (practicaId, { descripcion, archivo }, corte = 1) => {
    const form = new FormData();
    form.append('descripcion', descripcion);
    if (archivo) form.append('archivo', archivo);
    return http.post(`/seguimiento/${practicaId}/bitacora`, form, {
      params: { corte },
      headers: { 'Content-Type': 'multipart/form-data' },
    });
  },

  /** GET /seguimiento/bitacora/{bitacoraId}/archivo — devuelve blob para descarga */
  descargarArchivoBitacora: (bitacoraId) =>
    http.get(`/seguimiento/bitacora/${bitacoraId}/archivo`, { responseType: 'blob' }),

  /** GET /seguimiento/alertas */
  alertas: () => http.get('/seguimiento/alertas'),

  /** PATCH /vinculaciones/alertas/{id}/resolver */
  marcarAlertaLeida: (id) => http.patch(`/vinculaciones/alertas/${id}/resolver`),

  /** GET /seguimiento/{practicaId} */
  obtenerDetallePractica: (id) => http.get(`/seguimiento/${id}`),

  /** GET /seguimiento/estudiantes/{estudianteId}/practicas */
  historialEstudiante: (estudianteId) =>
    http.get(`/seguimiento/estudiantes/${estudianteId}/practicas`),

  /** GET /seguimiento/{practicaId}/bitacora */
  obtenerBitacoras: (practicaId) => http.get(`/seguimiento/${practicaId}/bitacora`),
};

export default seguimientoService;
