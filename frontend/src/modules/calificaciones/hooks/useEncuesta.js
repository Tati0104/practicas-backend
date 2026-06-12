import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import toast from 'react-hot-toast';
import http from '@/shared/services/http';
import { resolverPreguntas } from '../utils/preguntasEncuesta';
import { extraerMensajeError } from '../utils/schemas';

function parseRespuestas(respuestasJson) {
  if (!respuestasJson || respuestasJson === '{}') return {};
  try {
    return JSON.parse(respuestasJson);
  } catch {
    return {};
  }
}

export default function useEncuesta(practicaId, tipo, habilitada = true) {
  const queryClient = useQueryClient();
  const queryKey = ['encuesta', practicaId, tipo];

  const query = useQuery({
    queryKey,
    queryFn: async () => {
      const { data } = await http.get(`/encuestas/${practicaId}/${tipo}`);
      return {
        ...data,
        respuestas: parseRespuestas(data.respuestasJson),
        preguntas: resolverPreguntas(tipo, data.preguntas),
      };
    },
    enabled: Boolean(practicaId && tipo && habilitada),
  });

  const invalidar = () => {
    queryClient.invalidateQueries({ queryKey });
  };

  const guardarBorrador = useMutation({
    mutationFn: ({ encuestaId, respuestas }) =>
      http.post(`/encuestas/${encuestaId}/borrador`, {
        respuestasJson: JSON.stringify(respuestas),
      }),
    onSuccess: () => {
      toast.success('Borrador guardado');
      invalidar();
    },
    onError: (error) => {
      toast.error(extraerMensajeError(error, 'No se pudo guardar el borrador'));
    },
  });

  const enviar = useMutation({
    mutationFn: ({ encuestaId, respuestas }) =>
      http
        .post(`/encuestas/${encuestaId}/borrador`, {
          respuestasJson: JSON.stringify(respuestas),
        })
        .then(() => http.post(`/encuestas/${encuestaId}/enviar`)),
    onSuccess: () => {
      toast.success('Encuesta enviada correctamente');
      invalidar();
    },
    onError: (error) => {
      toast.error(extraerMensajeError(error, 'No se pudo enviar la encuesta'));
    },
  });

  const enviarRecordatorio = useMutation({
    mutationFn: () => http.post(`/encuestas/${practicaId}/${tipo}/recordatorio`),
    onSuccess: () => {
      toast.success('Recordatorio enviado');
      invalidar();
    },
    onError: (error) => {
      toast.error(extraerMensajeError(error, 'No se pudo enviar el recordatorio'));
    },
  });

  return {
    ...query,
    guardarBorrador,
    enviar,
    enviarRecordatorio,
  };
}
