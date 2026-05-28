import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import dashboardService from '../services/dashboardService';

export function useResumen() {
  return useQuery({
    queryKey: ['dashboard-resumen'],
    queryFn:  () => dashboardService.obtenerResumen().then(r => r.data.data),
    refetchInterval: 30000 // actualiza cada 30 segundos
  });
}

export function useAlertas() {
  const queryClient = useQueryClient();

  const alertas = useQuery({
    queryKey: ['dashboard-alertas'],
    queryFn:  () => dashboardService.obtenerAlertas().then(r => r.data.data)
  });

  const marcarLeida = useMutation({
    mutationFn: (id) => dashboardService.marcarAlertaLeida(id),
    onSuccess:  () => queryClient.invalidateQueries(['dashboard-alertas'])
  });

  return { alertas, marcarLeida };
}

export function useFiltrosGlobales() {
  return useQuery({
    queryKey: ['filtros-disponibles'],
    queryFn:  () => dashboardService.obtenerFiltrosDisponibles().then(r => r.data.data)
  });
}