import http from '../../../shared/services/http';

const docentesAsesoresService = {
  listarPorPrograma: async (programaId) => {
    let url = '/docentes-asesores';
    if (programaId) {
      url += `?programaId=${programaId}`;
    }
    const response = await http.get(url);
    return response.data;
  },

  obtener: async (id) => {
    const response = await http.get(`/docentes-asesores/${id}`);
    return response.data;
  },

  crear: async (docente) => {
    const response = await http.post('/docentes-asesores', docente);
    return response.data;
  },

  actualizar: async (id, docente) => {
    const response = await http.put(`/docentes-asesores/${id}`, docente);
    return response.data;
  },

  cambiarEstado: async (id, activo) => {
    if (activo) {
      const response = await http.patch(`/docentes-asesores/${id}/activar`);
      return response.data;
    } else {
      const response = await http.patch(`/docentes-asesores/${id}/inactivar`);
      return response.data;
    }
  }
};

export default docentesAsesoresService;
