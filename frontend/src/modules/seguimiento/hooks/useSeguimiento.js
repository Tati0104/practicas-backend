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

/**
 * Carga las prácticas visibles para el usuario autenticado sin exigir ningún filtro:
 * el backend (GET /seguimiento/practicas) resuelve el scope (ESTUDIANTE ve la suya,
 * coordinadores su facultad, ADMIN/SECRETARIA todas). El "ID Programa" y el resto de
 * filtros son opcionales y solo acotan la lista ya cargada.
 */
export function useSeguimiento() {
  const token = useAuthStore((state) => state.token);

  const [filtros, setFiltros] = useState({
    page: 0,
    size: 10,
    programaId: usarMocks() ? '1' : '',
    docenteId: '',
    estado: '',
    busqueda: '',
    numeroPractica: '',
    estadoPractica: '',
  });

  const { data, isLoading, isError, isFetching } = useQuery({
    queryKey: ['seguimiento-practicas', filtros, usarMocks()],
    queryFn: () =>
      ejecutarConsulta({
        mock: () => {
          let items = MOCK_PRACTICAS;
          if (filtros.numeroPractica) {
            items = items.filter(
              (p) => String(p.numeroPractica) === String(filtros.numeroPractica)
            );
          }
          return paginarEnCliente(items, filtros);
        },
        api: async () => {
          const resp = await seguimientoService.practicas(filtros);
          const lista = resp.data?.data ?? resp.data ?? [];
          const allItems = Array.isArray(lista) ? lista : (lista.content ?? []);
          let items = allItems;
          if (filtros.numeroPractica) {
            items = items.filter(
              (p) => String(p.numeroPractica) === String(filtros.numeroPractica)
            );
          }
          if (filtros.estadoPractica) {
            items = items.filter((p) => p.estadoPractica === filtros.estadoPractica);
          }
          const paginado = paginarEnCliente(items, filtros);
          return { ...paginado, allItems };
        },
      }),
    placeholderData: placeholderDesdeMock(MOCK_PRACTICAS),
    enabled: usarMocks() || Boolean(token),
  });

  const actualizarFiltros = (cambios) =>
    setFiltros((prev) => ({ ...prev, ...cambios, page: 0 }));

  return {
    practicas: data?.content ?? [],
    practicasTodas: data?.allItems ?? data?.content ?? [],
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
