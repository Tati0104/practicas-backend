import { useMemo } from 'react';
import { FiltrosActivos } from '@/shared/components/filtros';

const ESTADOS_PRACTICA = [
  { value: 'EN_CURSO', label: 'En curso' },
  { value: 'COMPLETADA', label: 'Finalizada' },
  { value: 'REPROBADA', label: 'Reprobada' },
  { value: 'ASIGNADA_PENDIENTE_INICIO', label: 'Pendiente de inicio' },
];

export default function VinculacionFiltrosEstudiante({ filtros, setFiltros, practicasDisponibles = [] }) {
  const opcionesPractica = useMemo(
    () =>
      practicasDisponibles
        .filter((v) => v.numeroPractica != null)
        .map((v) => ({
          value: String(v.numeroPractica),
          label: `Práctica ${v.numeroPractica}${v.vacante?.cargo ? ` — ${v.vacante.cargo}` : ''}`,
        })),
    [practicasDisponibles]
  );

  const campos = useMemo(
    () => [
      {
        key: 'numeroPractica',
        label: 'Práctica',
        type: 'select',
        placeholder: 'Todas mis prácticas',
        opciones: opcionesPractica,
      },
      {
        key: 'estadoPractica',
        label: 'Estado',
        type: 'select',
        placeholder: 'Todos los estados',
        opciones: ESTADOS_PRACTICA,
      },
    ],
    [opcionesPractica]
  );

  return <FiltrosActivos campos={campos} filtros={filtros} onChange={setFiltros} />;
}
