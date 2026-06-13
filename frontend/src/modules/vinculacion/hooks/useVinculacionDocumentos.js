import { useQuery } from '@tanstack/react-query';
import vinculacionService from '../services/vinculacionService';
import { MOCK_DOCUMENTOS } from '@/shared/mocks/datos';
import {
  ejecutarConsulta,
  placeholderSimple,
  usarMocks,
} from '@/shared/config/dataSource';

export function useVinculacionDocumentos(practicaId) {
  const { data, isLoading, isError, refetch } = useQuery({
    queryKey: ['vinculacion-documentos', practicaId, usarMocks()],
    queryFn: () =>
      ejecutarConsulta({
        mock: () => MOCK_DOCUMENTOS,
        api: async () => {
          const resp = await vinculacionService.obtenerDocumentos(practicaId);
          return resp.data?.data ?? resp.data ?? [];
        },
      }),
    enabled: Boolean(practicaId),
    placeholderData: placeholderSimple(MOCK_DOCUMENTOS),
  });

  return {
    documentos: data ?? [],
    isLoading,
    isError,
    refetch,
  };
}
