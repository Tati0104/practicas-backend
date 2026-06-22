import { Badge, Button, Card } from '@/shared/components/ui';
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

export default function PracticaCard({ practica, onVerDetalle, etiquetaAccion = 'Ver detalle', modoEstudiante = false }) {
  const estadoClave = estadoSeguimientoPractica(practica);
  const badge = BADGE[estadoClave] || { label: estadoClave, variant: 'neutral' };
  const fechaStr = formatearFechaSeguimiento(practica.fechaUltimaActividad);
  const codigo =
    typeof practica.estudiante === 'object' ? practica.estudiante?.codigo : null;
  const programa =
    typeof practica.estudiante === 'object' ? practica.estudiante?.programa : null;

  return (
    <Card padding="p-4">
      <div className="mb-2 flex items-start justify-between gap-2">
        <div>
          <div className="font-bold text-gray-900">
            {modoEstudiante
              ? `Práctica ${practica.numeroPractica ?? '—'}`
              : nombreEstudiantePractica(practica)}
          </div>
          {modoEstudiante && practica.estadoPractica && (
            <div className="text-xs text-gray-500">{practica.estadoPractica.replace(/_/g, ' ')}</div>
          )}
          {!modoEstudiante && (codigo || programa) && (
            <div className="text-xs text-gray-500">
              {[codigo, programa].filter(Boolean).join(' — ')}
            </div>
          )}
        </div>
        <Badge variant={badge.variant}>{badge.label}</Badge>
      </div>

      <div className="text-sm text-gray-700">
        <span className="font-semibold">{practica.cargo || '—'}</span>
        {practica.empresa ? ` · ${practica.empresa}` : ''}
      </div>
      <div className="mt-1 text-xs text-gray-500">Docente: {practica.docente || '—'}</div>

      <div className="mt-2 flex justify-between text-xs text-gray-500">
        <span>Corte: {practica.corte ?? '—'}</span>
        <span>Última actividad: {fechaStr}</span>
      </div>

      {practica.porcentajeAvance != null && (
        <>
          <div className="my-2 h-1.5 overflow-hidden rounded-full bg-gray-200">
            <div
              className="h-full rounded-full bg-primary"
              style={{ width: `${practica.porcentajeAvance || 0}%` }}
            />
          </div>
          <div className="mb-3 text-right text-xs text-gray-500">
            {practica.porcentajeAvance || 0}% completado
          </div>
        </>
      )}

      <Button size="sm" className="w-full" onClick={() => onVerDetalle(practica.id)}>
        {etiquetaAccion}
      </Button>
    </Card>
  );
}
