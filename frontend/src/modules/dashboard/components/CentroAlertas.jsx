import { Bell } from 'lucide-react';
import { useNotificaciones } from '@/shared/hooks/useNotificaciones';
import { Button, Card } from '@/shared/components/ui';

export default function CentroAlertas() {
  const { alertas, noLeidas, isLoading, isError, marcarLeida, isPendingMarcar } =
    useNotificaciones();

  if (isLoading) {
    return <p className="text-sm text-gray-500">Cargando alertas...</p>;
  }

  if (isError) {
    return <p className="text-sm text-gray-500">No se pudieron cargar las alertas.</p>;
  }

  return (
    <Card>
      <h3 className="mb-4 flex items-center gap-2 text-base font-semibold text-primary">
        <Bell className="h-4 w-4" aria-hidden="true" />
        Alertas y notificaciones
        {noLeidas > 0 && (
          <span className="rounded-full bg-red-500 px-2 py-0.5 text-xs font-bold text-white">
            {noLeidas}
          </span>
        )}
      </h3>

      {alertas.length === 0 ? (
        <p className="text-sm text-emerald-600">No tienes alertas pendientes</p>
      ) : (
        <div className="flex flex-col gap-2">
          {alertas.map((alerta) => (
            <div
              key={alerta.id}
              className={[
                'flex items-start justify-between gap-3 rounded-lg border p-3',
                alerta.leida ? 'border-gray-200 bg-gray-50' : 'border-amber-200 bg-amber-50',
              ].join(' ')}
            >
              <div className="min-w-0 flex-1">
                {alerta.tipo && (
                  <span className="text-xs font-semibold uppercase tracking-wide text-primary">
                    {alerta.tipo.replace(/_/g, ' ')}
                  </span>
                )}
                <p className="mt-0.5 text-sm text-gray-700">{alerta.mensaje}</p>
                <p className="mt-1 text-xs text-gray-400">{alerta.fecha}</p>
              </div>
              {!alerta.leida && (
                <Button
                  variant="info"
                  size="sm"
                  className="shrink-0"
                  onClick={() => marcarLeida(alerta.id)}
                  disabled={isPendingMarcar}
                >
                  Marcar leída
                </Button>
              )}
            </div>
          ))}
        </div>
      )}
    </Card>
  );
}
