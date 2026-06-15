import { useMutation, useQueryClient } from '@tanstack/react-query';
import toast from 'react-hot-toast';
import estudianteService from '../services/estudianteService';
import { MOCK_ESTUDIANTES } from '@/shared/mocks/datos';
import { useListadoPaginado } from '@/shared/hooks/useListadoPaginado';
import { extraerMensajeError } from '@/modules/auth/utils/schemas';

export default function useEstudiantes() {
  const queryClient = useQueryClient();

  const listado = useListadoPaginado({
    clave: 'estudiantes',
    mockData: MOCK_ESTUDIANTES,
    fetchApi: (filtros) => estudianteService.listar(filtros),
  });

  const alError = (err) => {
    toast.error(extraerMensajeError(err));
  };

  const registrar = useMutation({
    mutationFn: (dto) => estudianteService.registrar(dto),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['estudiantes'] });
      toast.success('Estudiante registrado correctamente');
    },
    onError: alError,
  });

  const editar = useMutation({
    mutationFn: ({ id, dto }) => estudianteService.editar(id, dto),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['estudiantes'] });
      toast.success('Estudiante actualizado correctamente');
    },
    onError: alError,
  });

  const marcarApto = useMutation({
    mutationFn: (id) => estudianteService.marcarApto(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['estudiantes'] });
      toast.success('Estudiante marcado como apto');
    },
    onError: alError,
  });

  const marcarNoApto = useMutation({
    mutationFn: ({ id, motivo }) => estudianteService.marcarNoApto(id, motivo),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['estudiantes'] });
      toast.success('Estudiante marcado como no apto');
    },
    onError: alError,
  });

  const importarExcel = useMutation({
    mutationFn: (archivo) => estudianteService.importarExcel(archivo),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['estudiantes'] }),
  });

  return {
    estudiantes: listado.items,
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
    marcarApto,
    marcarNoApto,
    importarExcel,
  };
}
