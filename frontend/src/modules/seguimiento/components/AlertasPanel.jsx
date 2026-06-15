// src/modules/seguimiento/components/AlertasPanel.jsx

import { Bell } from 'lucide-react';
import { useAlertas } from '../hooks/useAlertas';
import { Badge, Button, Card } from '@/shared/components/ui';

export default function AlertasPanel({ practicaId = null }) {
  const {
    alertas,
    noLeidas,
    isLoading,
    soloNoLeidas,
    setSoloNoLeidas,
    marcarLeida,
    isPendingMarcar,
  } = useAlertas(practicaId);

  return (
    <Card padding="p-4" className="min-w-0">
      <div className="mb-3 flex items-center justify-between gap-2">
        <h3 className="flex items-center gap-2 text-sm font-bold text-gray-900">
          <Bell className="h-4 w-4" aria-hidden="true" />
          Alertas
          {noLeidas > 0 && <Badge variant="danger">{noLeidas}</Badge>}
        </h3>
        <button
          type="button"
          onClick={() => setSoloNoLeidas(!soloNoLeidas)}
          className={`text-xs font-semibold ${soloNoLeidas ? 'text-blue-600' : 'text-gray-500'} hover:underline`}
        >
          {soloNoLeidas ? 'Ver todas' : 'Solo no leídas'}
        </button>
      </div>

      {isLoading && <p className="text-sm text-gray-400">Cargando...</p>}

      {!isLoading && alertas.length === 0 && (
        <p className="py-6 text-center text-sm text-gray-400">
          Sin alertas {soloNoLeidas ? 'pendientes' : ''}
        </p>
      )}

      <div className="flex flex-col gap-2">
        {alertas.map((alerta) => (
          <div
            key={alerta.id}
            className={[
              'rounded-lg border p-3',
              alerta.leida ? 'border-gray-200 bg-gray-50' : 'border-amber-300 bg-amber-50',
            ].join(' ')}
          >
            <p className="text-sm text-gray-700">{alerta.mensaje}</p>
            <p className="mt-1 text-xs text-gray-400">{alerta.fecha}</p>
            {!alerta.leida && (
              <Button
                variant="ghost"
                size="sm"
                className="mt-2 self-end text-blue-600"
                onClick={() => marcarLeida(alerta.id)}
                disabled={isPendingMarcar}
              >
                Marcar como leída
              </Button>
            )}
          </div>
        ))}
      </div>
    </Card>
  );
}
