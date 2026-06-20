import { useQueryClient } from '@tanstack/react-query';
import empresaService from '../../empresa/services/empresaService';
import { MOCK_VACANTES } from '@/shared/mocks/datos';
import { useListadoPaginado } from '@/shared/hooks/useListadoPaginado';

export function useVacantes() {
  const queryClient = useQueryClient();

  const listado = useListadoPaginado({
    clave: 'vacantes',
    mockData: MOCK_VACANTES,
    fetchApi: (filtros) => empresaService.listarVacantes(filtros),
    filtrosIniciales: {
      empresaId: '',
      programaId: '',
      estado: '',
      modalidad: '',
      area: '',
    },
  });

  return {
    vacantes: listado.items,
    totalElementos: listado.totalElementos,
    totalPaginas: listado.totalPaginas,
    isLoading: listado.isLoading,
    isFetching: listado.isFetching,
    isError: listado.isError,
    error: listado.error,
    filtros: listado.filtros,
    setFiltros: listado.setFiltros,
    actualizarFiltros: listado.actualizarFiltros,
    irAPagina: listado.irAPagina,
    refetch: () => queryClient.invalidateQueries({ queryKey: ['vacantes'] }),
  };
}
