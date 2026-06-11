// src/modules/asignaciones/hooks/useEstudiantesAptos.js

/**
 * Hook para obtener la lista de estudiantes aptos para una vacante y programa.
 * GET /api/estudiantes-aptos?programaId=&vacanteId=
 *
 * Solo se activa si `vacanteId` tiene valor.
 * Los datos se usan para poblar el selector en AsignacionForm.
 */
import { useQuery } from '@tanstack/react-query';
import asignacionService from '../services/asignacionService';

export function useEstudiantesAptos({ programaId, vacanteId } = {}) {
  const { data, isLoading } = useQuery({
    queryKey: ['estudiantes-aptos', programaId, vacanteId],
    queryFn: () =>
      asignacionService
        .estudiantesAptos({ programaId, vacanteId })
        .then((r) => r.data?.data || r.data || []),
    // Solo busca estudiantes cuando se haya seleccionado una vacante
    enabled: !!vacanteId,
  });

  return { estudiantes: data || [], isLoading };
}
