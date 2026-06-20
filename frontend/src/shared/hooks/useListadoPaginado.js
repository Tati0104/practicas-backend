import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import {
  ejecutarConsulta,
  placeholderDesdeMock,
  paginarEnCliente,
  usarMocks,
} from '../config/dataSource';
import { normalizarPagina } from '../utils/paginacion';

/**
 * Hook reutilizable para listados paginados con switch mock/API.
 *
 * @param {object} options
 * @param {string} options.clave - prefijo de queryKey (ej. 'estudiantes')
 * @param {unknown[]} options.mockData - datos mock centralizados
 * @param {(filtros: object) => Promise<import('axios').AxiosResponse>} options.fetchApi
 * @param {object} [options.filtrosIniciales]
 * @param {boolean} [options.enabled=true]
 */
export function useListadoPaginado({
  clave,
  mockData,
  fetchApi,
  filtrosIniciales = {},
  enabled = true,
}) {
  const [filtros, setFiltros] = useState({
    page: 0,
    size: 10,
    ...filtrosIniciales,
  });

  const query = useQuery({
    queryKey: [clave, filtros, usarMocks()],
    queryFn: () =>
      ejecutarConsulta({
        mock: () => paginarEnCliente(mockData, filtros),
        api: async () => {
          const resp = await fetchApi(filtros);
          return normalizarPagina(resp.data?.data ?? resp.data, []);
        },
      }),
    placeholderData: placeholderDesdeMock(mockData),
    enabled,
  });

  const actualizarFiltros = (cambios) =>
    setFiltros((prev) => ({ ...prev, ...cambios, page: 0 }));

  return {
    items: query.data?.content ?? [],
    totalElementos: query.data?.totalElements ?? 0,
    totalPaginas: query.data?.totalPages ?? 1,
    isLoading: query.isLoading,
    isFetching: query.isFetching,
    isError: query.isError,
    error: query.error,
    filtros,
    setFiltros,
    actualizarFiltros,
    irAPagina: (pagina) => setFiltros((f) => ({ ...f, page: pagina })),
  };
}
