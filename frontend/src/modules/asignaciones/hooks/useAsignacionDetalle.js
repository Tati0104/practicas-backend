// src/modules/asignaciones/hooks/useAsignacionDetalle.js

/**
 * Hook que obtiene el detalle completo de una asignación dado su ID.
 * GET /api/asignaciones/{id}
 *
 * Solo se activa si `id` es válido (enabled: !!id).
 * Devuelve el objeto asignacion, estados de carga y error.
 */
import { useQuery } from '@tanstack/react-query';
import asignacionService from '../services/asignacionService';

export function useAsignacionDetalle(id) {
  const { data, isLoading, isError } = useQuery({
    queryKey: ['asignacion', id],
    queryFn: () => asignacionService.obtener(id).then((r) => r.data?.data || r.data),
    // Solo ejecuta la query si hay un id
    enabled: !!id,
  });

  return { asignacion: data, isLoading, isError };
}
