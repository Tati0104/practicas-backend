import useAuthStore from '@/store/authStore';
import seguimientoService from '@/modules/seguimiento/services/seguimientoService';
import { listarVinculaciones } from '@/modules/vinculacion/utils/vinculacionApi';
import dashboardService from '../services/dashboardService';

function esEndpointPanelNuevo(err) {
  const status = err?.response?.status;
  return status === 403 || status === 404 || status === 405 || status === 501;
}

function contarDocumentosCompletos(documentos = []) {
  return documentos.filter((d) => {
    if (d.estado === 'PENDIENTE') return false;
    if (d.tipo === 'CONVENIO') return d.estado === 'FIRMADO';
    return true;
  }).length;
}

function mapearPracticaDesdeVinculacion(v) {
  const docs = v.documentos ?? [];
  return {
    asignacionId: v.asignacionId ?? null,
    practicaId: v.practicaId ?? null,
    numeroPractica: v.numeroPractica,
    cargo: v.vacante?.cargo ?? null,
    empresa: v.vacante?.empresa ?? null,
    estadoAsignacion: v.estado ?? null,
    estadoPractica: v.estadoPractica ?? null,
    documentosCompletos: contarDocumentosCompletos(docs),
    documentosTotal: docs.length || 4,
    convenioFirmadoEstudiante: Boolean(v.convenioFirmadoEstudiante),
    estadoSeguimiento: '—',
    estadoEncuesta: 'PENDIENTE',
    notaFinal: null,
    practicaFinalizada:
      v.estadoPractica === 'COMPLETADA' || v.estadoPractica === 'REPROBADA',
  };
}

async function cargarPanelEstudianteFallback() {
  const { nombre, correo } = useAuthStore.getState();

  const [practicasResp, vincResp] = await Promise.all([
    seguimientoService.practicas({ page: 0, size: 50 }).catch(() => ({ data: [] })),
    listarVinculaciones({ page: 0, size: 50 }).catch(() => ({ content: [] })),
  ]);

  const payload = practicasResp.data?.data ?? practicasResp.data ?? [];
  const practicasSeguimiento = Array.isArray(payload) ? payload : (payload.content ?? []);
  const vinculaciones = vincResp.content ?? [];

  const vincPorPractica = new Map(
    vinculaciones.filter((v) => v.practicaId).map((v) => [String(v.practicaId), v])
  );

  let practicas = practicasSeguimiento.map((p) => {
    const vinc = vincPorPractica.get(String(p.id));
    const docs = vinc?.documentos ?? [];
    return {
      asignacionId: vinc?.asignacionId ?? null,
      practicaId: p.id,
      numeroPractica: p.numeroPractica,
      cargo: vinc?.vacante?.cargo ?? null,
      empresa: p.empresa ?? vinc?.vacante?.empresa ?? null,
      estadoAsignacion: vinc?.estado ?? null,
      estadoPractica: p.estadoPractica ?? null,
      documentosCompletos: contarDocumentosCompletos(docs),
      documentosTotal: docs.length || 4,
      convenioFirmadoEstudiante: Boolean(vinc?.convenioFirmadoEstudiante),
      estadoSeguimiento: p.estadoSeguimiento ?? '—',
      estadoEncuesta: 'PENDIENTE',
      notaFinal: null,
      practicaFinalizada:
        p.estadoPractica === 'COMPLETADA' || p.estadoPractica === 'REPROBADA',
    };
  });

  if (practicas.length === 0 && vinculaciones.length > 0) {
    practicas = vinculaciones.map(mapearPracticaDesdeVinculacion);
  }

  return {
    nombre,
    correo,
    identificacion: null,
    programa: null,
    practicas,
  };
}

export async function cargarPanelEstudiante() {
  try {
    const resp = await dashboardService.obtenerPanelEstudiante();
    return resp.data?.data ?? resp.data;
  } catch (err) {
    if (!esEndpointPanelNuevo(err)) throw err;
    return cargarPanelEstudianteFallback();
  }
}
