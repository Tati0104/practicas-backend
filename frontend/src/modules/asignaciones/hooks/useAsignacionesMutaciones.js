// src/modules/asignaciones/hooks/useAsignacionesMutaciones.js

/**
 * Agrupa las mutaciones del módulo Asignaciones:
 *   - crear: POST /api/asignaciones
 *   - cancelar: PATCH /api/asignaciones/{id}/cancelar
 *
 * Cada mutación invalida la query 'asignaciones' para refrescar la lista
 * y dispara toasts de éxito/error.
 */
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { toast } from 'react-hot-toast';
import asignacionService from '../services/asignacionService';

export function useAsignacionesMutaciones({ onSuccess, onError } = {}) {
  const queryClient = useQueryClient();

  // Función helper: invalida cache y llama callback externo si existe
  const alExito = (mensaje) => () => {
    queryClient.invalidateQueries(['asignaciones']);
    toast.success(mensaje);
    if (onSuccess) onSuccess();
  };

  // Helper para error: muestra toast y llama callback externo
  const alError = (err) => {
    const msg = err?.response?.data?.message || err?.message || 'Error inesperado';
    toast.error(msg);
    if (onError) onError(err);
  };

  /**
   * Crear asignación.
   * Body esperado: { estudianteId: number, vacanteId: number }
   */
  const crear = useMutation({
    mutationFn: (dto) => asignacionService.crear(dto),
    onSuccess: alExito('Asignación creada exitosamente'),
    onError: alError,
  });

  /**
   * Cancelar asignación.
   * Parámetros: { id, motivo }
   */
  const cancelar = useMutation({
    mutationFn: ({ id, motivo }) => asignacionService.cancelar(id, motivo),
    onSuccess: alExito('Asignación cancelada'),
    onError: alError,
  });

  return { crear, cancelar };
}
