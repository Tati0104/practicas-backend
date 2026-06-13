import { useMutation, useQueryClient } from '@tanstack/react-query';
import empresaService from '../services/empresaService';
import { MOCK_EMPRESAS } from '@/shared/mocks/datos';
import { useListadoPaginado } from '@/shared/hooks/useListadoPaginado';

export function useEmpresas() {
  const queryClient = useQueryClient();

  const listado = useListadoPaginado({
    clave: 'empresas',
    mockData: MOCK_EMPRESAS,
    fetchApi: (filtros) => empresaService.listar(filtros),
  });

  const registrar = useMutation({
    mutationFn: (dto) => empresaService.registrar(dto),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['empresas'] }),
  });

  const activar = useMutation({
    mutationFn: (id) => empresaService.activar(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['empresas'] }),
  });

  const inactivar = useMutation({
    mutationFn: (id) => empresaService.inactivar(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['empresas'] }),
  });

  return {
    empresas: listado.items,
    totalElementos: listado.totalElementos,
    totalPaginas: listado.totalPaginas,
    isLoading: listado.isLoading,
    isFetching: listado.isFetching,
    isError: listado.isError,
    filtros: listado.filtros,
    setFiltros: listado.setFiltros,
    actualizarFiltros: listado.actualizarFiltros,
    irAPagina: listado.irAPagina,
    registrar,
    activar,
    inactivar,
  };
}
