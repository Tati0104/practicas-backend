import { useQuery } from '@tanstack/react-query';
import seguimientoService from '../services/seguimientoService';

export function useBitacorasEstudiante(practicaId, { enabled = true } = {}) {
  const { data, isLoading, isError } = useQuery({
    queryKey: ['bitacorasEstudiante', practicaId],
    queryFn: async () => {
      const resp = await seguimientoService.obtenerBitacoras(practicaId);
      return resp.data?.data ?? resp.data ?? [];
    },
    enabled: Boolean(practicaId) && enabled,
  });

  return { bitacoras: Array.isArray(data) ? data : [], isLoading, isError };
}
