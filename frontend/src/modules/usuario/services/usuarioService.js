import http from '../../../shared/services/http';

const usuarioService = {
  listar:    (filtros, page = 0, size = 10) =>
    http.get('/admin/usuarios', { params: { ...filtros, page, size } }),
  crear:     (dto)      => http.post('/admin/usuarios', dto),
  editar:    (id, dto)  => http.put(`/admin/usuarios/${id}`, dto),
  activar:   (id)       => http.patch(`/admin/usuarios/${id}/activar`),
  inactivar: (id)       => http.patch(`/admin/usuarios/${id}/inactivar`)
};

export default usuarioService;