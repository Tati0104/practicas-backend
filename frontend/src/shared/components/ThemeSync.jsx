import { useEffect } from 'react';
import useAuthStore from '@/store/authStore';
import useThemeStore, { aplicarModoEnDocumento } from '@/store/themeStore';

/** Aplica el tema guardado del usuario al cambiar sesión. */
export default function ThemeSync() {
  const correo = useAuthStore((state) => state.correo);
  const porUsuario = useThemeStore((state) => state.porUsuario);

  useEffect(() => {
    const store = useThemeStore.getState();
    const clave = store.resolverClave(correo);

    if (correo && !store.porUsuario[clave] && store.porUsuario.__anon__) {
      store.establecerModo(correo, store.porUsuario.__anon__);
      return;
    }

    const modo = store.obtenerModo(correo);
    aplicarModoEnDocumento(modo);
  }, [correo, porUsuario]);

  return null;
}
