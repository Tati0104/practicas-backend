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
        mock: () => ({ documentos: MOCK_DOCUMENTOS, estudiante: { id: 1, documentos: [] } }),
        api: async () => {
          const respDocs = await vinculacionService.obtenerDocumentos(practicaId);
          const docs = respDocs.data?.data ?? respDocs.data ?? [];
          
          let estudiante = null;
          try {
            const respEst = await vinculacionService.obtenerEstudiante(practicaId);
            estudiante = respEst.data?.data ?? respEst.data ?? null;
          } catch (e) {
            console.error("No se pudo obtener el estudiante", e);
          }
          
          return { documentos: docs, estudiante };
        },
      }),
    enabled: Boolean(practicaId),
    placeholderData: placeholderSimple({ documentos: MOCK_DOCUMENTOS, estudiante: { id: 1, documentos: [] } }),
  });

  return {
    documentos: data?.documentos ?? [],
    estudiante: data?.estudiante ?? null,
    isLoading,
    isError,
    refetch,
  };
}
