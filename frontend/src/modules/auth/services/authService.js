import http from '../../../shared/services/http';

const authService = {
  login: (correo, password) =>
    http.post('/auth/login', { correo, password }),

  recuperarPassword: (correo) =>
    http.post('/auth/recuperar', null, { params: { correo } }),

  resetearPassword: (token, nuevaPassword) =>
    http.post('/auth/resetear', null, { params: { token, nuevaPassword } }),

  cambiarPassword: (id, nuevaPassword) =>
    http.post('/auth/cambiar-password', null, { params: { id, nuevaPassword } })
};

export default authService;