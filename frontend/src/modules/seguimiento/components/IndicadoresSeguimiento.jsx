// src/modules/seguimiento/components/IndicadoresSeguimiento.jsx

import { AlertTriangle, CheckCircle2, Clock } from 'lucide-react';

const tarjetasConfig = [
  { key: 'AL_DIA', titulo: 'Al día', icon: CheckCircle2, bg: 'bg-emerald-50 border-emerald-200', text: 'text-emerald-700' },
  { key: 'PENDIENTE', titulo: 'Pendiente', icon: Clock, bg: 'bg-amber-50 border-amber-200', text: 'text-amber-700' },
  { key: 'EN_ALERTA', titulo: 'En alerta', icon: AlertTriangle, bg: 'bg-red-50 border-red-200', text: 'text-red-700' },
];

export default function IndicadoresSeguimiento({ practicas = [] }) {
  const conteos = {
    AL_DIA: practicas.filter((p) => p.estado === 'AL_DIA').length,
    PENDIENTE: practicas.filter((p) => p.estado === 'PENDIENTE').length,
    EN_ALERTA: practicas.filter((p) => p.estado === 'EN_ALERTA').length,
  };

  return (
    <div className="mb-5 grid grid-cols-1 gap-3 sm:grid-cols-3">
      {tarjetasConfig.map(({ key, titulo, icon: Icon, bg, text }) => (
        <div key={key} className={`rounded-xl border p-4 ${bg}`}>
          <Icon className={`mb-1 h-5 w-5 ${text}`} aria-hidden="true" />
          <div className={`text-2xl font-extrabold ${text}`}>{conteos[key]}</div>
          <div className="text-xs font-semibold text-gray-600">{titulo}</div>
        </div>
      ))}
    </div>
  );
}
