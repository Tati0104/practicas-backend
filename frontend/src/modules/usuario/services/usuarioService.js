import http from '../../../shared/services/http';
import { paramsListado } from '../../../shared/utils/paginacion';

const usuarioService = {
  listar: (filtros = {}) =>
    http.get('/admin/usuarios', { params: paramsListado(filtros) }),
  crear:     (dto)      => http.post('/admin/usuarios', dto),
  editar:    (id, dto)  => http.put(`/admin/usuarios/${id}`, dto),
  activar:   (id)       => http.patch(`/admin/usuarios/${id}/activar`),
  inactivar: (id)       => http.patch(`/admin/usuarios/${id}/inactivar`)
};

export default usuarioService;