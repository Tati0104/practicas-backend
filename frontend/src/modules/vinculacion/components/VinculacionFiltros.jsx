// src/modules/vinculacion/components/VinculacionFiltros.jsx

/**
 * Componente: VinculacionFiltros
 * ───────────────────────────────
 * Barra de filtros de la página principal de Vinculación.
 *
 * ¿Qué filtra?
 *   - Estado general de la vinculación: PENDIENTE / EN_PROCESO / COMPLETADA
 *   - Programa académico (por ID, en un proyecto real sería un select cargado desde API)
 *   - Búsqueda de texto libre (nombre del estudiante, empresa, cargo)
 *
 * Patrón de uso:
 *   El componente recibe los filtros actuales y la función setFiltros del hook
 *   useVinculacion. Al enviar el form, actualiza los filtros y resetea la página a 0.
 *   Al limpiar, restaura todos los campos a su valor vacío.
 *
 * Props:
 *   filtros     → object  — estado actual de filtros { busqueda, programaId, estado, page }
 *   setFiltros  → function — actualizador del estado de filtros
 *
 * Validación:
 *   Todos los campos son opcionales (son filtros), así que el schema de Zod
 *   solo define los tipos sin restricciones de requerido.
 */

import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { zodResolver } from '@hookform/resolvers/zod';

// Schema de validación: todos opcionales porque son filtros
const schema = z.object({
  busqueda:   z.string().optional(),
  programaId: z.string().optional(),
  estado:     z.string().optional(),
});

// Opciones del selector de estado de vinculación
const ESTADOS = [
  { value: '',            label: 'Todos los estados'  },
  { value: 'PENDIENTE',  label: 'Pendiente'           },
  { value: 'EN_PROCESO', label: 'En proceso'          },
  { value: 'COMPLETADA', label: 'Completada'          },
];

export default function VinculacionFiltros({ filtros, setFiltros }) {
  const { register, handleSubmit, reset } = useForm({
    resolver: zodResolver(schema),
    // Inicializa los campos con los valores actuales de los filtros
    defaultValues: {
      busqueda:   filtros.busqueda   || '',
      programaId: filtros.programaId || '',
      estado:     filtros.estado     || '',
    },
  });

  /**
   * Al enviar: actualiza los filtros del hook y vuelve a la página 0
   * para que la tabla muestre resultados desde el principio.
   */
  const onSubmit = (data) => {
    setFiltros((prev) => ({ ...prev, ...data, page: 0 }));
  };

  /**
   * Limpiar: resetea el formulario a valores vacíos
   * y restaura los filtros a su estado inicial.
   */
  const limpiar = () => {
    reset({ busqueda: '', programaId: '', estado: '' });
    setFiltros((prev) => ({
      ...prev,
      busqueda: '',
      programaId: '',
      estado: '',
      page: 0,
    }));
  };

  return (
    <form
      onSubmit={handleSubmit(onSubmit)}
      style={estilos.form}
    >
      {/* ── Búsqueda libre ── */}
      <input
        {...register('busqueda')}
        id="filtro-busqueda"
        placeholder="Buscar estudiante, empresa, cargo..."
        style={estilos.input}
        aria-label="Búsqueda de texto"
      />

      {/* ── Filtro de programa (ID numérico; en producción sería un select con datos de API) ── */}
      <input
        {...register('programaId')}
        id="filtro-programa"
        placeholder="ID Programa"
        style={{ ...estilos.input, maxWidth: 130 }}
        aria-label="Filtrar por programa"
      />

      {/* ── Filtro de estado ── */}
      <select
        {...register('estado')}
        id="filtro-estado"
        style={estilos.select}
        aria-label="Filtrar por estado"
      >
        {ESTADOS.map((e) => (
          <option key={e.value} value={e.value}>{e.label}</option>
        ))}
      </select>

      {/* ── Botones ── */}
      <button type="submit" style={estilos.btnBuscar}>
        Buscar
      </button>
      <button type="button" onClick={limpiar} style={estilos.btnLimpiar}>
        Limpiar
      </button>
    </form>
  );
}

// ── Estilos ───────────────────────────────────────────────────────────────────
const estilos = {
  form: {
    display: 'flex',
    flexWrap: 'wrap',      // En móvil los elementos se apilan
    gap: 10,
    marginBottom: 16,
    padding: '12px 16px',
    background: '#f8fafc',
    borderRadius: 8,
    border: '1px solid #e5e7eb',
  },
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
    background: '#fff',
    cursor: 'pointer',
    fontFamily: 'Arial, sans-serif',
  },
  btnBuscar: {
    padding: '7px 18px',
    background: '#2563eb',
    color: '#fff',
    border: 'none',
    borderRadius: 6,
    fontSize: 13,
    fontWeight: 600,
    cursor: 'pointer',
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
