import { useMutation, useQueryClient } from '@tanstack/react-query';
import toast from 'react-hot-toast';
import http from '@/shared/services/http';
import { extraerMensajeError } from '@/modules/calificaciones/utils/schemas';

export default function useCierreMutaciones(practicaId) {
  const queryClient = useQueryClient();

  const invalidarRelacionados = () => {
    queryClient.invalidateQueries({ queryKey: ['cierre', practicaId] });
    queryClient.invalidateQueries({ queryKey: ['asignaciones'] });
    queryClient.invalidateQueries({ queryKey: ['practicaSeguimiento', practicaId] });
    queryClient.invalidateQueries({ queryKey: ['seguimiento'] });
    queryClient.invalidateQueries({ queryKey: ['calificaciones', practicaId] });
  };

  const ejecutarCierre = useMutation({
    mutationFn: () =>
      http.post(`/cierres/${practicaId}/ejecutar`, { confirmacion: true }),
    onSuccess: (response) => {
      const mensaje =
        response?.data?.message ??
        response?.data?.mensaje ??
        'Cierre de práctica ejecutado';
      toast.success(mensaje);
      invalidarRelacionados();
    },
    onError: (error) => {
      toast.error(extraerMensajeError(error, 'No se pudo ejecutar el cierre de la práctica'));
    },
  });

  const enviarRecordatorio = useMutation({
    mutationFn: (tipo) => http.post(`/cierres/${practicaId}/recordatorio/${tipo}`),
    onSuccess: (response) => {
      const mensaje =
        response?.data?.message ??
        response?.data?.mensaje ??
        'Recordatorio enviado';
      toast.success(mensaje);
      queryClient.invalidateQueries({ queryKey: ['cierre', practicaId] });
    },
    onError: (error) => {
      toast.error(extraerMensajeError(error, 'No se pudo enviar el recordatorio'));
    },
  });

  return { ejecutarCierre, enviarRecordatorio };
}
