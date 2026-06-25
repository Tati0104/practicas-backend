import { useQuery } from '@tanstack/react-query';
import reporteService from '../services/reporteService';

export function useReporteResumen() {
  return useQuery({
    queryKey: ['reportes-resumen'],
    queryFn: () => reporteService.obtenerResumen().then((r) => r.data?.data ?? r.data),
  });
}
