import { FiltrosBar, FiltroInput, FiltroSelect } from '@/shared/components/filtros';

const APTITUDES = [
  { value: '', label: 'Todos los estados' },
  { value: 'SIN_EVALUAR', label: 'SIN_EVALUAR' },
  { value: 'APTO', label: 'APTO' },
  { value: 'NO_APTO', label: 'NO_APTO' },
];


export default function FiltrosEstudiante({ filtros, onChange }) {
  const actualizar = (cambios) => onChange({ ...filtros, ...cambios, page: 0 });

  return (
    <FiltrosBar variant="inline">
      <FiltroInput
        compacto
        placeholder="Buscar por nombre o ID..."
        value={filtros.busqueda || ''}
        onChange={(e) => actualizar({ busqueda: e.target.value || undefined })}
      />
      <FiltroSelect
        compacto
        opciones={APTITUDES}
        value={filtros.aptitud || ''}
        onChange={(e) => actualizar({ aptitud: e.target.value || undefined })}
      />
    </FiltrosBar>
  );
}
