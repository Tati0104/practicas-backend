import { BookOpen, Eye, TrendingUp } from 'lucide-react';
import { formatearFechaHoraSeguimiento } from '../utils/fechas';

const TIPO_CONFIG = {
  OBSERVACION: {
    icon: Eye,
    label: 'Observación',
    badgeClass: 'bg-blue-100 text-blue-800',
    iconClass: 'bg-blue-50 text-blue-600 border-blue-200',
  },
  AVANCE_TUTOR: {
    icon: TrendingUp,
    label: 'Avance tutor',
    badgeClass: 'bg-emerald-100 text-emerald-800',
    iconClass: 'bg-emerald-50 text-emerald-600 border-emerald-200',
  },
  BITACORA: {
    icon: BookOpen,
    label: 'Bitácora',
    badgeClass: 'bg-violet-100 text-violet-800',
    iconClass: 'bg-violet-50 text-violet-600 border-violet-200',
  },
};

const DEFAULT_CONFIG = {
  icon: BookOpen,
  label: 'Evento',
  badgeClass: 'bg-gray-100 text-gray-700',
  iconClass: 'bg-gray-50 text-gray-600 border-gray-200',
};

export default function TimelineSeguimiento({ timeline = [] }) {
  if (timeline.length === 0) {
    return (
      <p className="py-8 text-center text-sm text-gray-400">Sin eventos registrados aún.</p>
    );
  }

  return (
    <div className="flex flex-col">
      {timeline.map((evento, idx) => {
        const cfg = TIPO_CONFIG[evento.tipo] ?? { ...DEFAULT_CONFIG, label: evento.tipo };
        const Icon = cfg.icon;

        return (
          <div key={evento.id} className="flex gap-3.5">
            <div className="flex flex-col items-center">
              <div
                className={`flex h-9 w-9 shrink-0 items-center justify-center rounded-full border-2 ${cfg.iconClass}`}
              >
                <Icon className="h-4 w-4" aria-hidden="true" />
              </div>
              {idx < timeline.length - 1 && (
                <div className="my-1 w-0.5 flex-1 bg-gray-200" />
              )}
            </div>

            <div className="min-w-0 flex-1 pb-5">
              <div className="mb-1 flex flex-wrap items-center justify-between gap-2">
                <span
                  className={`inline-flex rounded-full px-2.5 py-0.5 text-xs font-semibold ${cfg.badgeClass}`}
                >
                  {cfg.label}
                </span>
                <span className="text-xs text-gray-400">
                  {formatearFechaHoraSeguimiento(evento.fecha)}
                </span>
              </div>
              <p className="text-sm text-gray-700">{evento.contenido}</p>
              {evento.porcentaje !== undefined && (
                <p className="mt-1 text-xs text-gray-500">Avance: {evento.porcentaje}%</p>
              )}
              <p className="mt-1 text-xs text-gray-400">Por: {evento.autor}</p>
            </div>
          </div>
        );
      })}
    </div>
  );
}
