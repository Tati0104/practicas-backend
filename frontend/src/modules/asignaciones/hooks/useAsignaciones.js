import asignacionService from '../services/asignacionService';
import { MOCK_ASIGNACIONES } from '@/shared/mocks/datos';
import { useListadoPaginado } from '@/shared/hooks/useListadoPaginado';

export function useAsignaciones() {
  const listado = useListadoPaginado({
    clave: 'asignaciones',
    mockData: MOCK_ASIGNACIONES,
    fetchApi: (filtros) => asignacionService.listar(filtros),
    filtrosIniciales: {
      programaId: '',
      estado: '',
      busqueda: '',
    },
  });

  return {
    asignaciones: listado.items,
    totalElementos: listado.totalElementos,
    totalPaginas: listado.totalPaginas,
    isLoading: listado.isLoading,
    isFetching: listado.isFetching,
    isError: listado.isError,
    filtros: listado.filtros,
    setFiltros: listado.setFiltros,
    actualizarFiltros: listado.actualizarFiltros,
    irAPagina: listado.irAPagina,
  };
}
