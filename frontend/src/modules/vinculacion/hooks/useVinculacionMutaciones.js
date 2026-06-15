import { useMutation, useQueryClient } from '@tanstack/react-query';
import { toast } from 'react-hot-toast';
import vinculacionService from '../services/vinculacionService';

const CATEGORIA_API = {
  HOJA_VIDA: 'HOJA_VIDA',
  CARTA: 'CARTA_PRESENTACION',
  PROYECTO: 'PROYECTO_PRACTICA',
  CONVENIO: 'CONVENIO_PRACTICA',
};

const MENSAJES = {
  HOJA_VIDA: 'Hoja de vida subida correctamente',
  CARTA: 'Carta de presentación subida correctamente',
  PROYECTO: 'Documento del proyecto subido correctamente',
  CONVENIO: 'Convenio subido correctamente',
};

async function subirConFallback(asignacionId, tipo, archivo) {
  try {
    return await vinculacionService.subirDocumento(asignacionId, CATEGORIA_API[tipo], archivo);
  } catch (err) {
    const status = err?.response?.status;
    if (status !== 404 && status !== 405 && status !== 501) throw err;

    if (tipo === 'CARTA') return vinculacionService.subirCarta(asignacionId, archivo);
    if (tipo === 'CONVENIO') return vinculacionService.subirConvenio(asignacionId, archivo);

    throw err;
  }
}

export function useVinculacionMutaciones({ asignacionId, onSuccess, onError } = {}) {
  const queryClient = useQueryClient();

  const alExito = (mensaje) => () => {
    queryClient.invalidateQueries(['vinculacion-documentos', asignacionId]);
    queryClient.invalidateQueries(['vinculacion']);
    toast.success(mensaje);
    onSuccess?.();
  };

  const alError = (err) => {
    const msg =
      err?.response?.data?.mensaje ||
      err?.response?.data?.message ||
      err?.message ||
      'Error inesperado. Intenta de nuevo.';
    toast.error(msg);
    onError?.(err);
  };

  const subirDocumento = useMutation({
    mutationFn: ({ tipo, archivo }) => subirConFallback(asignacionId, tipo, archivo),
    onSuccess: (_, { tipo }) => alExito(MENSAJES[tipo] ?? 'Documento subido')(),
    onError: alError,
  });

  const confirmarFirma = useMutation({
    mutationFn: ({ convenioId, tipoFirmante }) =>
      vinculacionService.confirmarFirma(convenioId, tipoFirmante),
    onSuccess: alExito('Firma confirmada'),
    onError: alError,
  });

  return { subirDocumento, confirmarFirma };
}
