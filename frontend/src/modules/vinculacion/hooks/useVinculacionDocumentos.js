import { keepPreviousData, useQuery } from '@tanstack/react-query';
import { MOCK_DOCUMENTOS_DETALLE } from '@/shared/mocks/datos';
import {
  ejecutarConsulta,
  usarMocks,
} from '@/shared/config/dataSource';
import {
  claveQueryDocumentos,
  obtenerDocumentosAsignacion,
  obtenerDocumentosPracticaDetalle,
} from '../utils/vinculacionApi';

export function useVinculacionDocumentos({ asignacionId, practicaId } = {}) {
  const useMocks = usarMocks();
  const idAsignacion = asignacionId || null;
  const idPractica = practicaId || null;

  const { data, isLoading, isError, refetch } = useQuery({
    queryKey: claveQueryDocumentos(idAsignacion, idPractica, useMocks),
    queryFn: () =>
      ejecutarConsulta({
        mock: () => MOCK_DOCUMENTOS_DETALLE,
        api: () =>
          idAsignacion
            ? obtenerDocumentosAsignacion(idAsignacion)
            : obtenerDocumentosPracticaDetalle(idPractica),
      }),
    enabled: Boolean(idAsignacion || idPractica),
    staleTime: 0,
    gcTime: 10 * 60_000,
    refetchOnMount: 'always',
    refetchOnWindowFocus: true,
    placeholderData: useMocks ? MOCK_DOCUMENTOS_DETALLE : keepPreviousData,
  });

  return {
    documentos: data?.documentos ?? [],
    convenioId: data?.convenioId ?? null,
    practicaId: data?.practicaId ?? idPractica,
    detalle: data?.detalle ?? null,
    isLoading,
    isError,
    refetch,
  };
}
