// src/modules/vacantes/components/VacantesFiltros.jsx
/**
 * Componente de filtros para el módulo Vacantes.
 * Utiliza React Hook Form + Zod para la validación.
 * Exporta los props `filtros` (objeto) y `setFiltros` (setter) recibidos del padre.
 */
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { zodResolver } from '@hookform/resolvers/zod';
import { useEffect } from 'react';

// Esquema de validación simple – todos los campos son opcionales.
const filtroSchema = z.object({
  empresaId: z.string().optional().or(z.number().optional()),
  programaId: z.string().optional().or(z.number().optional()),
  estado:    z.string().optional(),
  busqueda:  z.string().optional()
});

export default function VacantesFiltros({ filtros, setFiltros }) {
  const { register, handleSubmit, reset, watch } = useForm({
    resolver: zodResolver(filtroSchema),
    defaultValues: filtros
  });

  // Sincroniza los valores del formulario cuando `filtros` cambia externamente.
  useEffect(() => {
    reset(filtros);
  }, [filtros, reset]);

  const onSubmit = data => {
    // Actualiza el estado de filtros del padre; la query se refetchará automáticamente.
    setFiltros(prev => ({ ...prev, ...data }));
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="flex flex-wrap gap-2 mb-4">
      {/* Campo Empresa */}
      <input
        type="text"
        placeholder="Empresa"
        {...register('empresaId')}
        className="border rounded px-2 py-1"
      />
      {/* Campo Programa */}
      <input
        type="text"
        placeholder="Programa"
        {...register('programaId')}
        className="border rounded px-2 py-1"
      />
      {/* Campo Estado */}
      <select {...register('estado')} className="border rounded px-2 py-1">
        <option value="">Todos los estados</option>
        <option value="ACTIVA">Activa</option>
        <option value="PENDIENTE_APROBACION">Pendiente</option>
        <option value="PAUSADA">Pausada</option>
        <option value="CUPOS_COMPLETOS">Cupos completos</option>
        <option value="CERRADA">Cerrada</option>
      </select>
      {/* Campo Búsqueda */}
      <input
        type="text"
        placeholder="Buscar..."
        {...register('busqueda')}
        className="border rounded px-2 py-1"
      />
      <button type="submit" className="bg-indigo-600 text-white rounded px-4 py-1">
        Aplicar
      </button>
    </form>
  );
}
