import { useQuery } from '@tanstack/react-query';
import { MOCK_DETALLE_PRACTICA } from '@/shared/mocks/datos';
import {
  ejecutarConsulta,
  placeholderSimple,
  usarMocks,
} from '@/shared/config/dataSource';
import seguimientoService from '../services/seguimientoService';
import { parseFechaSeguimiento } from '../utils/fechas';

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

  const timeline = [...(data?.timeline ?? [])].sort((a, b) => {
    const fechaA = parseFechaSeguimiento(a.fecha)?.getTime() ?? 0;
    const fechaB = parseFechaSeguimiento(b.fecha)?.getTime() ?? 0;
    return fechaB - fechaA;
  });

  return { practica: data, timeline, isLoading, isError };
}
