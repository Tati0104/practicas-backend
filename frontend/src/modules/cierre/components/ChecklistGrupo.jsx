import { CheckCircle2, Clock3 } from 'lucide-react';
import ChecklistItem from './ChecklistItem';

const ESTADO_GRUPO = {
  COMPLETADO: { icon: CheckCircle2, className: 'text-green-600' },
  PENDIENTE: { icon: Clock3, className: 'text-amber-600' },
  EN_BORRADOR: { icon: Clock3, className: 'text-blue-600' },
};

export default function ChecklistGrupo({
  grupo,
  puedeEnviarRecordatorio = false,
  recordatorioPendiente = false,
  onRecordatorio,
}) {
  const estadoConfig = ESTADO_GRUPO[grupo.estado] ?? ESTADO_GRUPO.PENDIENTE;
  const IconoEstado = estadoConfig.icon;
  const porcentaje = Math.round((grupo.progreso ?? 0) * 100);

  return (
    <section className="overflow-hidden rounded-xl border border-gray-200 bg-gray-50">
      <header className="flex flex-col gap-2 border-b border-gray-200 bg-white px-4 py-3 sm:flex-row sm:items-center sm:justify-between sm:px-5">
        <div className="flex items-center gap-2">
          <IconoEstado className={`h-5 w-5 ${estadoConfig.className}`} aria-hidden="true" />
          <h3 className="text-base font-semibold text-gray-900">{grupo.nombre}</h3>
        </div>
        <div className="flex items-center gap-3 text-sm text-gray-500">
          <span>{porcentaje}% completado</span>
          {grupo.verificado && (
            <span className="rounded-full bg-green-100 px-2 py-0.5 text-xs font-semibold text-green-700">
              Verificado
            </span>
          )}
        </div>
      </header>

      <ul className="space-y-3 p-4 sm:p-5" role="list">
        {(grupo.items ?? []).map((item, index) => (
          <ChecklistItem
            key={`${grupo.nombre}-${item.nombre}-${index}`}
            item={item}
            puedeEnviarRecordatorio={puedeEnviarRecordatorio}
            recordatorioPendiente={recordatorioPendiente}
            onRecordatorio={onRecordatorio}
          />
        ))}
      </ul>
    </section>
  );
}
