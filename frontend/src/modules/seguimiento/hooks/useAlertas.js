// src/modules/seguimiento/hooks/useAlertas.js

/**
 * Hook para obtener y gestionar alertas de una práctica.
 * Permite filtrar por estado (leída/no leída) e invalidar al marcar como leída.
 */
import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { toast } from 'react-hot-toast';
import seguimientoService from '../services/seguimientoService';

const MOCK_ALERTAS = [
  { id: 1, practicaId: 3, mensaje: 'Sin actividad registrada en los últimos 15 días', leida: false, fecha: '2024-03-01' },
  { id: 2, practicaId: 3, mensaje: 'Bitácora pendiente de la semana 4', leida: false, fecha: '2024-03-05' },
  { id: 3, practicaId: 2, mensaje: 'Porcentaje de avance inferior al esperado', leida: true, fecha: '2024-02-28' },
];

export function useAlertas(practicaId = null) {
  const queryClient = useQueryClient();
  const [soloNoLeidas, setSoloNoLeidas] = useState(false);

  const params = {};
  if (practicaId) params.practicaId = practicaId;
  if (soloNoLeidas) params.leida = false;

  const { data, isLoading, isError } = useQuery({
    queryKey: ['alertas', practicaId, soloNoLeidas],
    queryFn: async () => {
      const resp = await seguimientoService.alertas(params);
      return resp.data?.data || resp.data || [];
    },
    placeholderData: practicaId
      ? MOCK_ALERTAS.filter((a) => a.practicaId === practicaId)
      : MOCK_ALERTAS,
  });

  const marcarLeida = useMutation({
    mutationFn: (id) => seguimientoService.marcarAlertaLeida(id),
    onSuccess: () => {
      queryClient.invalidateQueries(['alertas']);
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
