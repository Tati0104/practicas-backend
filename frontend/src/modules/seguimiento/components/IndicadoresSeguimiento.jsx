import { AlertTriangle, CheckCircle2, Clock } from 'lucide-react';
import { AnilloProgreso } from '@/shared/components/indicadores';

const tarjetasConfig = [
  {
    key: 'AL_DIA',
    titulo: 'Al día',
    icon: CheckCircle2,
    color: 'emerald',
    bg: 'bg-emerald-50/60 border-emerald-100',
    text: 'text-emerald-700',
  },
  {
    key: 'PENDIENTE',
    titulo: 'Pendiente',
    icon: Clock,
    color: 'amber',
    bg: 'bg-amber-50/60 border-amber-100',
    text: 'text-amber-700',
  },
  {
    key: 'EN_ALERTA',
    titulo: 'En alerta',
    icon: AlertTriangle,
    color: 'red',
    bg: 'bg-red-50/60 border-red-100',
    text: 'text-red-700',
  },
];

export default function IndicadoresSeguimiento({ practicas = [] }) {
  const conteos = {
    AL_DIA: practicas.filter((p) => p.estado === 'AL_DIA').length,
    PENDIENTE: practicas.filter((p) => p.estado === 'PENDIENTE').length,
    EN_ALERTA: practicas.filter((p) => p.estado === 'EN_ALERTA').length,
  };

  const total = practicas.length || 1;

  return (
    <div className="mb-5 grid grid-cols-1 gap-3 sm:grid-cols-3">
      {tarjetasConfig.map(({ key, titulo, icon: Icon, color, bg, text }) => (
        <div
          key={key}
          className={`flex items-center gap-4 rounded-xl border p-4 transition-shadow hover:shadow-sm ${bg}`}
        >
          <AnilloProgreso valor={conteos[key]} total={total} color={color} size={68} />
          <div className="min-w-0 flex-1">
            <div className={`text-2xl font-extrabold tabular-nums ${text}`}>{conteos[key]}</div>
            <div className="text-xs font-semibold text-gray-600">{titulo}</div>
          </div>
          <Icon className={`h-5 w-5 shrink-0 opacity-40 ${text}`} aria-hidden="true" />
        </div>
      ))}
    </div>
  );
}
