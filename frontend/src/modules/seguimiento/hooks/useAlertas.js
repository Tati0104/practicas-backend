import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { toast } from 'react-hot-toast';
import seguimientoService from '../services/seguimientoService';
import { MOCK_ALERTAS } from '@/shared/mocks/datos';
import {
  ejecutarConsulta,
  placeholderSimple,
  usarMocks,
} from '@/shared/config/dataSource';

export function useAlertas(practicaId = null) {
  const queryClient = useQueryClient();
  const [soloNoLeidas, setSoloNoLeidas] = useState(false);

  const { data, isLoading, isError } = useQuery({
    queryKey: ['alertas', practicaId, soloNoLeidas, usarMocks()],
    queryFn: () =>
      ejecutarConsulta({
        mock: () =>
          practicaId
            ? MOCK_ALERTAS.filter((a) => a.practicaId === practicaId)
            : MOCK_ALERTAS,
        api: async () => {
          const resp = await seguimientoService.alertas();
          return resp.data?.data ?? resp.data ?? [];
        },
      }),
    placeholderData: placeholderSimple(
      practicaId
        ? MOCK_ALERTAS.filter((a) => a.practicaId === practicaId)
        : MOCK_ALERTAS
    ),
  });

  const marcarLeida = useMutation({
    mutationFn: (id) => seguimientoService.marcarAlertaLeida(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['alertas'] });
      toast.success('Alerta marcada como leída');
    },
    onError: () => toast.error('Error al marcar alerta'),
  });

  const alertas = Array.isArray(data) ? data : [];
  const noLeidas = alertas.filter((a) => !a.leida).length;

  return {
    alertas: soloNoLeidas ? alertas.filter((a) => !a.leida) : alertas,
    noLeidas,
    isLoading,
    isError,
    soloNoLeidas,
    setSoloNoLeidas,
    marcarLeida: (id) => marcarLeida.mutate(id),
    isPendingMarcar: marcarLeida.isPending,
  };
}
