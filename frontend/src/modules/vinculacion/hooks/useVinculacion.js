import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { MOCK_VINCULACIONES } from '@/shared/mocks/datos';
import {
  ejecutarConsulta,
  paginarEnCliente,
  placeholderDesdeMock,
  usarMocks,
} from '@/shared/config/dataSource';
import { listarVinculaciones } from '../utils/vinculacionApi';

export function useVinculacion() {
  const [filtros, setFiltros] = useState({
    page: 0,
    size: 10,
    programaId: '',
    empresaId: '',
    estado: '',
    busqueda: '',
  });

  const { data, isLoading, isFetching, isError } = useQuery({
    queryKey: ['vinculacion', filtros, usarMocks()],
    queryFn: () =>
      ejecutarConsulta({
        mock: () => paginarEnCliente(MOCK_VINCULACIONES, filtros),
        api: () => listarVinculaciones(filtros),
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
