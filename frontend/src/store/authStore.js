import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import {
  decodificarToken,
  eliminarToken,
  extraerDatosSesion,
  guardarToken,
  obtenerTokenAlmacenado,
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

const useAuthStore = create(
  persist(
    (set, get) => ({
      ...estadoInicial,

      iniciarSesion: (token, datosLogin = {}) => {
        if (!token || tokenExpirado(token)) {
          get().cerrarSesion();
          return;
        }

        guardarToken(token);
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
        eliminarToken();
        localStorage.removeItem('token');
        localStorage.removeItem('usuario');
        set({ ...estadoInicial });
      },

      rehidratarDesdeToken: () => {
        const token = obtenerTokenAlmacenado();

        if (!token || tokenExpirado(token)) {
          get().cerrarSesion();
          return false;
        }

        const claims = extraerDatosSesion(token);
        if (!claims) {
          get().cerrarSesion();
          return false;
        }

        set({
          token,
          usuario: { ...claims, token, ...(get().usuario ?? {}) },
          rol: get().rol ?? claims.rol,
          scope: get().scope ?? claims.scope,
          facultadId: get().facultadId ?? claims.facultadId,
          programaId: get().programaId ?? claims.programaId,
          nombre: get().nombre ?? claims.nombre,
          correo: get().correo ?? claims.correo,
        });

        return true;
      },

      tieneRol: (rol) => get().rol === rol,

      // Compatibilidad con código existente que usa login/logout
      login: (token, usuario) => get().iniciarSesion(token, usuario),
      logout: () => get().cerrarSesion(),
    }),
    {
      name: 'auth-storage',
      partialize: (state) => ({
        token: state.token,
        usuario: state.usuario,
        rol: state.rol,
        scope: state.scope,
        facultadId: state.facultadId,
        programaId: state.programaId,
        nombre: state.nombre,
        correo: state.correo,
      }),
    }
  )
);

export default useAuthStore;
