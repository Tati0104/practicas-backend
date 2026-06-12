import { useQuery } from '@tanstack/react-query';
import empresaService from '../../empresa/services/empresaService';

export function useVacanteDetalle(id) {
  const { data, isLoading, isError } = useQuery({
    queryKey: ['vacanteDetalle', id],
    queryFn: () => empresaService.getVacante(id).then(r => r.data),
    enabled: !!id,
  });
  return { vacante: data, isLoading, isError };
}
