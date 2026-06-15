import { FiltrosBar, FiltroSelect } from '@/shared/components/filtros';
import { opcionesRol } from '../constants/catalogoUsuario';

const ACTIVO = [
  { value: '', label: 'Todos los estados' },
  { value: 'true', label: 'Activos' },
  { value: 'false', label: 'Inactivos' },
];

export default function FiltrosUsuario({ filtros, onChange }) {
  const actualizar = (cambios) => onChange({ ...filtros, ...cambios, page: 0 });

  return (
    <FiltrosBar variant="inline">
      <FiltroSelect
        compacto
        opciones={opcionesRol(true)}
        value={filtros.rol || ''}
        onChange={(e) => actualizar({ rol: e.target.value || undefined })}
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
