import { useQuery } from '@tanstack/react-query';
import { MOCK_DETALLE_PRACTICA } from '@/shared/mocks/datos';
import {
  ejecutarConsulta,
  placeholderSimple,
  usarMocks,
} from '@/shared/config/dataSource';
import seguimientoService from '../services/seguimientoService';

export function usePracticaSeguimiento(practicaId) {
  const { data, isLoading, isError } = useQuery({
    queryKey: ['practicaSeguimiento', practicaId, usarMocks()],
    queryFn: () =>
      ejecutarConsulta({
        mock: () => ({ ...MOCK_DETALLE_PRACTICA, id: Number(practicaId) || MOCK_DETALLE_PRACTICA.id }),
        api: async () => {
          const resp = await seguimientoService.obtenerDetallePractica(practicaId);
          return resp.data?.data ?? resp.data;
        },
      }),
    enabled: Boolean(practicaId),
    placeholderData: placeholderSimple(MOCK_DETALLE_PRACTICA),
  });

  const timeline = [...(data?.timeline ?? [])].sort(
    (a, b) => new Date(b.fecha) - new Date(a.fecha)
  );

  return { practica: data, timeline, isLoading, isError };
}