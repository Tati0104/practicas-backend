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
import vinculacionService from '../../vinculacion/services/vinculacionService';
import useAuthStore from '@/store/authStore';
import { extraerMensajeError } from '@/modules/auth/utils/schemas';

function obtenerUsuarioIdSesion() {
  return useAuthStore.getState().usuario?.id ?? null;
}

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
    const data = err?.response?.data;
    const detalle =
      data?.data && typeof data.data === 'object'
        ? Object.values(data.data).filter(Boolean).join('. ')
        : '';
    const msg = detalle || extraerMensajeError(err);
    toast.error(msg);
    if (onError) onError(err);
  };

  /**
   * Crear asignación.
   * Body esperado: { estudianteId: number, vacanteId: number, coordinadorId?: number }
   */
  const crear = useMutation({
    mutationFn: (dto) => {
      const coordinadorId = dto.coordinadorId ?? obtenerUsuarioIdSesion();
      if (!coordinadorId) {
        return Promise.reject(new Error('No se encontró tu usuario en sesión. Cierra sesión e ingresa de nuevo.'));
      }
      return asignacionService.crear({ ...dto, coordinadorId });
    },
    onSuccess: alExito('Asignación creada exitosamente'),
    onError: alError,
  });

  /**
   * Cancelar asignación.
   * Parámetros: { id, motivo }
   */
  const cancelar = useMutation({
    mutationFn: ({ id, motivo }) => {
      const responsableId = obtenerUsuarioIdSesion();
      if (!responsableId) {
        return Promise.reject(new Error('No se encontró tu usuario en sesión. Cierra sesión e ingresa de nuevo.'));
      }
      return asignacionService.cancelar(id, { motivo, responsableId });
    },
    onSuccess: alExito('Asignación cancelada'),
    onError: alError,
  });

  /**
   * Asignar/cambiar el docente asesor de la práctica vinculada a una asignación.
   * Parámetros: { asignacionId, docenteAsesorId }
   */
  const asignarDocente = useMutation({
    mutationFn: ({ asignacionId, docenteAsesorId }) =>
      vinculacionService.asignarDocenteAsesor(asignacionId, docenteAsesorId),
    onSuccess: alExito('Docente asesor asignado correctamente'),
    onError: alError,
  });

  return { crear, cancelar, asignarDocente };
}
