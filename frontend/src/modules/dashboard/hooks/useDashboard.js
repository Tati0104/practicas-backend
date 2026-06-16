import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import useAuthStore from '@/store/authStore';
import dashboardService from '../services/dashboardService';

function extraerDatos(resp) {
  return resp.data?.data ?? resp.data;
}

export function useResumen() {
  const token = useAuthStore((state) => state.token);

  return useQuery({
    queryKey: ['dashboard-resumen'],
    queryFn: () => dashboardService.obtenerResumen().then(extraerDatos),
    enabled: Boolean(token),
    refetchInterval: 30000,
  });
}

export function useAlertas() {
  const queryClient = useQueryClient();
  const token = useAuthStore((state) => state.token);

  const alertas = useQuery({
    queryKey: ['dashboard-alertas'],
    queryFn: () => dashboardService.obtenerAlertas().then(extraerDatos),
    enabled: Boolean(token),
  });

  const marcarLeida = useMutation({
    mutationFn: (id) => dashboardService.marcarAlertaLeida(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['dashboard-alertas'] }),
  });

  return { alertas, marcarLeida };
}

export function useFiltrosGlobales() {
  const token = useAuthStore((state) => state.token);

  return useQuery({
    queryKey: ['filtros-disponibles'],
    queryFn: () => dashboardService.obtenerFiltrosDisponibles().then(extraerDatos),
    enabled: Boolean(token),
  });
}
