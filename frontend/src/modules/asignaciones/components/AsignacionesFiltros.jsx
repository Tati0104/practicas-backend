import { z } from 'zod';
import { FiltrosFormulario, FiltroInput, FiltroSelect } from '@/shared/components/filtros';

const schema = z.object({
  busqueda: z.string().optional(),
  programaId: z.string().optional(),
  estado: z.string().optional(),
});

const ESTADOS = [
  { value: '', label: 'Todos los estados' },
  { value: 'ASIGNADA', label: 'Asignada' },
  { value: 'EN_VINCULACION', label: 'En Vinculación' },
  { value: 'VINCULADA', label: 'Vinculada' },
  { value: 'CANCELADA', label: 'Cancelada' },
];

export default function AsignacionesFiltros({ filtros, setFiltros }) {
  const valoresIniciales = {
    busqueda: filtros.busqueda || '',
    programaId: filtros.programaId || '',
    estado: filtros.estado || '',
  };

  return (
    <FiltrosFormulario
      schema={schema}
      valoresIniciales={valoresIniciales}
      onAplicar={(data) => setFiltros((prev) => ({ ...prev, ...data, page: 0 }))}
      onLimpiar={() =>
        setFiltros((prev) => ({
          ...prev,
          busqueda: '',
          programaId: '',
          estado: '',
          page: 0,
        }))
      }
    >
      {({ register }) => (
        <>
          <FiltroInput
            {...register('busqueda')}
            placeholder="Buscar estudiante, empresa, cargo..."
          />
          <FiltroInput
            {...register('programaId')}
            placeholder="ID Programa"
            className="max-w-[130px] flex-none"
          />
          <FiltroSelect {...register('estado')} opciones={ESTADOS} />
        </>
      )}
    </FiltrosFormulario>
  );
}
