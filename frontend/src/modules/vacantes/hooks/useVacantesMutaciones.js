// src/modules/vacantes/hooks/useVacantesMutaciones.js

/**
 * Agrupa todas las mutaciones relacionadas con vacantes.
 * Cada mutación muestra un toast de éxito/error y opcionalmente invalida la query
 * 'vacantes' para refrescar la lista.
 */
import { useMutation, useQueryClient } from '@tanstack/react-query';
import empresaService from '../../services/empresaService'; // reutiliza el mismo servicio
import { toast } from 'react-hot-toast';

export function useVacantesMutaciones({ onSuccess, onError } = {}) {
  const queryClient = useQueryClient();

  const crear = useMutation({
    mutationFn: dto => empresaService.crearVacante(dto),
    onSuccess: () => {
      queryClient.invalidateQueries(['vacantes']);
      if (onSuccess) onSuccess();
    },
    onError,
  });

  const editar = useMutation({
    mutationFn: ({ id, dto }) => empresaService.editarVacante(id, dto),
    onSuccess: () => {
      queryClient.invalidateQueries(['vacantes']);
      if (onSuccess) onSuccess();
    },
    onError,
  });

  const aprobar = useMutation({
    mutationFn: id => empresaService.aprobarVacante(id),
    onSuccess: () => {
      queryClient.invalidateQueries(['vacantes']);
      if (onSuccess) onSuccess();
    },
    onError,
  });

  const rechazar = useMutation({
    mutationFn: ({ id, motivo }) => empresaService.rechazarVacante(id, motivo),
    onSuccess: () => {
      queryClient.invalidateQueries(['vacantes']);
      if (onSuccess) onSuccess();
    },
    onError,
  });

  const pausar = useMutation({
    mutationFn: id => empresaService.pausarVacante(id),
    onSuccess: () => {
      queryClient.invalidateQueries(['vacantes']);
      if (onSuccess) onSuccess();
    },
    onError,
  });

  const cerrar = useMutation({
    mutationFn: id => empresaService.cerrarVacante(id),
    onSuccess: () => {
      queryClient.invalidateQueries(['vacantes']);
      if (onSuccess) onSuccess();
    },
    onError,
  });

  return { crear, editar, aprobar, rechazar, pausar, cerrar };
}
