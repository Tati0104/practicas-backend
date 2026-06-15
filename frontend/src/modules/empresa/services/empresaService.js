import http from '../../../shared/services/http';
import { paramsListado } from '../../../shared/utils/paginacion';

const empresaService = {
  // Catálogo de sectores
  listarSectores: () =>
    http.get('/catalogos', { params: { tipo: 'SECTOR_ECONOMICO', soloActivos: true } }),

  // Empresas
  listar: (filtros = {}) =>
    http.get('/empresas', { params: paramsListado(filtros) }),
  registrar:        (dto)     => http.post('/empresas', JSON.stringify(dto), { headers: { 'Content-Type': 'application/json' } }),
  editar:           (id, dto) => http.put(`/empresas/${id}`, dto),
  activar:          (id)      => http.patch(`/empresas/${id}/activar`),
  inactivar:        (id, motivo) =>
    http.patch(`/empresas/${id}/desactivar`, JSON.stringify({ motivo }), {
      headers: { 'Content-Type': 'application/json' },
    }),
  obtener:          (id)      => http.get(`/empresas/${id}`),

  // Tutores
  listarTutores:    (empresaId) =>
    http.get(`/empresas/${empresaId}/tutores`),
  registrarTutor:   (dto)     => http.post('/tutores', JSON.stringify(dto), { headers: { 'Content-Type': 'application/json' } }),
  editarTutor:      (id, dto) => http.put(`/tutores/${id}`, dto),
  inactivarTutor:   (id)      => http.patch(`/tutores/${id}/inactivar`),

  getVacante: (id)      => http.get(`/vacantes/${id}`),
  listarVacantes: (filtros = {}) =>
    http.get('/vacantes', { params: paramsListado(filtros) }),
  crearVacante:     (dto)     => http.post('/vacantes', JSON.stringify(dto), { headers: { 'Content-Type': 'application/json' } }),
  aprobarVacante:   (id)      => http.patch(`/vacantes/${id}/aprobar`),
  rechazarVacante:  (id, motivo) =>
    http.patch(`/vacantes/${id}/rechazar`, null, { params: { motivo } }),
  pausarVacante:    (id)      => http.patch(`/vacantes/${id}/pausar`),
  cerrarVacante:    (id)      => http.patch(`/vacantes/${id}/cerrar`)
};

export default empresaService;