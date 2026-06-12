const ESTADOS = {
  PENDIENTE: {
    label: 'Pendiente',
    className: 'bg-amber-100 text-amber-800',
  },
  EN_BORRADOR: {
    label: 'En borrador',
    className: 'bg-blue-100 text-blue-800',
  },
  COMPLETADA: {
    label: 'Completada',
    className: 'bg-green-100 text-green-800',
  },
};

export default function EstadoEncuestaBadge({ estado }) {
  const config = ESTADOS[estado] ?? {
    label: estado ?? 'Desconocido',
    className: 'bg-gray-100 text-gray-700',
  };

  return (
    <span
      className={`inline-flex rounded-full px-2.5 py-0.5 text-xs font-semibold ${config.className}`}
    >
      {config.label}
    </span>
  );
}
