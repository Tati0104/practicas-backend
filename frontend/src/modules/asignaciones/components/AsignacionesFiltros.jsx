// src/modules/asignaciones/components/AsignacionesFiltros.jsx

/**
 * Barra de filtros del listado de asignaciones.
 * Permite filtrar por: programa, estado y búsqueda de texto.
 *
 * Usa React Hook Form + Zod para validación.
 * Llama a setFiltros del hook useAsignaciones cada vez que el usuario cambia un campo.
 */
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { zodResolver } from '@hookform/resolvers/zod';

// Esquema: todos los campos son opcionales (son filtros)
const schema = z.object({
  busqueda:  z.string().optional(),
  programaId: z.string().optional(),
  estado:    z.string().optional(),
});

const ESTADOS = [
  { value: '', label: 'Todos los estados' },
  { value: 'ASIGNADA',       label: 'Asignada' },
  { value: 'EN_VINCULACION', label: 'En Vinculación' },
  { value: 'VINCULADA',      label: 'Vinculada' },
  { value: 'CANCELADA',      label: 'Cancelada' },
];

export default function AsignacionesFiltros({ filtros, setFiltros }) {
  const { register, handleSubmit, reset } = useForm({
    resolver: zodResolver(schema),
    defaultValues: {
      busqueda:   filtros.busqueda   || '',
      programaId: filtros.programaId || '',
      estado:     filtros.estado     || '',
    },
  });

  // Al enviar el form actualizamos los filtros y volvemos a la página 0
  const onSubmit = (data) => {
    setFiltros((prev) => ({ ...prev, ...data, page: 0 }));
  };

  // Limpia todos los filtros y vuelve al estado inicial
  const limpiar = () => {
    reset({ busqueda: '', programaId: '', estado: '' });
    setFiltros((prev) => ({ ...prev, busqueda: '', programaId: '', estado: '', page: 0 }));
  };

  return (
    <form
      onSubmit={handleSubmit(onSubmit)}
      style={{
        display: 'flex',
        flexWrap: 'wrap',
        gap: 10,
        marginBottom: 16,
        padding: '12px 16px',
        background: '#f8fafc',
        borderRadius: 8,
        border: '1px solid #e5e7eb',
      }}
    >
      {/* Búsqueda libre */}
      <input
        {...register('busqueda')}
        placeholder="Buscar estudiante, empresa, cargo..."
        style={estilos.input}
      />

      {/* Filtro por ID de programa (en un proyecto real sería un select cargado desde API) */}
      <input
        {...register('programaId')}
        placeholder="ID Programa"
        style={{ ...estilos.input, maxWidth: 130 }}
      />

      {/* Filtro por estado */}
      <select {...register('estado')} style={estilos.select}>
        {ESTADOS.map((e) => (
          <option key={e.value} value={e.value}>{e.label}</option>
        ))}
      </select>

      <button type="submit" style={estilos.btnBuscar}>Buscar</button>
      <button type="button" onClick={limpiar} style={estilos.btnLimpiar}>Limpiar</button>
    </form>
  );
}

const estilos = {
  input: {
    flex: 1,
    minWidth: 180,
    padding: '7px 12px',
    fontSize: 13,
    border: '1px solid #d1d5db',
    borderRadius: 6,
    outline: 'none',
    fontFamily: 'Arial, sans-serif',
  },
  select: {
    padding: '7px 12px',
    fontSize: 13,
    border: '1px solid #d1d5db',
    borderRadius: 6,
    outline: 'none',
    fontFamily: 'Arial, sans-serif',
    background: '#ffffff',
    cursor: 'pointer',
  },
  btnBuscar: {
    padding: '7px 18px',
    background: '#2563eb',
    color: '#fff',
    border: 'none',
    borderRadius: 6,
    fontSize: 13,
    cursor: 'pointer',
    fontWeight: 600,
  },
  btnLimpiar: {
    padding: '7px 14px',
    background: '#f1f5f9',
    color: '#374151',
    border: '1px solid #d1d5db',
    borderRadius: 6,
    fontSize: 13,
    cursor: 'pointer',
  },
};
