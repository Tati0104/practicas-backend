import { useMemo } from 'react';
import useAuth from './useAuth';
import { obtenerMenuPorRol } from '../config/navegacion';

/**
 * Hook de presentación: expone el menú lateral filtrado por rol del usuario.
 */
export default function useNavegacion() {
  const { rol, usuario } = useAuth();
  const rolActivo = rol ?? usuario?.rol;

  const grupos = useMemo(() => obtenerMenuPorRol(rolActivo), [rolActivo]);

  return { grupos, rol: rolActivo };
}
