import { useQuery } from '@tanstack/react-query';
import { MOCK_DOCUMENTOS_DETALLE } from '@/shared/mocks/datos';
import {
  ejecutarConsulta,
  placeholderSimple,
  usarMocks,
} from '@/shared/config/dataSource';
import { obtenerDocumentosAsignacion } from '../utils/vinculacionApi';

export function useVinculacionDocumentos(asignacionId) {
  const { data, isLoading, isError, refetch } = useQuery({
    queryKey: ['vinculacion-documentos', asignacionId, usarMocks()],
    queryFn: () =>
      ejecutarConsulta({
        mock: () => MOCK_DOCUMENTOS_DETALLE,
        api: () => obtenerDocumentosAsignacion(asignacionId),
      }),
    enabled: Boolean(asignacionId),
    placeholderData: placeholderSimple(MOCK_DOCUMENTOS_DETALLE),
  });

  return {
    documentos: data?.documentos ?? [],
    convenioId: data?.convenioId ?? null,
    practicaId: data?.practicaId ?? null,
    detalle: data?.detalle ?? null,
    isLoading,
    isError,
    refetch,
  };
}
