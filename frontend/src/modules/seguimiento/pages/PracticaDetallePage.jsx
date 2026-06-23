import { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { usePracticaSeguimiento } from '../hooks/usePracticaSeguimiento';
import { useSeguimientoMutaciones } from '../hooks/useSeguimientoMutaciones';
import { usePermisos } from '../../../shared/hooks/usePermisos';
import TimelineSeguimiento from '../components/TimelineSeguimiento';
import ObservacionModal from '../components/ObservacionModal';
import AvanceTutorModal from '../components/AvanceTutorModal';
import BitacoraModal from '../components/BitacoraModal';
import { Badge, Button, Card, LoadingState, PageBackHeader } from '@/shared/components/ui';
import {
  estadoSeguimientoPractica,
  formatearFechaSeguimiento,
  nombreEstudiantePractica,
} from '../utils/fechas';

const BADGE = {
  AL_DIA: { label: 'Revisado / Al día', variant: 'success' },
  PENDIENTE: { label: 'Pendiente de revisión', variant: 'warning' },
  EN_ALERTA: { label: 'En alerta', variant: 'danger' },
};

const CAMPOS_INFO = [
  { label: 'Empresa', key: 'empresa' },
  { label: 'Cargo', key: 'cargo' },
  { label: 'Docente', key: 'docente' },
  { label: 'Tutor', key: 'tutor' },
  { label: 'Inicio', key: 'fechaInicio' },
  { label: 'Fin', key: 'fechaFin' },
];

function valorCampo(practica, key) {
  const valor = practica?.[key];
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

  const [modalObservacion, setModalObservacion] = useState(false);
  const [modalAvance, setModalAvance] = useState(false);
  const [modalBitacora, setModalBitacora] = useState(false);

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
  const avance = practica.porcentajeAvance || 0;
  const identificacion =
    typeof practica.estudiante === 'object'
      ? practica.estudiante?.identificacion ?? practica.estudiante?.codigo
      : null;
  const programa =
    typeof practica.estudiante === 'object' ? practica.estudiante?.programa : null;
  const esEstudiante = rol === 'ESTUDIANTE';
  const tituloDetalle = esEstudiante
    ? `Seguimiento — Práctica ${practica.numeroPractica ?? id}`
    : nombreEstudiantePractica(practica);
  const descripcionDetalle = esEstudiante
    ? [practica.empresa, practica.cargo].filter(Boolean).join(' · ') || undefined
    : [identificacion, programa].filter(Boolean).join(' — ') || undefined;

  return (
    <div className="mx-auto max-w-5xl">
      <PageBackHeader
        titulo={tituloDetalle}
        descripcion={descripcionDetalle}
        onVolver={() => navigate('/seguimiento')}
        acciones={
          <Badge variant={badge.variant} className="px-3 py-1 text-sm">
            {esEstudiante && estadoClave === 'AL_DIA'
              ? 'Revisado por docente'
              : badge.label}
          </Badge>
        }
      />

      <div className="flex flex-col gap-5 xl:flex-row xl:items-start">
        <div className="min-w-0 flex-1">
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

          <div className="mb-5">
            {rol === 'DOCENTE_ASESOR' && (
              <Button onClick={() => setModalObservacion(true)}>+ Registrar observación</Button>
            )}
            {rol === 'TUTOR_EMPRESARIAL' && (
              <Button
                variant="success"
                className="bg-emerald-600 text-white hover:bg-emerald-700"
                onClick={() => setModalAvance(true)}
              >
                + Registrar avance
              </Button>
            )}
            {rol === 'ESTUDIANTE' && (
              <Button
                variant="primary"
                className="bg-violet-600 hover:bg-violet-700"
                onClick={() => setModalBitacora(true)}
              >
              {esEstudiante ? (
            <>
              + Nueva entrega de seguimiento
            </>
          ) : (
            '+ Nueva bitácora'
          )}
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
      </div>

      <ObservacionModal
        isOpen={modalObservacion}
        practicaId={Number(id)}
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
