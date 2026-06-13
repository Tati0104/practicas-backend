import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import useAuthStore from '@/store/authStore';
import seguimientoService from '../services/seguimientoService';
import { MOCK_PRACTICAS } from '@/shared/mocks/datos';
import {
  ejecutarConsulta,
  paginarEnCliente,
  placeholderDesdeMock,
  usarMocks,
} from '@/shared/config/dataSource';

export function useSeguimiento() {
  const programaIdSesion = useAuthStore((state) => state.programaId);

  const [filtros, setFiltros] = useState({
    page: 0,
    size: 10,
    programaId: programaIdSesion || (usarMocks() ? '1' : ''),
    docenteId: '',
    estado: '',
    busqueda: '',
  });

  const { data, isLoading, isError, isFetching } = useQuery({
    queryKey: ['seguimiento', filtros, usarMocks()],
    queryFn: () =>
      ejecutarConsulta({
        mock: () => paginarEnCliente(MOCK_PRACTICAS, filtros),
        api: async () => {
          const resp = await seguimientoService.tablero(filtros);
          const lista = resp.data?.data ?? resp.data ?? [];
          const items = Array.isArray(lista) ? lista : (lista.content ?? []);
          return paginarEnCliente(items, filtros);
        },
      }),
    placeholderData: placeholderDesdeMock(MOCK_PRACTICAS),
    enabled: usarMocks() || Boolean(filtros.programaId),
  });

  const actualizarFiltros = (cambios) =>
    setFiltros((prev) => ({ ...prev, ...cambios, page: 0 }));

  return {
    practicas: data?.content ?? [],
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
