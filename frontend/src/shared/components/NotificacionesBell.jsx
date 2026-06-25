import { useState, useRef, useEffect } from 'react';
import { Bell } from 'lucide-react';
import { useNotificaciones } from '@/shared/hooks/useNotificaciones';

export default function NotificacionesBell() {
  const [abierto, setAbierto] = useState(false);
  const ref = useRef(null);
  const {
    alertas,
    noLeidas,
    isLoading,
    soloNoLeidas,
    setSoloNoLeidas,
    marcarLeida,
    isPendingMarcar,
  } = useNotificaciones();

  useEffect(() => {
    const handler = (e) => {
      if (ref.current && !ref.current.contains(e.target)) setAbierto(false);
    };
    document.addEventListener('mousedown', handler);
    return () => document.removeEventListener('mousedown', handler);
  }, []);

  return (
    <div ref={ref} className="relative">
      <button
        type="button"
        onClick={() => setAbierto((v) => !v)}
        className="relative rounded-lg p-2 text-gray-500 transition-colors hover:bg-gray-100 hover:text-primary dark:text-slate-400 dark:hover:bg-white/[0.06] dark:hover:text-primary-glow"
        title="Notificaciones"
        aria-label={`Notificaciones${noLeidas > 0 ? `, ${noLeidas} sin leer` : ''}`}
      >
        <Bell className="h-5 w-5" aria-hidden="true" />
        {noLeidas > 0 && (
          <span className="absolute right-1 top-1 flex h-4 min-w-4 items-center justify-center rounded-full bg-red-500 px-1 text-[9px] font-bold text-white">
            {noLeidas > 99 ? '99+' : noLeidas}
          </span>
        )}
      </button>

      {abierto && (
        <div className="absolute right-0 top-[calc(100%+8px)] z-50 w-80 overflow-hidden rounded-2xl border ui-border bg-white shadow-xl dark:bg-dark-card dark:shadow-card">
          <div className="flex items-center justify-between border-b ui-border px-4 py-3">
            <span className="flex items-center gap-2 text-sm font-bold ui-text-title">
              Notificaciones
              {noLeidas > 0 && (
                <span className="rounded-full bg-red-500 px-1.5 py-0.5 text-[10px] font-bold text-white">
                  {noLeidas}
                </span>
              )}
            </span>
            <button
              type="button"
              onClick={() => setSoloNoLeidas(!soloNoLeidas)}
              className="text-xs font-semibold text-primary hover:underline"
            >
              {soloNoLeidas ? 'Ver todas' : 'Solo no leídas'}
            </button>
          </div>

          <div className="max-h-80 overflow-y-auto p-2">
            {isLoading && <p className="py-6 text-center text-sm text-gray-400 dark:text-slate-500">Cargando...</p>}
            {!isLoading && alertas.length === 0 && (
              <p className="py-6 text-center text-sm text-emerald-600">
                Sin alertas {soloNoLeidas ? 'pendientes' : ''}
              </p>
            )}
            {alertas.map((a) => (
              <div
                key={a.id}
                className={[
                  'mb-1 rounded-lg border p-3',
                  a.leida ? 'border-gray-100 bg-gray-50 dark:border-white/[0.04] dark:bg-dark-elevated' : 'border-amber-200 bg-amber-50 dark:border-amber-700/40 dark:bg-amber-950/30',
                  a.prioritaria && !a.leida ? 'ring-1 ring-red-200' : '',
                ].join(' ')}
              >
                {a.tipo && (
                  <span className="mb-1 block text-[10px] font-bold uppercase tracking-wide text-primary">
                    {a.tipo.replace(/_/g, ' ')}
                  </span>
                )}
                <p className="text-sm text-gray-800 dark:text-slate-200">{a.mensaje}</p>
                <div className="mt-2 flex items-center justify-between gap-2">
                  <span className="text-[11px] text-gray-400 dark:text-slate-500">{a.fecha}</span>
                  {!a.leida && (
                    <button
                      type="button"
                      onClick={() => marcarLeida(a.id)}
                      disabled={isPendingMarcar}
                      className="text-[11px] font-semibold text-primary hover:underline disabled:opacity-50"
                    >
                      Marcar leída
                    </button>
                  )}
                </div>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
