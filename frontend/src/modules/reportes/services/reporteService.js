import http from '@/shared/services/http';

const reporteService = {
  obtenerResumen: () => http.get('/reportes/resumen'),
};

export default reporteService;
