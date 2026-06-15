import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import useAuthStore from '@/store/authStore';
import asignacionService from '../../asignaciones/services/asignacionService';
import { MOCK_VINCULACIONES } from '@/shared/mocks/datos';
import {
  ejecutarConsulta,
  paginarEnCliente,
  placeholderDesdeMock,
  usarMocks,
} from '@/shared/config/dataSource';
import { normalizarPagina } from '@/shared/utils/paginacion';

export function useVinculacion() {
  const [filtros, setFiltros] = useState({
    page: 0,
    size: 10,
    programaId: '',
    estado: '',
    busqueda: '',
  });

  const { data, isLoading, isFetching, isError } = useQuery({
    queryKey: ['vinculacion', filtros, usarMocks()],
    queryFn: () =>
      ejecutarConsulta({
        mock: () => paginarEnCliente(MOCK_VINCULACIONES, filtros),
        api: async () => {
          const params = { ...filtros, estado: filtros.estado || 'EN_PROCESO_VINCULACION' };
          console.log('[useVinculacion] params enviados:', params);
          try {
            const resp = await asignacionService.listar(params);
            console.log('[useVinculacion] respuesta OK:', resp.data);
            return normalizarPagina(resp.data?.data ?? resp.data, []);
          } catch (err) {
            console.error('[useVinculacion] status:', err?.response?.status);
            console.error('[useVinculacion] body:', err?.response?.data);
            console.error('[useVinculacion] URL:', err?.config?.url, '| params:', err?.config?.params);
            throw err;
          }
        },
      }),
    placeholderData: placeholderDesdeMock(MOCK_VINCULACIONES),
  });

  const actualizarFiltros = (cambios) =>
    setFiltros((prev) => ({ ...prev, ...cambios, page: 0 }));

  return {
    vinculaciones: data?.content ?? [],
    totalElementos: data?.totalElements ?? 0,
    totalPaginas: data?.totalPages ?? 1,
    isLoading,
    isFetching,
    isError,
    filtros,
    setFiltros,
    actualizarFiltros,
    irAPagina: (pagina) => setFiltros((f) => ({ ...f, page: pagina })),
  };
}
