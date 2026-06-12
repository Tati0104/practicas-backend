import { normalizarGruposChecklist } from '../hooks/useCierre';
import ChecklistGrupo from './ChecklistGrupo';

export default function ChecklistCierre({
  checklist,
  puedeEnviarRecordatorio = false,
  recordatorioPendiente = false,
  onRecordatorio,
}) {
  const grupos = normalizarGruposChecklist(checklist);

  if (!grupos.length) {
    return (
      <div className="rounded-xl border border-dashed border-gray-300 bg-gray-50 p-8 text-center text-sm text-gray-600">
        No hay requisitos de cierre configurados para esta práctica.
      </div>
    );
  }

  return (
    <div className="space-y-4">
      {grupos.map((grupo) => (
        <ChecklistGrupo
          key={grupo.nombre}
          grupo={grupo}
          puedeEnviarRecordatorio={puedeEnviarRecordatorio}
          recordatorioPendiente={recordatorioPendiente}
          onRecordatorio={onRecordatorio}
        />
      ))}
    </div>
  );
}
