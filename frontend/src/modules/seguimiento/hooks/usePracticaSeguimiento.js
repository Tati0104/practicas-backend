import { useQuery } from '@tanstack/react-query';
import { MOCK_DETALLE_PRACTICA } from '@/shared/mocks/datos';
import {
  ejecutarConsulta,
  placeholderSimple,
  usarMocks,
} from '@/shared/config/dataSource';

export function usePracticaSeguimiento(practicaId) {
  const { data, isLoading, isError } = useQuery({
    queryKey: ['practicaSeguimiento', practicaId, usarMocks()],
    queryFn: () =>
      ejecutarConsulta({
        mock: () => ({ ...MOCK_DETALLE_PRACTICA, id: Number(practicaId) || MOCK_DETALLE_PRACTICA.id }),
        api: async () => {
          // Detalle unificado pendiente en backend; evita mezclar mock en modo API.
          return null;
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
