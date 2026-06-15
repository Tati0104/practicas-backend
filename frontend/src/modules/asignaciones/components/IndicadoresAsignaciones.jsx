import { Link2, Ban, ClipboardList } from 'lucide-react';

const tarjetasConfig = [
  {
    key: 'activas',
    titulo: 'Asignaciones activas',
    icon: ClipboardList,
    bg: 'bg-blue-50 border-blue-200',
    text: 'text-blue-700',
  },
  {
    key: 'vinculacion',
    titulo: 'En vinculación',
    icon: Link2,
    bg: 'bg-amber-50 border-amber-200',
    text: 'text-amber-700',
  },
  {
    key: 'canceladas',
    titulo: 'Canceladas este mes',
    icon: Ban,
    bg: 'bg-red-50 border-red-200',
    text: 'text-red-700',
  },
];

export default function IndicadoresAsignaciones({ asignaciones = [] }) {
  const ahora = new Date();

  const conteos = {
    activas: asignaciones.filter(
      (a) => a.estado === 'ASIGNADA' || a.estado === 'EN_VINCULACION'
    ).length,
    vinculacion: asignaciones.filter((a) => a.estado === 'EN_VINCULACION').length,
    canceladas: asignaciones.filter((a) => {
      if (a.estado !== 'CANCELADA' || !a.fechaAsignacion) return false;
      const fecha = new Date(a.fechaAsignacion);
      return fecha.getMonth() === ahora.getMonth() && fecha.getFullYear() === ahora.getFullYear();
    }).length,
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
