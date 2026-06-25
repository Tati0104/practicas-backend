import http from '../../../shared/services/http';

const configuracionService = {
  // Facultades
  listarFacultades: ()           => http.get('/facultades'),
  crearFacultad:    (dto)        => http.post('/facultades', dto),
  editarFacultad:   (id, dto)    => http.put(`/facultades/${id}`, dto),
  activarFacultad:  (id)         => http.patch(`/facultades/${id}/activar`),
  inactivarFacultad:(id)         => http.patch(`/facultades/${id}/desactivar`),

  // Programas
  listarProgramas:  (facultadId) => http.get('/programas', { params: { facultadId } }),
  crearPrograma:    (dto)        => http.post('/programas', dto),
  editarPrograma:   (id, dto)    => http.put(`/programas/${id}`, dto),
  inactivarPrograma:(id)         => http.patch(`/programas/${id}/desactivar`),

  // Catálogos maestros
  listarCatalogos:  (tipo)       => http.get('/catalogos', { params: { tipo } }),
  crearCatalogo:    (dto)        => http.post('/catalogos', dto),
  activarCatalogo:  (id)         => http.patch(`/catalogos/${id}/activar`),
  inactivarCatalogo:(id)         => http.patch(`/catalogos/${id}/desactivar`),

  // Configuración de parámetros
  obtenerConfig:    (programaId) => http.get(`/configuracion/programas/${programaId}`),
  guardarConfig:    (programaId, dto) => http.put(`/configuracion/programas/${programaId}`, dto),

  // Catálogo de prácticas
  listarCatalogoPracticas: (programaId) =>
    http.get('/configuracion/catalogo', programaId ? { params: { programaId } } : {}),
  crearCatalogoPractica:     (dto)      => http.post('/configuracion/catalogo', dto),
  editarCatalogoPractica:    (id, dto)  => http.put(`/configuracion/catalogo/${id}`, dto),
  activarCatalogoPractica:   (id)       => http.patch(`/configuracion/catalogo/${id}/activar`),
  desactivarCatalogoPractica:(id)       => http.patch(`/configuracion/catalogo/${id}/desactivar`),
};

export default configuracionService;