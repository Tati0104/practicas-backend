import { Link } from 'react-router-dom';
import { FileText, Activity, Star, CheckCircle2, Clock, AlertCircle } from 'lucide-react';
import { usePanelEstudiante } from '../hooks/usePanelEstudiante';
import CentroAlertas from './CentroAlertas';
import { PageHeader, Badge, Card, LoadingState, ErrorState } from '@/shared/components/ui';

const ESTADO_PRACTICA = {
  EN_CURSO: { label: 'En curso', variant: 'success' },
  COMPLETADA: { label: 'Finalizada', variant: 'neutral' },
  REPROBADA: { label: 'Reprobada', variant: 'danger' },
  ASIGNADA_PENDIENTE_INICIO: { label: 'Pendiente de inicio', variant: 'warning' },
  CANCELADA: { label: 'Cancelada', variant: 'neutral' },
};

const ESTADO_ENCUESTA = {
  COMPLETADA: 'Encuesta realizada',
  EN_BORRADOR: 'Encuesta en borrador',
  PENDIENTE: 'Encuesta pendiente',
};

function PracticaResumenCard({ practica }) {
  const estado = ESTADO_PRACTICA[practica.estadoPractica] ?? {
    label: practica.estadoPractica ?? 'Sin estado',
    variant: 'neutral',
  };
  const encuestaLabel = ESTADO_ENCUESTA[practica.estadoEncuesta] ?? practica.estadoEncuesta;

  return (
    <Card padding="p-5" className="space-y-4">
      <div className="flex flex-wrap items-start justify-between gap-3">
        <div>
          <h3 className="text-base font-bold text-gray-900">
            Práctica {practica.numeroPractica ?? '—'}
          </h3>
          <p className="text-sm text-gray-600">
            {practica.cargo ?? 'Cargo por definir'}
            {practica.empresa ? ` · ${practica.empresa}` : ''}
          </p>
        </div>
        <Badge variant={estado.variant}>{estado.label}</Badge>
      </div>

      <div className="grid grid-cols-1 gap-2 text-sm text-gray-700 sm:grid-cols-2">
        <p>
          <span className="font-medium">Documentos:</span>{' '}
          {practica.documentosCompletos}/{practica.documentosTotal}
        </p>
        <p>
          <span className="font-medium">Convenio firmado:</span>{' '}
          {practica.convenioFirmadoEstudiante ? 'Sí' : 'Pendiente'}
        </p>
        <p>
          <span className="font-medium">Encuesta:</span> {encuestaLabel}
        </p>
        <p>
          <span className="font-medium">Nota final:</span>{' '}
          {practica.notaFinal != null ? practica.notaFinal.toFixed(1) : 'Pendiente'}
        </p>
      </div>

      <div className="flex flex-wrap gap-2 pt-1">
        {(practica.asignacionId || practica.practicaId) && (
          <Link
            to={
              practica.asignacionId
                ? `/vinculacion/${practica.asignacionId}`
                : `/vinculacion/practica/${practica.practicaId}`
            }
            className="inline-flex items-center gap-1.5 rounded-lg bg-blue-600 px-3 py-1.5 text-xs font-semibold text-white hover:bg-blue-700"
          >
            <FileText className="h-3.5 w-3.5" aria-hidden="true" />
            Documentos
          </Link>
        )}
        {practica.practicaId && (
          <>
            <Link
              to={`/seguimiento/${practica.practicaId}`}
              className="inline-flex items-center gap-1.5 rounded-lg border border-gray-300 px-3 py-1.5 text-xs font-semibold text-gray-700 hover:bg-gray-50"
            >
              <Activity className="h-3.5 w-3.5" aria-hidden="true" />
              Seguimiento
            </Link>
            <Link
              to={`/evaluaciones/${practica.practicaId}`}
              className="inline-flex items-center gap-1.5 rounded-lg border border-gray-300 px-3 py-1.5 text-xs font-semibold text-gray-700 hover:bg-gray-50"
            >
              <Star className="h-3.5 w-3.5" aria-hidden="true" />
              Evaluaciones
            </Link>
          </>
        )}
      </div>
    </Card>
  );
}

export default function EstudiantePanelPage() {
  const { data, isLoading, isError, refetch } = usePanelEstudiante();

  if (isLoading) {
    return <LoadingState mensaje="Cargando tu panel..." />;
  }

  if (isError) {
    return (
      <ErrorState
        mensaje="No se pudo cargar tu información. Si acabas de actualizar la app, recarga con Ctrl+F5 o verifica que el backend esté en marcha."
        onReintentar={refetch}
      />
    );
  }

  const practicas = data?.practicas ?? [];

  return (
    <div className="space-y-6">
      <PageHeader
        titulo="Panel de inicio"
        descripcion="Resumen de tus prácticas y pendientes"
      />

      <Card padding="p-5">
        <h2 className="text-sm font-semibold uppercase tracking-wide text-gray-400">Mis datos</h2>
        <p className="mt-2 text-lg font-bold text-gray-900">{data?.nombre}</p>
        <div className="mt-1 flex flex-wrap gap-x-4 gap-y-1 text-sm text-gray-600">
          {data?.identificacion && <span>ID: {data.identificacion}</span>}
          {data?.programa && <span>Programa: {data.programa}</span>}
          {data?.correo && <span>{data.correo}</span>}
        </div>
      </Card>

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
        <Card padding="p-4" className="flex items-center gap-3">
          <CheckCircle2 className="h-8 w-8 text-emerald-600" aria-hidden="true" />
          <div>
            <p className="text-2xl font-bold text-gray-900">{practicas.length}</p>
            <p className="text-xs text-gray-500">Prácticas registradas</p>
          </div>
        </Card>
        <Card padding="p-4" className="flex items-center gap-3">
          <Clock className="h-8 w-8 text-amber-600" aria-hidden="true" />
          <div>
            <p className="text-2xl font-bold text-gray-900">
              {practicas.filter((p) => p.estadoPractica === 'EN_CURSO').length}
            </p>
            <p className="text-xs text-gray-500">En curso</p>
          </div>
        </Card>
        <Card padding="p-4" className="flex items-center gap-3">
          <AlertCircle className="h-8 w-8 text-blue-600" aria-hidden="true" />
          <div>
            <p className="text-2xl font-bold text-gray-900">
              {practicas.filter((p) => !p.convenioFirmadoEstudiante).length}
            </p>
            <p className="text-xs text-gray-500">Firmas pendientes</p>
          </div>
        </Card>
      </div>

      <section className="space-y-3">
        <h2 className="text-lg font-semibold text-gray-900">Mis prácticas</h2>
        {practicas.length === 0 ? (
          <div className="rounded-lg border border-dashed border-gray-300 py-10 text-center text-sm text-gray-500">
            Aún no tienes prácticas asignadas. El coordinador académico las registrará cuando
            correspondan.
          </div>
        ) : (
          practicas.map((p) => (
            <PracticaResumenCard key={p.asignacionId ?? p.practicaId} practica={p} />
          ))
        )}
      </section>

      <CentroAlertas />
    </div>
  );
}
