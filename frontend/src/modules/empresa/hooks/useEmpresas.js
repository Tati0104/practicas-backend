import { useMutation, useQueryClient } from '@tanstack/react-query';
import toast from 'react-hot-toast';
import empresaService from '../services/empresaService';
import { MOCK_EMPRESAS } from '@/shared/mocks/datos';
import { useListadoPaginado } from '@/shared/hooks/useListadoPaginado';
import { extraerMensajeError } from '@/modules/auth/utils/schemas';

export function useEmpresas() {
  const queryClient = useQueryClient();

  const alError = (err) => {
    toast.error(extraerMensajeError(err));
  };

  const listado = useListadoPaginado({
    clave: 'empresas',
    mockData: MOCK_EMPRESAS,
    fetchApi: (filtros) => empresaService.listar(filtros),
  });

  const registrar = useMutation({
    mutationFn: (dto) => empresaService.registrar(dto),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['empresas'] });
      toast.success('Empresa registrada correctamente');
    },
    onError: alError,
  });

  const editar = useMutation({
    mutationFn: ({ id, dto }) => empresaService.editar(id, dto),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['empresas'] });
      toast.success('Empresa actualizada correctamente');
    },
    onError: alError,
  });

  const activar = useMutation({
    mutationFn: (id) => empresaService.activar(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['empresas'] });
      toast.success('Empresa reactivada correctamente');
    },
    onError: alError,
  });

  const inactivar = useMutation({
    mutationFn: ({ id, motivo }) => empresaService.inactivar(id, motivo),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['empresas'] });
      queryClient.invalidateQueries({ queryKey: ['empresa-tutores'] });
      toast.success('Empresa inactivada correctamente');
    },
    onError: alError,
  });

  const registrarTutor = useMutation({
    mutationFn: (dto) => empresaService.registrarTutor(dto),
    onSuccess: (_data, variables) => {
      const empresaId = variables?.empresa?.id;
      queryClient.invalidateQueries({ queryKey: ['empresa-tutores', empresaId] });
      queryClient.invalidateQueries({ queryKey: ['empresa-tutores'] });
      toast.success('Tutor empresarial registrado correctamente');
    },
    onError: alError,
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
    editar,
    activar,
    inactivar,
    registrarTutor,
  };
}
