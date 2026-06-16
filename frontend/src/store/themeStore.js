import { create } from 'zustand';
import { persist } from 'zustand/middleware';

const CLAVE_ANONIMO = '__anon__';

export function aplicarModoEnDocumento(modo) {
  const root = document.documentElement;
  if (modo === 'dark') {
    root.classList.add('dark');
  } else {
    root.classList.remove('dark');
  }
}

export function initThemeFromStorage() {
  try {
    const raw = localStorage.getItem('practi-theme');
    if (!raw) {
      aplicarModoEnDocumento('light');
      return;
    }

    const data = JSON.parse(raw);
    const porUsuario = data?.state?.porUsuario ?? {};

    let correo = null;
    const authRaw = localStorage.getItem('auth-storage');
    if (authRaw) {
      const auth = JSON.parse(authRaw);
      correo = auth?.state?.correo ?? null;
    }

    const clave = correo ? correo.toLowerCase() : CLAVE_ANONIMO;
    const modo = porUsuario[clave] ?? 'light';
    aplicarModoEnDocumento(modo);
  } catch {
    aplicarModoEnDocumento('light');
  }
}

const useThemeStore = create(
  persist(
    (set, get) => ({
      porUsuario: {},

      resolverClave: (correo) => (correo ? correo.toLowerCase() : CLAVE_ANONIMO),

      obtenerModo: (correo) => {
        const clave = get().resolverClave(correo);
        return get().porUsuario[clave] ?? 'light';
      },

      establecerModo: (correo, modo) => {
        const clave = get().resolverClave(correo);
        const valor = modo === 'dark' ? 'dark' : 'light';
        set((state) => ({
          porUsuario: { ...state.porUsuario, [clave]: valor },
        }));
        aplicarModoEnDocumento(valor);
      },

      alternarModo: (correo) => {
        const actual = get().obtenerModo(correo);
        get().establecerModo(correo, actual === 'dark' ? 'light' : 'dark');
      },
    }),
    {
      name: 'practi-theme',
      partialize: (state) => ({ porUsuario: state.porUsuario }),
    }
  )
);

export default useThemeStore;
