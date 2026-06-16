import { Link2, Ban, ClipboardList } from 'lucide-react';
import { AnilloProgreso } from '@/shared/components/indicadores';

const tarjetasConfig = [
  {
    key: 'activas',
    titulo: 'Asignaciones activas',
    icon: ClipboardList,
    color: 'primary',
    bg: 'bg-blue-50/60 border-blue-100',
    text: 'text-blue-700',
  },
  {
    key: 'vinculacion',
    titulo: 'En vinculación',
    icon: Link2,
    color: 'amber',
    bg: 'bg-amber-50/60 border-amber-100',
    text: 'text-amber-700',
  },
  {
    key: 'canceladas',
    titulo: 'Canceladas este mes',
    icon: Ban,
    color: 'red',
    bg: 'bg-red-50/60 border-red-100',
    text: 'text-red-700',
  },
];

export default function IndicadoresAsignaciones({ asignaciones = [] }) {
  const ahora = new Date();

  const conteos = {
    activas: asignaciones.filter(
      (a) => a.estado === 'ASIGNADA' || a.estado === 'EN_PROCESO_VINCULACION'
    ).length,
    vinculacion: asignaciones.filter((a) => a.estado === 'EN_PROCESO_VINCULACION').length,
    canceladas: asignaciones.filter((a) => {
      if (a.estado !== 'CANCELADA' || !a.fechaAsignacion) return false;
      const fecha = new Date(a.fechaAsignacion);
      return fecha.getMonth() === ahora.getMonth() && fecha.getFullYear() === ahora.getFullYear();
    }).length,
  };

  const total = asignaciones.length || 1;

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
