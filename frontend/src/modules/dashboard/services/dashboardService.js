import http from '../../../shared/services/http';

const dashboardService = {
  obtenerResumen: () =>
    http.get('/dashboard'),

  obtenerAlertas: (page = 0, size = 10) =>
    http.get('/dashboard/alertas', { params: { page, size } }),

  marcarAlertaLeida: (id) =>
    http.patch(`/dashboard/alertas/${id}/leer`),

  obtenerFiltrosDisponibles: () =>
    http.get('/dashboard/filtros-disponibles'),

  obtenerPanelEstudiante: () =>
    http.get('/dashboard/panel-estudiante'),
};

export default dashboardService;