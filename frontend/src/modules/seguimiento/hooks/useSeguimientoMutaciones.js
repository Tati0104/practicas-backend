// src/modules/seguimiento/hooks/useSeguimientoMutaciones.js

/**
 * Agrupa las mutaciones del módulo Seguimiento:
 *   - registrarObservacion: POST /api/seguimiento/observaciones
 *   - registrarAvance:      POST /api/seguimiento/avances-tutor
 *   - registrarBitacora:    POST /api/seguimiento/bitacora
 *
 * Cada mutación invalida la query 'seguimiento' para refrescar el tablero.
 */
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { toast } from 'react-hot-toast';
import seguimientoService from '../services/seguimientoService';

export function useSeguimientoMutaciones({ onSuccess, onError } = {}) {
  const queryClient = useQueryClient();

  const alExito = (mensaje) => () => {
    queryClient.invalidateQueries(['seguimiento']);
    queryClient.invalidateQueries(['practicaSeguimiento']);
    toast.success(mensaje);
    if (onSuccess) onSuccess();
  };

  const alError = (err) => {
    const msg = err?.response?.data?.message || err?.message || 'Error inesperado';
    toast.error(msg);
    if (onError) onError(err);
  };

  const registrarObservacion = useMutation({
    mutationFn: (dto) => seguimientoService.registrarObservacion(dto),
    onSuccess: alExito('Observación registrada'),
    onError: alError,
  });

  const registrarAvance = useMutation({
    mutationFn: (dto) => seguimientoService.registrarAvance(dto),
    onSuccess: alExito('Avance registrado'),
    onError: alError,
  });

  const registrarBitacora = useMutation({
    mutationFn: (dto) => seguimientoService.registrarBitacora(dto),
    onSuccess: alExito('Entrada de bitácora guardada'),
    onError: alError,
  });

  return { registrarObservacion, registrarAvance, registrarBitacora };
}
