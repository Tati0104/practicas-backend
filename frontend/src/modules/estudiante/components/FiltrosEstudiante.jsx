import { FiltrosBar, FiltroInput, FiltroSelect } from '@/shared/components/filtros';

const APTITUDES = [
  { value: '', label: 'Todos los estados' },
  { value: 'SIN_EVALUAR', label: 'SIN_EVALUAR' },
  { value: 'APTO', label: 'APTO' },
  { value: 'NO_APTO', label: 'NO_APTO' },
];

const ACTIVO = [
  { value: '', label: 'Activo / Inactivo' },
  { value: 'true', label: 'Activos' },
  { value: 'false', label: 'Inactivos' },
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
        value={filtros.estadoAptitud || ''}
        onChange={(e) => actualizar({ estadoAptitud: e.target.value || undefined })}
      />
      <FiltroSelect
        compacto
        opciones={ACTIVO}
        value={filtros.activo ?? ''}
        onChange={(e) =>
          actualizar({
            activo: e.target.value === '' ? undefined : e.target.value === 'true',
          })
        }
      />
    </FiltrosBar>
  );
}
