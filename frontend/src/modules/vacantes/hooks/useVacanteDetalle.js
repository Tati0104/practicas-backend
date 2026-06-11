// src/modules/vacantes/hooks/useVacanteDetalle.js

/**
 * Hook que obtiene los datos completos de una vacante dado su ID.
 * Usa la función `getVacante` del servicio `empresaService`.
 * Devuelve el objeto vacante, estados de carga y error.
 */
import { useQuery } from '@tanstack/react-query';
import empresaService from '../../services/empresaService';

export function useVacanteDetalle(id) {
  const { data, isLoading, isError } = useQuery({
    queryKey: ['vacanteDetalle', id],
    queryFn: () => empresaService.getVacante(id).then(r => r.data),
    enabled: !!id, // solo se ejecuta si hay id
  });

  return { vacante: data, isLoading, isError };
}
