// src/modules/vacantes/components/VacanteForm.jsx

/**
 * Formulario (modal) para crear o editar una vacante.
 * - Usa React Hook Form + Zod para validación.
 * - Campos obligatorios con etiquetas visibles y mensajes de error.
 * - Al cerrar el modal se llama reset() para limpiar.
 * - Llama a create o edit mutation a través de useVacantesMutaciones.
 */
import { useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { zodResolver } from '@hookform/resolvers/zod';
import { toast } from 'react-hot-toast';
import { useVacantesMutaciones } from '../hooks/useVacantesMutaciones';

// Esquema Zod con los campos requeridos para una vacante.
const vacanteSchema = z.object({
  id: z.number().optional(), // solo para edición
  empresa: z.string().min(1, 'Empresa es requerida'),
  cargo: z.string().min(1, 'Cargo es requerido'),
  modalidad: z.enum(['PRESENCIAL', 'REMOTO', 'HÍBRIDO']),
  cuposTotal: z.number().int().positive('Cupos totales debe ser positivo'),
  cuposDisponibles: z.number().int().nonnegative('Cupos disponibles debe ser >= 0'),
  estado: z.enum([
    'ACTIVA',
    'PENDIENTE_APROBACION',
    'PAUSADA',
    'CUPOS_COMPLETOS',
    'CERRADA'
  ])
});

export default function VacanteForm({ isOpen, onClose, vacante }) {
  const isEdit = !!vacante?.id;

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors }
  } = useForm({
    resolver: zodResolver(vacanteSchema),
    defaultValues: vacante || {}
  });

  const { crear, editar } = useVacantesMutaciones({
    onSuccess: () => {
      toast.success(isEdit ? 'Vacante actualizada' : 'Vacante creada');
      onClose();
    },
    onError: err => toast.error(err?.message || 'Error al guardar')
  });

  // Cuando el modal se abre con una vacante para editar, cargamos los valores.
  useEffect(() => {
    if (isOpen) {
      reset(vacante || {});
    }
  }, [isOpen, vacante, reset]);

  const onSubmit = data => {
    if (isEdit) {
      editar.mutate({ id: vacante.id, dto: data });
    } else {
      crear.mutate(data);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 flex items-center justify-center bg-black/40 z-50">
      <div className="bg-white rounded-lg w-full max-w-lg p-6 shadow-lg">
        <h2 className="text-xl font-semibold mb-4">
          {isEdit ? 'Editar Vacante' : 'Crear Vacante'}
        </h2>
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          {/* Empresa */}
          <div>
            <label className="block text-sm font-medium mb-1">Empresa</label>
            <input
              {...register('empresa')}
              className="w-full border rounded px-3 py-2"
            />
            {errors.empresa && (
              <p className="text-red-600 text-sm mt-1">{errors.empresa.message}</p>
            )}
          </div>

          {/* Cargo */}
          <div>
            <label className="block text-sm font-medium mb-1">Cargo</label>
            <input
              {...register('cargo')}
              className="w-full border rounded px-3 py-2"
            />
            {errors.cargo && (
              <p className="text-red-600 text-sm mt-1">{errors.cargo.message}</p>
            )}
          </div>

          {/* Modalidad */}
          <div>
            <label className="block text-sm font-medium mb-1">Modalidad</label>
            <select {...register('modalidad')} className="w-full border rounded px-3 py-2">
              <option value="PRESENCIAL">Presencial</option>
              <option value="REMOTO">Remoto</option>
              <option value="HÍBRIDO">Híbrido</option>
            </select>
            {errors.modalidad && (
              <p className="text-red-600 text-sm mt-1">{errors.modalidad.message}</p>
            )}
          </div>

          {/* Cupos */}
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium mb-1">Cupos Totales</label>
              <input
                type="number"
                {...register('cuposTotal', { valueAsNumber: true })}
                className="w-full border rounded px-3 py-2"
              />
              {errors.cuposTotal && (
                <p className="text-red-600 text-sm mt-1">{errors.cuposTotal.message}</p>
              )}
            </div>
            <div>
              <label className="block text-sm font-medium mb-1">Cupos Disponibles</label>
              <input
                type="number"
                {...register('cuposDisponibles', { valueAsNumber: true })}
                className="w-full border rounded px-3 py-2"
              />
              {errors.cuposDisponibles && (
                <p className="text-red-600 text-sm mt-1">{errors.cuposDisponibles.message}</p>
              )}
            </div>
          </div>

          {/* Estado */}
          <div>
            <label className="block text-sm font-medium mb-1">Estado</label>
            <select {...register('estado')} className="w-full border rounded px-3 py-2">
              <option value="ACTIVA">Activa</option>
              <option value="PENDIENTE_APROBACION">Pendiente Aproba​ción</option>
              <option value="PAUSADA">Pausada</option>
              <option value="CUPOS_COMPLETOS">Cupos completos</option>
              <option value="CERRADA">Cerrada</option>
            </select>
            {errors.estado && (
              <p className="text-red-600 text-sm mt-1">{errors.estado.message}</p>
            )}
          </div>

          <div className="flex justify-end space-x-2 mt-4">
            <button
              type="button"
              onClick={() => { onClose(); reset(); }}
              className="px-4 py-2 bg-gray-200 text-gray-800 rounded"
            >
              Cancelar
            </button>
            <button
              type="submit"
              className="px-4 py-2 bg-blue-600 text-white rounded"
            >
              {isEdit ? 'Actualizar' : 'Crear'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
