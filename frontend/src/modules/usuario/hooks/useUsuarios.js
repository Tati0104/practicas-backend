import { useMutation, useQueryClient } from '@tanstack/react-query';
import usuarioService from '../services/usuarioService';
import { MOCK_USUARIOS } from '@/shared/mocks/datos';
import { useListadoPaginado } from '@/shared/hooks/useListadoPaginado';

export default function useUsuarios() {
  const queryClient = useQueryClient();

  const listado = useListadoPaginado({
    clave: 'usuarios',
    mockData: MOCK_USUARIOS,
    fetchApi: (filtros) => usuarioService.listar(filtros),
  });

  const crear = useMutation({
    mutationFn: (dto) => usuarioService.crear(dto),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['usuarios'] }),
  });

  const editar = useMutation({
    mutationFn: ({ id, dto }) => usuarioService.editar(id, dto),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['usuarios'] }),
  });

  const activar = useMutation({
    mutationFn: (id) => usuarioService.activar(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['usuarios'] }),
  });

  const inactivar = useMutation({
    mutationFn: (id) => usuarioService.inactivar(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['usuarios'] }),
  });

  const eliminar = useMutation({
    mutationFn: (id) => usuarioService.eliminar(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['usuarios'] });
      queryClient.invalidateQueries({ queryKey: ['docentes-asesores'] });
      queryClient.invalidateQueries({ queryKey: ['estudiantes'] });
    },
  });

  return {
    usuarios: listado.items,
    totalElementos: listado.totalElementos,
    totalPaginas: listado.totalPaginas,
    isLoading: listado.isLoading,
    isFetching: listado.isFetching,
    isError: listado.isError,
    filtros: listado.filtros,
    setFiltros: listado.setFiltros,
    actualizarFiltros: listado.actualizarFiltros,
    irAPagina: listado.irAPagina,
    crear,
    editar,
    activar,
    inactivar,
    eliminar,
  };
}
