import { useMutation, useQueryClient } from '@tanstack/react-query';
import { toast } from 'react-hot-toast';
import { usarMocks } from '@/shared/config/dataSource';
import vinculacionService from '../services/vinculacionService';
import {
  claveQueryDocumentos,
  fusionarDocumentoSubido,
  obtenerDocumentosAsignacion,
  obtenerDocumentosPracticaDetalle,
} from '../utils/vinculacionApi';

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

async function subirConFallback({ asignacionId, practicaId, tipo, archivo }) {
  const categoria = CATEGORIA_API[tipo];
  try {
    if (asignacionId) {
      return await vinculacionService.subirDocumento(asignacionId, categoria, archivo);
    }
    return await vinculacionService.subirDocumentoPractica(practicaId, categoria, archivo);
  } catch (err) {
    const status = err?.response?.status;
    if (status !== 404 && status !== 405 && status !== 501) throw err;

    if (asignacionId) {
      if (tipo === 'CARTA') return vinculacionService.subirCarta(asignacionId, archivo);
      if (tipo === 'CONVENIO') return vinculacionService.subirConvenio(asignacionId, archivo);
    }

    throw err;
  }
}

export function useVinculacionMutaciones({ asignacionId, practicaId, onSuccess, onError } = {}) {
  const queryClient = useQueryClient();
  const idAsignacion = asignacionId || null;
  const idPractica = practicaId || null;
  const queryKey = claveQueryDocumentos(idAsignacion, idPractica, usarMocks());

  const sincronizarDocumentos = async () => {
    if (usarMocks() || (!idAsignacion && !idPractica)) return;
    try {
      const data = idAsignacion
        ? await obtenerDocumentosAsignacion(idAsignacion)
        : await obtenerDocumentosPracticaDetalle(idPractica);
      queryClient.setQueryData(queryKey, (prev) => {
        if (!prev?.documentos?.length) return data;

        const documentos = data.documentos.map((doc) => {
          if (doc.id) return doc;
          const local = prev.documentos.find((item) => item.tipo === doc.tipo);
          return local?.id ? { ...doc, ...local } : doc;
        });

        return { ...data, documentos };
      });
    } catch {
      // Si falla la recarga, conservamos la caché actualizada localmente.
    }
  };

  const alExitoSubida = (mensaje) => async (response, variables) => {
    queryClient.setQueryData(queryKey, (prev) =>
      fusionarDocumentoSubido(prev, variables, response)
    );
    await sincronizarDocumentos();
    queryClient.invalidateQueries({ queryKey: ['vinculacion'] });
    toast.success(mensaje);
    onSuccess?.();
  };

  const alExito = (mensaje) => async () => {
    await sincronizarDocumentos();
    await queryClient.invalidateQueries({ queryKey: claveQueryDocumentos(idAsignacion, idPractica, false) });
    await queryClient.refetchQueries({ queryKey: claveQueryDocumentos(idAsignacion, idPractica, false) });
    queryClient.invalidateQueries({ queryKey: ['vinculacion'] });
    queryClient.invalidateQueries({ queryKey: ['seguimiento'] });
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
    mutationFn: ({ tipo, archivo }) =>
      subirConFallback({ asignacionId: idAsignacion, practicaId: idPractica, tipo, archivo }),
    onSuccess: (response, variables) =>
      alExitoSubida(MENSAJES[variables.tipo] ?? 'Documento subido')(response, variables),
    onError: alError,
  });

  const confirmarFirma = useMutation({
    mutationFn: ({ convenioId, tipoFirmante }) =>
      vinculacionService.confirmarFirma(convenioId, tipoFirmante),
    onSuccess: alExito('Firma confirmada'),
    onError: alError,
  });

  const activarPractica = useMutation({
    mutationFn: ({ practicaId: pid, payload }) => vinculacionService.activarPractica(pid, payload),
    onSuccess: alExito('Práctica activada y vinculada exitosamente'),
    onError: alError,
  });

  const asignarDocenteAsesor = useMutation({
    mutationFn: ({ asignacionId: aid, docenteAsesorId }) =>
      vinculacionService.asignarDocenteAsesor(aid, docenteAsesorId),
    onSuccess: alExito('Docente Asesor asignado correctamente'),
    onError: alError,
  });

  return { subirDocumento, confirmarFirma, activarPractica, asignarDocenteAsesor };
}
