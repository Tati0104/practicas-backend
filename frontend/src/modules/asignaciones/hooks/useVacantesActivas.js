// src/modules/asignaciones/hooks/useVacantesActivas.js

/**
 * Hook para obtener la lista de vacantes activas filtradas por programa.
 * GET /api/vacantes-activas?programaId=
 *
 * Los datos se usan para poblar el selector en AsignacionForm.
 */
import { useQuery } from '@tanstack/react-query';
import asignacionService from '../services/asignacionService';

export function useVacantesActivas({ programaId } = {}) {
  const { data, isLoading } = useQuery({
    queryKey: ['vacantes-activas', programaId],
    queryFn: () =>
      asignacionService
        .vacantesActivas({ programaId })
        .then((r) => r.data?.data || r.data || []),
    // Se ejecuta siempre (para que el select tenga opciones aunque no haya programa)
    enabled: true,
  });

  return { vacantes: data || [], isLoading };
}
