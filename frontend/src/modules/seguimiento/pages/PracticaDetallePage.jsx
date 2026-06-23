import { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { usePracticaSeguimiento } from '../hooks/usePracticaSeguimiento';
import { useSeguimientoMutaciones } from '../hooks/useSeguimientoMutaciones';
import { useBitacorasEstudiante } from '../hooks/useBitacorasEstudiante';
import { usePermisos } from '../../../shared/hooks/usePermisos';
import TimelineSeguimiento from '../components/TimelineSeguimiento';
import AlertasPanel from '../components/AlertasPanel';
import ObservacionModal from '../components/ObservacionModal';
import AvanceTutorModal from '../components/AvanceTutorModal';
import BitacoraModal from '../components/BitacoraModal';
import { Badge, Button, Card, LoadingState, PageBackHeader } from '@/shared/components/ui';
import {
  estadoSeguimientoPractica,
  formatearFechaSeguimiento,
  formatearFechaHoraSeguimiento,
  nombreEstudiantePractica,
} from '../utils/fechas';

const BADGE = {
  AL_DIA: { label: 'Al día', variant: 'success' },
  PENDIENTE: { label: 'Pendiente', variant: 'warning' },
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
  const { bitacoras } = useBitacorasEstudiante(id, { enabled: rol === 'DOCENTE_ASESOR' });

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

  return (
    <div className="mx-auto max-w-5xl">
      <PageBackHeader
        titulo={nombreEstudiantePractica(practica)}
        descripcion={[identificacion, programa].filter(Boolean).join(' — ') || undefined}
        onVolver={() => navigate('/seguimiento')}
        acciones={
          <Badge variant={badge.variant} className="px-3 py-1 text-sm">
            {badge.label}
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
                      </li>
                    ))}
                </ul>
              )}
            </Card>
          )}

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
                + Nueva bitácora
              </Button>
            )}
          </div>

          <Card padding="p-5">
            <h2 className="mb-4 text-base font-bold text-gray-900">Historial de actividades</h2>
            <TimelineSeguimiento timeline={timeline} />
          </Card>
        </div>

        <aside className="w-full shrink-0 xl:w-72">
          <AlertasPanel practicaId={Number(id)} />
        </aside>
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
