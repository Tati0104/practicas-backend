import { Bell, CheckCircle2, CircleDashed } from 'lucide-react';
import { itemPendienteRecordatorio } from '../hooks/useCierre';

const ESTADO_ESTILOS = {
  COMPLETADO: { label: 'Completado', className: 'bg-green-100 text-green-800' },
  COMPLETADA: { label: 'Completada', className: 'bg-green-100 text-green-800' },
  PENDIENTE: { label: 'Pendiente', className: 'bg-amber-100 text-amber-800' },
  EN_BORRADOR: { label: 'En borrador', className: 'bg-blue-100 text-blue-800' },
};

function formatearFecha(fecha) {
  if (!fecha) return null;
  const parsed = new Date(fecha);
  if (Number.isNaN(parsed.getTime())) return null;
  return parsed.toLocaleString('es-CO', {
    day: 'numeric',
    month: 'short',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
}

export default function ChecklistItem({
  item,
  puedeEnviarRecordatorio = false,
  recordatorioPendiente = false,
  onRecordatorio,
}) {
  const estadoConfig = ESTADO_ESTILOS[item.estado] ?? {
    label: item.estado ?? 'Desconocido',
    className: 'bg-gray-100 text-gray-700',
  };

  const mostrarRecordatorio =
    puedeEnviarRecordatorio &&
    item.tipoEncuesta &&
    itemPendienteRecordatorio(item);

  const fechaRecordatorio = formatearFecha(item.fechaUltimoRecordatorio);

  return (
    <li className="rounded-lg border border-gray-200 bg-white p-4 shadow-sm">
      <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
        <div className="min-w-0 flex-1">
          <div className="flex items-start gap-2">
            {itemPendienteRecordatorio(item) ? (
              <CircleDashed className="mt-0.5 h-4 w-4 shrink-0 text-amber-500" aria-hidden="true" />
            ) : (
              <CheckCircle2 className="mt-0.5 h-4 w-4 shrink-0 text-green-600" aria-hidden="true" />
            )}
            <div className="min-w-0">
              <p className="text-sm font-medium text-gray-900">
                {item.nombre}
                {item.obligatorio && (
                  <span className="ml-1 text-red-600" title="Requisito obligatorio" aria-label="Obligatorio">
                    *
                  </span>
                )}
              </p>
              {item.estadoEncuesta && (
                <p className="mt-1 text-xs text-gray-500">
                  Encuesta: {item.estadoEncuesta.replace(/_/g, ' ').toLowerCase()}
                </p>
              )}
              {fechaRecordatorio && (
                <p className="mt-1 text-xs text-gray-400">
                  Último recordatorio: {fechaRecordatorio}
                </p>
              )}
            </div>
          </div>
        </div>

        <div className="flex flex-wrap items-center gap-2 sm:justify-end">
          <span
            className={`inline-flex rounded-full px-2.5 py-1 text-xs font-semibold ${estadoConfig.className}`}
          >
            {estadoConfig.label}
          </span>

          {mostrarRecordatorio && (
            <button
              type="button"
              onClick={() => onRecordatorio?.(item.tipoEncuesta)}
              disabled={recordatorioPendiente}
              className="inline-flex items-center gap-1.5 rounded-lg border border-blue-200 bg-blue-50 px-3 py-1.5 text-xs font-semibold text-blue-700 hover:bg-blue-100 disabled:cursor-not-allowed disabled:opacity-60"
              aria-label={`Enviar recordatorio para ${item.nombre}`}
            >
              <Bell className="h-3.5 w-3.5" aria-hidden="true" />
              {recordatorioPendiente ? 'Enviando...' : 'Recordatorio'}
            </button>
          )}
        </div>
      </div>
    </li>
  );
}
