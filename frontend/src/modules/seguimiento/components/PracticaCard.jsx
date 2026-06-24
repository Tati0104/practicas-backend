import { Badge, Button, Card } from '@/shared/components/ui';
import {
  estadoSeguimientoPractica,
  formatearFechaSeguimiento,
  nombreEstudiantePractica,
} from '../utils/fechas';
import { resolverIdPractica } from '../utils/practicaId';
import { badgeEstadoPractica } from '../utils/estadosPractica';

const BADGE = {
  AL_DIA: { label: 'Revisado / Al día', variant: 'success' },
  PENDIENTE: { label: 'Pendiente de revisión', variant: 'warning' },
  EN_ALERTA: { label: 'En alerta', variant: 'danger' },
};

export default function PracticaCard({
  practica,
  onVerDetalle,
  etiquetaAccion = 'Ver detalle',
  modoEstudiante = false,
  mostrarEstadoPractica = false,
}) {
  const estadoClave = estadoSeguimientoPractica(practica);
  const badge = BADGE[estadoClave] || { label: estadoClave, variant: 'neutral' };
  const estadoPracticaBadge = badgeEstadoPractica(practica.estadoPractica);
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
        <div className="flex flex-col items-end gap-1">
          {mostrarEstadoPractica && practica.estadoPractica && (
            <Badge variant={estadoPracticaBadge.variant}>{estadoPracticaBadge.label}</Badge>
          )}
          {!mostrarEstadoPractica && <Badge variant={badge.variant}>{badge.label}</Badge>}
          {mostrarEstadoPractica && (
            <Badge variant={badge.variant} className="text-[10px]">
              {badge.label}
            </Badge>
          )}
        </div>
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

      {mostrarEstadoPractica && practica.cantidadPracticas > 1 && (
        <div className="mt-1 text-xs text-gray-500">
          {practica.cantidadPracticas} prácticas registradas
        </div>
      )}

      <Button size="sm" className="w-full" onClick={() => onVerDetalle(resolverIdPractica(practica) ?? practica)}>
        {etiquetaAccion}
      </Button>
    </Card>
  );
}
