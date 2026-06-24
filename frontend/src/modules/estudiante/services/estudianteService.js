import http from '../../../shared/services/http';
import { paramsListado } from '../../../shared/utils/paginacion';

const estudianteService = {
  listar: (filtros = {}) => {
    const { estadoAptitud, ...rest } = filtros;
    const params = paramsListado({
      ...rest,
      aptitud: estadoAptitud ?? rest.aptitud,
    });
    return http.get('/estudiantes', { params });
  },
  obtenerPorId:    (id)       => http.get(`/estudiantes/${id}`),
  registrar:       (dto)      => http.post('/estudiantes', JSON.stringify(dto), { headers: { 'Content-Type': 'application/json' } }),
  editar:          (id, dto)  => http.put(`/estudiantes/${id}`, dto),
  eliminar:        (id)       => http.delete(`/estudiantes/${id}`),
  marcarApto:      (id, numeroPractica) =>
    http.patch(`/estudiantes/${id}/aptitud`, {
      aptitud: 'APTO',
      ...(numeroPractica != null ? { numeroPractica } : {}),
    }),
  marcarNoApto:    (id, motivo) => http.patch(`/estudiantes/${id}/aptitud`, { aptitud: 'NO_APTO', motivo }),
  obtenerExpediente: (id)     => http.get(`/expedientes/${id}`),
  importarExcel:   (archivo)  => {
    const form = new FormData();
    form.append('archivo', archivo);
    return http.post('/estudiantes/importar', form,
      { headers: { 'Content-Type': 'multipart/form-data' } });
  },
  subirDocumento:  (id, formData) => {
    return http.post(`/estudiantes/${id}/documentos`, formData,
      { headers: { 'Content-Type': 'multipart/form-data' } });
  }
};

export default estudianteService;