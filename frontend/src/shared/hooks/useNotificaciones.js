import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { toast } from 'react-hot-toast';
import useAuthStore from '@/store/authStore';
import notificacionService from '@/shared/services/notificacionService';
import { MOCK_ALERTAS } from '@/shared/mocks/datos';
import {
  ejecutarConsulta,
  placeholderSimple,
  usarMocks,
} from '@/shared/config/dataSource';

function normalizarAlerta(alerta) {
  if (!alerta) return alerta;
  const fechaRaw = alerta.fecha;
  const fecha =
    typeof fechaRaw === 'string'
      ? new Date(fechaRaw).toLocaleDateString('es-CO')
      : fechaRaw ?? '';

  return {
    ...alerta,
    practicaId: alerta.practicaId ?? alerta.instanciaPracticaId ?? null,
    fecha,
  };
}

export function useNotificaciones(practicaId = null) {
  const queryClient = useQueryClient();
  const [soloNoLeidas, setSoloNoLeidas] = useState(false);
  const correo = useAuthStore((state) => state.correo ?? state.usuario?.correo);
  const rol = useAuthStore((state) => state.rol);

  const { data, isLoading, isError } = useQuery({
    queryKey: ['notificaciones', correo, rol, practicaId, usarMocks()],
    queryFn: () =>
      ejecutarConsulta({
        mock: () => {
          const base = practicaId
            ? MOCK_ALERTAS.filter((a) => a.practicaId === practicaId)
            : MOCK_ALERTAS;
          return base.map(normalizarAlerta);
        },
        api: async () => {
          const params = practicaId ? { practicaId } : {};
          const resp = await notificacionService.listar(params);
          const lista = resp.data?.data ?? resp.data ?? [];
          return (Array.isArray(lista) ? lista : []).map(normalizarAlerta);
        },
      }),
    enabled: Boolean(correo) || usarMocks(),
    placeholderData: placeholderSimple(
      (practicaId
        ? MOCK_ALERTAS.filter((a) => a.practicaId === practicaId)
        : MOCK_ALERTAS
      ).map(normalizarAlerta)
    ),
    refetchInterval: 60_000,
  });

  const marcarLeidaMut = useMutation({
    mutationFn: (id) => notificacionService.marcarLeida(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['notificaciones'] });
      queryClient.invalidateQueries({ queryKey: ['alertas'] });
    },
    onError: () => toast.error('No se pudo marcar la notificación'),
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
    marcarLeida: (id) => marcarLeidaMut.mutate(id),
    isPendingMarcar: marcarLeidaMut.isPending,
  };
}
