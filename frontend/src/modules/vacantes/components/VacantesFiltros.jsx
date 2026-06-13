import { z } from 'zod';
import { FiltrosFormulario, FiltroInput, FiltroSelect } from '@/shared/components/filtros';

const filtroSchema = z.object({
  empresaId: z.string().optional(),
  programaId: z.string().optional(),
  estado: z.string().optional(),
  busqueda: z.string().optional(),
});

const ESTADOS = [
  { value: '', label: 'Todos los estados' },
  { value: 'ACTIVA', label: 'Activa' },
  { value: 'PENDIENTE_APROBACION', label: 'Pendiente' },
  { value: 'PAUSADA', label: 'Pausada' },
  { value: 'CUPOS_COMPLETOS', label: 'Cupos completos' },
  { value: 'CERRADA', label: 'Cerrada' },
];

export default function VacantesFiltros({ filtros, setFiltros }) {
  const valoresIniciales = {
    empresaId: filtros.empresaId || '',
    programaId: filtros.programaId || '',
    estado: filtros.estado || '',
    busqueda: filtros.busqueda || '',
  };

  return (
    <FiltrosFormulario
      schema={filtroSchema}
      valoresIniciales={valoresIniciales}
      variant="inline"
      onAplicar={(data) => setFiltros((prev) => ({ ...prev, ...data, page: 0 }))}
      onLimpiar={() =>
        setFiltros((prev) => ({
          ...prev,
          empresaId: '',
          programaId: '',
          estado: '',
          busqueda: '',
          page: 0,
        }))
      }
    >
      {({ register }) => (
        <>
          <FiltroInput {...register('empresaId')} placeholder="Empresa" style={{ flex: 'none', minWidth: 140 }} />
          <FiltroInput {...register('programaId')} placeholder="Programa" style={{ flex: 'none', minWidth: 140 }} />
          <FiltroSelect {...register('estado')} opciones={ESTADOS} />
          <FiltroInput {...register('busqueda')} placeholder="Buscar..." style={{ flex: 'none', minWidth: 160 }} />
        </>
      )}
    </FiltrosFormulario>
  );
}
