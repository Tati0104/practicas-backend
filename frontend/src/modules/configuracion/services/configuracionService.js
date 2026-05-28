import http from '../../../shared/services/http';

const configuracionService = {
  // Facultades
  listarFacultades: ()           => http.get('/facultades'),
  crearFacultad:    (dto)        => http.post('/facultades', dto),
  editarFacultad:   (id, dto)    => http.put(`/facultades/${id}`, dto),
  activarFacultad:  (id)         => http.patch(`/facultades/${id}/activar`),
  inactivarFacultad:(id)         => http.patch(`/facultades/${id}/inactivar`),

  // Programas
  listarProgramas:  (facultadId) => http.get('/programas', { params: { facultadId } }),
  crearPrograma:    (dto)        => http.post('/programas', dto),
  editarPrograma:   (id, dto)    => http.put(`/programas/${id}`, dto),
  inactivarPrograma:(id)         => http.patch(`/programas/${id}/inactivar`),

  // Catálogos maestros
  listarCatalogos:  (tipo)       => http.get('/catalogos', { params: { tipo } }),
  crearCatalogo:    (dto)        => http.post('/catalogos', dto),
  activarCatalogo:  (id)         => http.patch(`/catalogos/${id}/activar`),
  inactivarCatalogo:(id)         => http.patch(`/catalogos/${id}/desactivar`),

  // Configuración de parámetros
  obtenerConfig:    (programaId) => http.get(`/configuracion/programas/${programaId}`),
  guardarConfig:    (programaId, dto) => http.put(`/configuracion/programas/${programaId}`, dto)
};

export default configuracionService;