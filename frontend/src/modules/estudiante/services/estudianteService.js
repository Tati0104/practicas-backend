import http from '../../../shared/services/http';
import { paramsListado } from '../../../shared/utils/paginacion';

const estudianteService = {
  listar: (filtros = {}) =>
    http.get('/estudiantes', { params: paramsListado(filtros) }),
  obtenerPorId:    (id)       => http.get(`/estudiantes/${id}`),
  registrar:       (dto)      => http.post('/estudiantes', JSON.stringify(dto), { headers: { 'Content-Type': 'application/json' } }),
  editar:          (id, dto)  => http.put(`/estudiantes/${id}`, dto),
  marcarApto:      (id)       => http.patch(`/estudiantes/${id}/aptitud`, { estado: 'APTO' }),
  marcarNoApto:    (id, motivo) => http.patch(`/estudiantes/${id}/aptitud`, { estado: 'NO_APTO', motivo }),
  obtenerExpediente: (id)     => http.get(`/expedientes/${id}`),
  importarExcel:   (archivo)  => {
    const form = new FormData();
    form.append('archivo', archivo);
    return http.post('/estudiantes/importar', form,
      { headers: { 'Content-Type': 'multipart/form-data' } });
  }
};

export default estudianteService;