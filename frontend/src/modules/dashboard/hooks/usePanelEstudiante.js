import { useQuery } from '@tanstack/react-query';
import useAuthStore from '@/store/authStore';
import { cargarPanelEstudiante } from '../utils/panelEstudianteApi';

export function usePanelEstudiante() {
  const token = useAuthStore((state) => state.token);
  const rol = useAuthStore((state) => state.rol);

  return useQuery({
    queryKey: ['panel-estudiante'],
    queryFn: cargarPanelEstudiante,
    enabled: Boolean(token) && rol === 'ESTUDIANTE',
  });
}
