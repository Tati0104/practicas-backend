import http from './http';

const notificacionService = {
  listar: (params = {}) => http.get('/notificaciones', { params }),

  marcarLeida: (id) => http.patch(`/notificaciones/${id}/leer`),
};

export default notificacionService;
