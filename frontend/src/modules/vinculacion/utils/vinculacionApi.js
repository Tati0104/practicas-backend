import asignacionService from '../../asignaciones/services/asignacionService';
import estudianteService from '../../estudiante/services/estudianteService';
import empresaService from '../../empresa/services/empresaService';
import vinculacionService from '../services/vinculacionService';
import { normalizarPagina } from '@/shared/utils/paginacion';

const TIPOS_DOCUMENTO = ['HOJA_VIDA', 'CARTA', 'PROYECTO', 'CONVENIO'];

const FIRMAS_VACIAS = [
  { tipoFirmante: 'TUTOR_EMPRESARIAL', firmado: false, fechaFirma: null },
  { tipoFirmante: 'ESTUDIANTE', firmado: false, fechaFirma: null },
];

export function documentosPendientesPorDefecto() {
  return TIPOS_DOCUMENTO.map((tipo) => ({
    id: null,
    tipo,
    nombre: null,
    estado: 'PENDIENTE',
    firmas: tipo === 'CONVENIO' ? FIRMAS_VACIAS : [],
  }));
}

function esErrorEndpointNuevo(err) {
  const status = err?.response?.status;
  return status === 403 || status === 404 || status === 405 || status === 501;
}

async function cargarCatalogosEnriquecimiento() {
  const [estResp, vacResp] = await Promise.all([
    estudianteService.listar({ page: 0, size: 500 }),
    empresaService.listarVacantes({ page: 0, size: 500 }),
  ]);

  const estudiantes = new Map(
    (estResp.data?.content ?? estResp.data ?? []).map((e) => [e.id, e])
  );
  const vacantes = new Map(
    (vacResp.data?.content ?? vacResp.data ?? []).map((v) => [v.id, v])
  );

  return { estudiantes, vacantes };
}

function mapearAsignacionBasica(asignacion, { estudiantes, vacantes }) {
  const est = estudiantes.get(asignacion.estudianteId);
  const vac = vacantes.get(asignacion.vacanteId);

  return {
    asignacionId: asignacion.id,
    practicaId: asignacion.instanciaPracticaId ?? null,
    estado: asignacion.estado,
    estudiante: {
      nombre: est?.nombre ?? '—',
      codigo: est?.identificacion ?? est?.codigo ?? String(asignacion.estudianteId),
      programa: est?.programaNombre ?? est?.programa?.nombre ?? '—',
    },
    vacante: {
      id: vac?.id ?? asignacion.vacanteId,
      cargo: vac?.cargo ?? '—',
      empresa: vac?.empresaNombre ?? vac?.empresa ?? '—',
      empresaId: vac?.empresaId ?? null,
      programaId: vac?.programaId ?? est?.programaId ?? null,
    },
    documentos: documentosPendientesPorDefecto(),
  };
}

function coincideBusqueda(vinculacion, busqueda) {
  const q = busqueda.trim().toLowerCase();
  if (!q) return true;

  const campos = [
    vinculacion.estudiante?.nombre,
    vinculacion.estudiante?.codigo,
    vinculacion.vacante?.cargo,
    vinculacion.vacante?.empresa,
  ];

  return campos.some((c) => String(c ?? '').toLowerCase().includes(q));
}

function aplicarFiltrosCliente(items, filtros) {
  return items.filter((item) => {
    if (filtros.estado && item.estado !== filtros.estado) return false;
    if (filtros.programaId && String(item.vacante?.programaId ?? '') !== String(filtros.programaId)) {
      return false;
    }
    if (filtros.empresaId && String(item.vacante?.empresaId ?? '') !== String(filtros.empresaId)) {
      return false;
    }
    if (filtros.busqueda && !coincideBusqueda(item, filtros.busqueda)) return false;
    return item.estado !== 'CANCELADA';
  });
}

async function listarDesdeAsignaciones(filtros) {
  const resp = await asignacionService.listar({
    page: filtros.page,
    size: filtros.size,
    estado: filtros.estado || undefined,
  });

  const page = normalizarPagina(resp.data, []);
  const catalogos = await cargarCatalogosEnriquecimiento();

  let content = page.content
    .filter((a) => a.estado !== 'CANCELADA')
    .map((a) => mapearAsignacionBasica(a, catalogos));

  const usaFiltroCliente =
    Boolean(filtros.busqueda) || Boolean(filtros.programaId) || Boolean(filtros.empresaId);

  if (usaFiltroCliente) {
    content = aplicarFiltrosCliente(content, filtros);
  }

  return {
    content,
    totalElements: usaFiltroCliente ? content.length : page.totalElements,
    totalPages: usaFiltroCliente
      ? Math.max(1, Math.ceil(content.length / (filtros.size || 10)))
      : page.totalPages,
  };
}

export async function listarVinculaciones(filtros) {
  try {
    const resp = await vinculacionService.listar(filtros);
    return normalizarPagina(resp.data, []);
  } catch (err) {
    if (!esErrorEndpointNuevo(err)) throw err;
    return listarDesdeAsignaciones(filtros);
  }
}

export function mapaTipoDesdeCategoria(categoria) {
  const mapa = {
    HOJA_VIDA: 'HOJA_VIDA',
    CARTA_PRESENTACION: 'CARTA',
    PROYECTO_PRACTICA: 'PROYECTO',
    CONVENIO_PRACTICA: 'CONVENIO',
    VINCULACION: 'CARTA',
    CONVENIO: 'CONVENIO',
  };
  return mapa[categoria] ?? categoria;
}

export function claveQueryDocumentos(asignacionId, useMocks = false) {
  return ['vinculacion-documentos', asignacionId, useMocks];
}

export function fusionarDocumentoSubido(cache, { tipo, archivo }, respuesta) {
  const payload = respuesta?.data?.data ?? respuesta?.data ?? respuesta ?? {};
  const documentoId = payload.documentoId ?? payload.id ?? null;
  const practicaId = payload.practicaId ?? null;
  const base = cache ?? normalizarRespuestaDocumentos(null);

  const documentos = base.documentos.map((doc) =>
    doc.tipo === tipo
      ? {
          ...doc,
          id: documentoId,
          nombre: archivo?.name ?? doc.nombre,
          estado: 'SUBIDO',
        }
      : doc
  );

  return {
    ...base,
    documentos,
    practicaId: practicaId ?? base.practicaId,
    asignacionId: payload.asignacionId ?? base.detalle?.asignacionId ?? base.asignacionId,
  };
}

export function normalizarRespuestaDocumentos(data) {
  if (!data) {
    return {
      documentos: documentosPendientesPorDefecto(),
      convenioId: null,
      practicaId: null,
      detalle: null,
    };
  }

  if (Array.isArray(data)) {
    data = { documentos: data };
  }

  if (data.documentosPorCategoria || data.documentos_por_categoria) {
    const porCategoria = data.documentosPorCategoria ?? data.documentos_por_categoria;
    const docs = [];
    Object.entries(porCategoria).forEach(([categoria, lista]) => {
      const ultimo = Array.isArray(lista) ? lista[0] : null;
      if (!ultimo) return;
      docs.push({
        id: ultimo.id,
        tipo: mapaTipoDesdeCategoria(categoria),
        categoria,
        nombre: ultimo.nombre,
        estado: 'SUBIDO',
        firmas: mapaTipoDesdeCategoria(categoria) === 'CONVENIO' ? FIRMAS_VACIAS : [],
      });
    });
    data = { ...data, documentos: docs };
  }

  const base = documentosPendientesPorDefecto();
  const recibidos = data.documentos ?? [];

  const documentos = base.map((slot) => {
    const encontrado = recibidos.find(
      (doc) => doc.tipo === slot.tipo || mapaTipoDesdeCategoria(doc.categoria) === slot.tipo
    );
    return encontrado ? { ...slot, ...encontrado, tipo: slot.tipo } : slot;
  });

  return {
    documentos,
    convenioId: data.convenioId ?? null,
    practicaId: data.practicaId ?? null,
    detalle: data,
  };
}

export async function obtenerDocumentosAsignacion(asignacionId) {
  try {
    const resp = await vinculacionService.obtenerDocumentos(asignacionId);
    return normalizarRespuestaDocumentos(resp.data?.data ?? resp.data);
  } catch (err) {
    if (!esErrorEndpointNuevo(err)) throw err;

    const asignacionResp = await asignacionService.obtener(asignacionId);
    const asignacion = asignacionResp.data?.asignacion ?? asignacionResp.data;
    const practicaId = asignacion?.instanciaPracticaId;

    if (!practicaId) {
      return normalizarRespuestaDocumentos({ asignacionId, practicaId: null, documentos: [] });
    }

    try {
      const docsResp = await vinculacionService.obtenerDocumentosPractica(practicaId);
      const payload = docsResp.data?.data ?? docsResp.data;
      return normalizarRespuestaDocumentos({
        asignacionId,
        practicaId,
        documentosPorCategoria: payload?.documentosPorCategoria ?? payload?.documentos_por_categoria,
      });
    } catch {
      return normalizarRespuestaDocumentos({ asignacionId, practicaId, documentos: [] });
    }
  }
}
