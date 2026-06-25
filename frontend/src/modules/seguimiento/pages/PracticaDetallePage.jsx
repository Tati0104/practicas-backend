import { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { toast } from 'react-hot-toast';
import { Download, Paperclip } from 'lucide-react';
import { usePracticaSeguimiento } from '../hooks/usePracticaSeguimiento';
import { useSeguimientoMutaciones } from '../hooks/useSeguimientoMutaciones';
import { useBitacorasEstudiante } from '../hooks/useBitacorasEstudiante';
import { usePermisos } from '../../../shared/hooks/usePermisos';
import TimelineSeguimiento from '../components/TimelineSeguimiento';
import ObservacionModal from '../components/ObservacionModal';
import AvanceTutorModal from '../components/AvanceTutorModal';
import BitacoraModal from '../components/BitacoraModal';
import PanelAccesosExpediente from '../components/PanelAccesosExpediente';
import { Badge, Button, Card, LoadingState, PageBackHeader } from '@/shared/components/ui';
import seguimientoService from '../services/seguimientoService';
import {
  estadoSeguimientoPractica,
  formatearFechaSeguimiento,
  formatearFechaHoraSeguimiento,
  nombreEstudiantePractica,
} from '../utils/fechas';
import {
  ROLES_EXPEDIENTE,
  badgeEstadoPractica,
  practicaFinalizada,
} from '../utils/estadosPractica';

const BADGE = {
  AL_DIA: { label: 'Revisado / Al día', variant: 'success' },
  PENDIENTE: { label: 'Pendiente de revisión', variant: 'warning' },
  EN_ALERTA: { label: 'En alerta', variant: 'danger' },
};

const CAMPOS_INFO = [
  { label: 'Programa', key: 'programaNombre' },
  { label: 'Práctica', key: 'numeroPractica' },
  { label: 'Empresa', key: 'empresa' },
  { label: 'Cargo', key: 'cargo' },
  { label: 'Docente', key: 'docente' },
  { label: 'Tutor', key: 'tutor' },
  { label: 'Inicio', key: 'fechaInicio' },
  { label: 'Fin', key: 'fechaFin' },
  { label: 'Nota final', key: 'notaFinal' },
];

function valorCampo(practica, key) {
  const valor = practica?.[key];
  if (key === 'notaFinal') {
    return valor != null ? Number(valor).toFixed(1) : '—';
  }
  if (key === 'numeroPractica') {
    return valor != null ? `Práctica ${valor}` : '—';
  }
  if (!valor) return '—';
  if (key === 'fechaInicio' || key === 'fechaFin') {
    return formatearFechaSeguimiento(valor);
  }
  return valor;
}

export default function PracticaDetallePage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { practica, timeline, isLoading, isError } = usePracticaSeguimiento(id);
  const { usuario } = usePermisos();
  const rol = usuario?.rol;
  const { bitacoras } = useBitacorasEstudiante(id, { enabled: rol === 'DOCENTE_ASESOR' });

  const [modalObservacion, setModalObservacion] = useState(false);
  const [modalAvance, setModalAvance] = useState(false);
  const [modalBitacora, setModalBitacora] = useState(false);

  const handleDescargarArchivo = async (bitacoraId, nombreArchivo) => {
    try {
      const resp = await seguimientoService.descargarArchivoBitacora(bitacoraId);
      const url = URL.createObjectURL(resp.data);
      const link = document.createElement('a');
      link.href = url;
      link.download = nombreArchivo || `bitacora_${bitacoraId}`;
      link.click();
      URL.revokeObjectURL(url);
    } catch {
      toast.error('No se pudo descargar el archivo');
    }
  };

  const { registrarObservacion, registrarAvance, registrarBitacora } =
    useSeguimientoMutaciones({
      onSuccess: () => {
        setModalObservacion(false);
        setModalAvance(false);
        setModalBitacora(false);
      },
    });

  if (isLoading) {
    return <LoadingState mensaje="Cargando detalle..." />;
  }

  if (isError || !practica) {
    return (
      <p className="p-5 text-sm text-red-700" role="alert">
        Error al cargar la práctica.
      </p>
    );
  }

  const estadoClave = estadoSeguimientoPractica(practica);
  const badge = BADGE[estadoClave] || { label: estadoClave, variant: 'neutral' };
  const estadoPracticaBadge = badgeEstadoPractica(practica.estadoPractica);
  const avance = practica.porcentajeAvance || 0;
  const identificacion =
    typeof practica.estudiante === 'object'
      ? practica.estudiante?.identificacion ?? practica.estudiante?.codigo
      : null;
  const programa =
    practica.programaNombre ??
    (typeof practica.estudiante === 'object' ? practica.estudiante?.programa : null);
  const esEstudiante = rol === 'ESTUDIANTE';
  const esExpediente = ROLES_EXPEDIENTE.includes(rol);
  const finalizada = practicaFinalizada(practica.estadoPractica);
  const tituloDetalle = esEstudiante
    ? `Seguimiento — Práctica ${practica.numeroPractica ?? id}`
    : nombreEstudiantePractica(practica);
  const descripcionDetalle = esEstudiante
    ? [practica.empresa, practica.cargo].filter(Boolean).join(' · ') || undefined
    : [identificacion, programa].filter(Boolean).join(' — ') || undefined;
  const mostrarAccionesRegistro =
    !finalizada &&
    (rol === 'DOCENTE_ASESOR' || rol === 'TUTOR_EMPRESARIAL' || rol === 'ESTUDIANTE');

  return (
    <div className="mx-auto max-w-5xl">
      <PageBackHeader
        titulo={tituloDetalle}
        descripcion={descripcionDetalle}
        onVolver={() => navigate('/seguimiento')}
        acciones={
          <div className="flex flex-wrap items-center gap-2">
            {practica.estadoPractica && (
              <Badge variant={estadoPracticaBadge.variant} className="px-3 py-1 text-sm">
                {estadoPracticaBadge.label}
              </Badge>
            )}
            {!finalizada && (
              <Badge variant={badge.variant} className="px-3 py-1 text-sm">
                {esEstudiante && estadoClave === 'AL_DIA'
                  ? 'Revisado por docente'
                  : badge.label}
              </Badge>
            )}
          </div>
        }
      />

      <div className="flex flex-col gap-5 xl:flex-row xl:items-start">
        <div className="min-w-0 flex-1">
          {finalizada && esExpediente && (
            <div className="mb-5 rounded-lg border border-slate-200 bg-slate-50 px-4 py-3 text-sm text-slate-700">
              Esta práctica ya fue cerrada. Puedes consultar documentos, evaluaciones y el checklist
              de cierre desde el panel lateral.
            </div>
          )}
          <Card className="mb-5" padding="p-5">
            <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-3">
              {CAMPOS_INFO.map(({ label, key }) => (
                <div key={key}>
                  <p className="text-xs font-semibold uppercase tracking-wide text-gray-400">
                    {label}
                  </p>
                  <p className="text-sm font-medium text-gray-900">{valorCampo(practica, key)}</p>
                </div>
              ))}
            </div>

            <div className="mt-4">
              <div className="mb-1 flex justify-between text-sm text-gray-700">
                <span>Avance general</span>
                <span>{avance}%</span>
              </div>
              <div className="h-2 overflow-hidden rounded-full bg-gray-200">
                <div
                  className="h-full rounded-full bg-primary transition-all"
                  style={{ width: `${avance}%` }}
                />
              </div>
            </div>
          </Card>

          {rol === 'DOCENTE_ASESOR' && (
            <Card className="mb-5" padding="p-5">
              <h2 className="mb-4 text-base font-bold text-gray-900">Seguimientos del estudiante</h2>
              {bitacoras.length === 0 ? (
                <p className="py-4 text-center text-sm text-gray-400">
                  El estudiante aún no ha registrado bitácoras.
                </p>
              ) : (
                <ul className="flex flex-col gap-4">
                  {[...bitacoras]
                    .sort((a, b) => new Date(b.fecha) - new Date(a.fecha))
                    .map((entrada) => (
                      <li key={entrada.id} className="rounded-lg border border-violet-100 bg-violet-50 p-4">
                        <div className="mb-1 flex flex-wrap items-center justify-between gap-2">
                          {entrada.corte != null && (
                            <span className="inline-flex rounded-full bg-violet-100 px-2.5 py-0.5 text-xs font-semibold text-violet-800">
                              Corte {entrada.corte}
                            </span>
                          )}
                          <span className="text-xs text-gray-400">
                            {formatearFechaHoraSeguimiento(entrada.fecha)}
                          </span>
                        </div>
                        <p className="text-sm text-gray-700">{entrada.descripcion}</p>
                        {entrada.nombreArchivo && (
                          <div className="mt-2 flex items-center gap-2">
                            <Paperclip className="h-3.5 w-3.5 shrink-0 text-violet-500" aria-hidden="true" />
                            <span className="truncate text-xs text-gray-500">{entrada.nombreArchivo}</span>
                            <button
                              type="button"
                              onClick={() => handleDescargarArchivo(entrada.id, entrada.nombreArchivo)}
                              className="ml-auto flex items-center gap-1 rounded-md bg-violet-100 px-2 py-1 text-xs font-semibold text-violet-700 hover:bg-violet-200"
                            >
                              <Download className="h-3 w-3" aria-hidden="true" />
                              Descargar
                            </button>
                          </div>
                        )}
                      </li>
                    ))}
                </ul>
              )}
            </Card>
          )}

          <div className="mb-5">
            {mostrarAccionesRegistro && rol === 'DOCENTE_ASESOR' && (
              <Button onClick={() => setModalObservacion(true)}>+ Registrar observación</Button>
            )}
            {mostrarAccionesRegistro && rol === 'TUTOR_EMPRESARIAL' && (
              <Button
                variant="success"
                className="bg-emerald-600 text-white hover:bg-emerald-700"
                onClick={() => setModalAvance(true)}
              >
                + Registrar avance
              </Button>
            )}
            {mostrarAccionesRegistro && rol === 'ESTUDIANTE' && (
              <Button
                variant="primary"
                className="bg-violet-600 hover:bg-violet-700"
                onClick={() => setModalBitacora(true)}
              >
                {esEstudiante ? '+ Nueva entrega de seguimiento' : '+ Nueva bitácora'}
              </Button>
            )}
          </div>

          <Card padding="p-5">
            <h2 className="mb-4 text-base font-bold text-gray-900">Historial de actividades</h2>
            {rol === 'ESTUDIANTE' && (
              <p className="mb-4 text-sm text-gray-600">
                Las observaciones del docente asesor aparecen como comentarios. El estado{' '}
                <strong>Pendiente de revisión</strong> indica que tu entrega aún no fue revisada.
              </p>
            )}
            <TimelineSeguimiento timeline={timeline} />
          </Card>
        </div>

        {esExpediente && (
          <PanelAccesosExpediente practicaId={id} soloLectura={finalizada} />
        )}
      </div>

      <ObservacionModal
        isOpen={modalObservacion}
        practicaId={Number(id)}
        numCortes={practica?.numCortes ?? 3}
        onClose={() => setModalObservacion(false)}
        onGuardar={(dto) => registrarObservacion.mutate(dto)}
        isPending={registrarObservacion.isPending}
      />
      <AvanceTutorModal
        isOpen={modalAvance}
        practicaId={Number(id)}
        onClose={() => setModalAvance(false)}
        onGuardar={(dto) => registrarAvance.mutate(dto)}
        isPending={registrarAvance.isPending}
      />
      <BitacoraModal
        isOpen={modalBitacora}
        practicaId={Number(id)}
        onClose={() => setModalBitacora(false)}
        onGuardar={(dto) => registrarBitacora.mutate(dto)}
        isPending={registrarBitacora.isPending}
      />
    </div>
  );
}
