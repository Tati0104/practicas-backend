import { FiltrosBar, FiltroSelect } from '@/shared/components/filtros';

const ROLES = [
  { value: '', label: 'Todos los roles' },
  { value: 'ADMIN', label: 'ADMIN' },
  { value: 'DIRECCION', label: 'DIRECCION' },
  { value: 'COORD_ACADEMICA', label: 'COORD_ACADEMICA' },
  { value: 'COORD_PRACTICA', label: 'COORD_PRACTICA' },
  { value: 'SECRETARIA', label: 'SECRETARIA' },
  { value: 'DOCENTE_ASESOR', label: 'DOCENTE_ASESOR' },
  { value: 'EMPRESA', label: 'EMPRESA' },
  { value: 'TUTOR_EMPRESARIAL', label: 'TUTOR_EMPRESARIAL' },
  { value: 'ESTUDIANTE', label: 'ESTUDIANTE' },
];

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
        opciones={ROLES}
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
