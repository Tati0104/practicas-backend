// src/modules/asignaciones/components/HistorialEstados.jsx

import BadgeAsignacion from './BadgeAsignacion';

export default function HistorialEstados({ historial = [] }) {
  if (historial.length === 0) {
    return <p className="py-3 text-sm text-gray-400">Sin historial de estados registrado.</p>;
  }

  return (
    <div className="flex flex-col">
      {historial.map((item, i) => (
        <div key={i} className="flex gap-3 pb-4">
          <div className="flex w-4 flex-col items-center">
            <div className="mt-1 h-3 w-3 shrink-0 rounded-full bg-blue-600" />
            {i < historial.length - 1 && <div className="mt-1 w-0.5 flex-1 bg-blue-100" />}
          </div>
          <div className="min-w-0 flex-1">
            <div className="mb-1 flex flex-wrap items-center gap-2">
              <BadgeAsignacion estado={item.estado} />
              <span className="text-xs text-gray-500">
                {item.fecha
                  ? new Date(item.fecha).toLocaleDateString('es-CO', {
                      day: '2-digit',
                      month: 'short',
                      year: 'numeric',
                    })
                  : '—'}
              </span>
            </div>
            {item.observacion && (
              <p className="text-sm italic text-gray-600">{item.observacion}</p>
            )}
          </div>
        </div>
      ))}
    </div>
  );
}
