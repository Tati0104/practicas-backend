import { keepPreviousData, useQuery } from '@tanstack/react-query';
import { MOCK_DOCUMENTOS_DETALLE } from '@/shared/mocks/datos';
import {
  ejecutarConsulta,
  usarMocks,
} from '@/shared/config/dataSource';
import { claveQueryDocumentos, obtenerDocumentosAsignacion } from '../utils/vinculacionApi';

export function useVinculacionDocumentos(asignacionId) {
  const useMocks = usarMocks();

  const { data, isLoading, isError, refetch } = useQuery({
    queryKey: claveQueryDocumentos(asignacionId, useMocks),
    queryFn: () =>
      ejecutarConsulta({
        mock: () => MOCK_DOCUMENTOS_DETALLE,
        api: () => obtenerDocumentosAsignacion(asignacionId),
      }),
    enabled: Boolean(asignacionId),
    staleTime: 60_000,
    gcTime: 10 * 60_000,
    refetchOnWindowFocus: false,
    placeholderData: useMocks ? MOCK_DOCUMENTOS_DETALLE : keepPreviousData,
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
