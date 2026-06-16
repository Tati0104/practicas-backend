import { useNotificaciones } from '@/shared/hooks/useNotificaciones';

/** @deprecated Usar useNotificaciones directamente */
export function useAlertas(practicaId = null) {
  return useNotificaciones(practicaId);
}
