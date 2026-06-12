// src/modules/vinculacion/hooks/useVinculacionMutaciones.js

/**
 * Agrupa todas las mutaciones del módulo Vinculación:
 *
 *  - subirCarta(asignacionId, archivo)
 *  - subirConvenio(asignacionId, archivo)
 *  - confirmarFirma(convenioId, tipoFirmante)
 *
 * Cada mutación:
 *   1. Llama al servicio correspondiente.
 *   2. Invalida la query 'vinculacion-documentos' para refrescar el panel.
 *   3. Muestra toast.success o toast.error según el resultado.
 *   4. Llama al callback onSuccess/onError si se proveen.
 */
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { toast } from 'react-hot-toast';
import vinculacionService from '../services/vinculacionService';

export function useVinculacionMutaciones({ practicaId, onSuccess, onError } = {}) {
  const queryClient = useQueryClient();

  // Helper: invalida cache y llama callback externo
  const alExito = (mensaje) => () => {
    // Invalida documentos de la práctica actual para que se recarguen
    queryClient.invalidateQueries(['vinculacion-documentos', practicaId]);
    toast.success(mensaje);
    if (onSuccess) onSuccess();
  };

  // Helper: extrae mensaje legible del backend y muestra toast
  const alError = (err) => {
    const msg =
      err?.response?.data?.mensaje ||
      err?.response?.data?.message ||
      err?.message ||
      'Error inesperado. Intenta de nuevo.';
    toast.error(msg);
    if (onError) onError(err);
  };

  /**
   * Sube carta de presentación.
   * Parámetros: { asignacionId, archivo }
   */
  const subirCarta = useMutation({
    mutationFn: ({ asignacionId, archivo }) =>
      vinculacionService.subirCarta(asignacionId, archivo),
    onSuccess: alExito('Carta de presentación subida correctamente'),
    onError: alError,
  });

  /**
   * Sube convenio de práctica.
   * Parámetros: { asignacionId, archivo }
   */
  const subirConvenio = useMutation({
    mutationFn: ({ asignacionId, archivo }) =>
      vinculacionService.subirConvenio(asignacionId, archivo),
    onSuccess: alExito('Convenio subido correctamente'),
    onError: alError,
  });

  /**
   * Confirma la firma de un firmante.
   * Parámetros: { convenioId, tipoFirmante }
   * tipoFirmante: 'COORDINADOR' | 'TUTOR' | 'ESTUDIANTE'
   */
  const confirmarFirma = useMutation({
    mutationFn: ({ convenioId, tipoFirmante }) =>
      vinculacionService.confirmarFirma(convenioId, tipoFirmante),
    onSuccess: alExito('Firma confirmada'),
    onError: alError,
  });

  return { subirCarta, subirConvenio, confirmarFirma };
}
