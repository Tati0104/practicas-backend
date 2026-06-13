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
          const resp = await asignacionService.listar({
            ...filtros,
            estado: filtros.estado || 'EN_VINCULACION',
          });
          return normalizarPagina(resp.data?.data ?? resp.data, []);
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
