import http from '@/shared/services/http';

const plantillaService = {
  obtener: (tipoEvento) => http.get(`/admin/plantillas/${tipoEvento}`),
  guardar: (tipoEvento, datos) => http.put(`/admin/plantillas/${tipoEvento}`, datos),
};

export default plantillaService;
