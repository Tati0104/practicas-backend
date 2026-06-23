import { create } from 'zustand';
import {
  extraerDatosSesion,
  limpiarAlmacenamientoSesion,
  tokenExpirado,
} from '@/modules/auth/utils/jwt';

const estadoInicial = {
  token: null,
  usuario: null,
  rol: null,
  scope: null,
  facultadId: null,
  programaId: null,
  nombre: null,
  correo: null,
};

const useAuthStore = create((set, get) => ({
  ...estadoInicial,

  iniciarSesion: (token, datosLogin = {}) => {
    if (!token || tokenExpirado(token)) {
      get().cerrarSesion();
      return;
    }

    const claims = extraerDatosSesion(token) ?? {};

    const usuario = {
      ...claims,
      ...datosLogin,
      token,
    };

    set({
      token,
      usuario,
      rol: datosLogin.rol ?? claims.rol ?? null,
      scope: datosLogin.scope ?? claims.scope ?? null,
      facultadId: datosLogin.facultadId ?? claims.facultadId ?? null,
      programaId: datosLogin.programaId ?? claims.programaId ?? null,
      nombre: datosLogin.nombre ?? claims.nombre ?? null,
      correo: claims.correo ?? datosLogin.correo ?? null,
    });
  },

  cerrarSesion: () => {
    limpiarAlmacenamientoSesion();
    set({ ...estadoInicial });
  },

  tieneRol: (rol) => get().rol === rol,

  login: (token, usuario) => get().iniciarSesion(token, usuario),
  logout: () => get().cerrarSesion(),
}));

export default useAuthStore;
