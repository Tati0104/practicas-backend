import { useMutation, useQueryClient } from '@tanstack/react-query';
import toast from 'react-hot-toast';
import http from '@/shared/services/http';
import { CORTE_REFERENCIA, extraerMensajeError } from '../utils/schemas';

export default function useCalificacionesMutaciones(practicaId) {
  const queryClient = useQueryClient();

  const invalidar = () => {
    queryClient.invalidateQueries({ queryKey: ['calificaciones', practicaId] });
  };

  const registrarNotaDocente = useMutation({
    mutationFn: ({ nota, observaciones, corte = CORTE_REFERENCIA }) =>
      http.post(`/calificaciones/${practicaId}/docente`, { nota, observaciones }, { params: { corte } }),
    onSuccess: () => {
      toast.success('Nota del docente registrada');
      invalidar();
    },
    onError: (error) => {
      toast.error(extraerMensajeError(error, 'No se pudo registrar la nota del docente'));
    },
  });

  const registrarNotaTutor = useMutation({
    mutationFn: ({ nota, observaciones, corte = CORTE_REFERENCIA }) =>
      http.post(`/calificaciones/${practicaId}/tutor`, { nota, observaciones }, { params: { corte } }),
    onSuccess: () => {
      toast.success('Nota del tutor registrada');
      invalidar();
    },
    onError: (error) => {
      toast.error(extraerMensajeError(error, 'No se pudo registrar la nota del tutor'));
    },
  });

  const registrarNotaFinal = useMutation({
    mutationFn: ({ nota }) =>
      http.post(`/calificaciones/${practicaId}/final`, { notaFinal: nota }),
    onSuccess: () => {
      toast.success('Nota final registrada');
      invalidar();
    },
    onError: (error) => {
      toast.error(extraerMensajeError(error, 'No se pudo registrar la nota final'));
    },
  });

  return {
    registrarNotaDocente,
    registrarNotaTutor,
    registrarNotaFinal,
  };
}
