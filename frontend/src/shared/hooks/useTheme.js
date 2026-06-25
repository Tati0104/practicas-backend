import useAuthStore from '@/store/authStore';
import useThemeStore from '@/store/themeStore';

const CLAVE_ANONIMO = '__anon__';

export default function useTheme() {
  const correo = useAuthStore((state) => state.correo);
  const clave = correo ? correo.toLowerCase() : CLAVE_ANONIMO;
  const modo = useThemeStore((state) => state.porUsuario[clave] ?? 'light');
  const alternarModo = useThemeStore((state) => state.alternarModo);
  const establecerModo = useThemeStore((state) => state.establecerModo);

  return {
    modo,
    esOscuro: modo === 'dark',
    alternar: () => alternarModo(correo),
    establecerClaro: () => establecerModo(correo, 'light'),
    establecerOscuro: () => establecerModo(correo, 'dark'),
  };
}
