import { Bell } from 'lucide-react';
import { useAlertas } from '../hooks/useDashboard';
import { Button, Card } from '@/shared/components/ui';

export default function CentroAlertas() {
  const { alertas, marcarLeida } = useAlertas();

  if (alertas.isLoading) {
    return <p className="text-sm text-gray-500">Cargando alertas...</p>;
  }

  if (alertas.isError) {
    return <p className="text-sm text-gray-500">No se pudieron cargar las alertas.</p>;
  }

  const lista = alertas.data?.content || [];

  return (
    <Card>
      <h3 className="mb-4 flex items-center gap-2 text-base font-semibold text-primary">
        <Bell className="h-4 w-4" aria-hidden="true" />
        Alertas y notificaciones
      </h3>

      {lista.length === 0 ? (
        <p className="text-sm text-emerald-600">No tienes alertas pendientes</p>
      ) : (
        <div className="flex flex-col gap-2">
          {lista.map((alerta) => (
            <div
              key={alerta.id}
              className="flex items-start justify-between gap-3 rounded-lg border border-gray-200 bg-slate-50 p-3"
            >
              <div className="min-w-0 flex-1">
                <span className="text-xs font-semibold uppercase tracking-wide text-blue-700">
                  {alerta.tipo}
                </span>
                <p className="mt-0.5 text-sm text-gray-700">{alerta.mensaje}</p>
              </div>
              {!alerta.leida && (
                <Button
                  variant="info"
                  size="sm"
                  className="shrink-0"
                  onClick={() => marcarLeida.mutate(alerta.id)}
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
