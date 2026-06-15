// src/modules/asignaciones/components/AsignacionForm.jsx

import { useEffect } from 'react';
import { useForm, useWatch } from 'react-hook-form';
import { z } from 'zod';
import { zodResolver } from '@hookform/resolvers/zod';
import { ClipboardList } from 'lucide-react';
import { useVacantesActivas } from '../hooks/useVacantesActivas';
import { useEstudiantesAptos } from '../hooks/useEstudiantesAptos';
import { Button, Modal, Select } from '@/shared/components/ui';

const schema = z.object({
  vacanteId: z.string().min(1, 'Selecciona una vacante'),
  estudianteId: z.string().min(1, 'Selecciona un estudiante'),
});

export default function AsignacionForm({ isOpen, onClose, onCrear, isPending }) {
  const {
    register,
    handleSubmit,
    reset,
    control,
    formState: { errors },
  } = useForm({ resolver: zodResolver(schema) });

  const vacanteId = useWatch({ control, name: 'vacanteId' });
  const estudianteId = useWatch({ control, name: 'estudianteId' });

  const { vacantes, isLoading: loadingVacantes } = useVacantesActivas();
  const { estudiantes, isLoading: loadingEstudiantes } = useEstudiantesAptos({ vacanteId });

  useEffect(() => {
    if (!isOpen) reset();
  }, [isOpen, reset]);

  const onSubmit = (data) => {
    onCrear({ vacanteId: Number(data.vacanteId), estudianteId: Number(data.estudianteId) });
  };

  if (!isOpen) return null;

  const vacanteSeleccionada = vacantes.find((v) => String(v.id) === String(vacanteId));
  const estudianteSeleccionado = estudiantes.find((e) => String(e.id) === String(estudianteId));

  return (
    <Modal
      titulo="Nueva asignación"
      onCerrar={() => {
        reset();
        onClose();
      }}
      ancho="max-w-lg"
      acciones={null}
    >
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
        <div>
          <label htmlFor="vacanteId" className="mb-1 block text-sm font-medium text-gray-700">
            Vacante activa <span className="text-red-600">*</span>
          </label>
          <Select id="vacanteId" {...register('vacanteId')} disabled={loadingVacantes}>
            <option value="">
              {loadingVacantes ? 'Cargando vacantes...' : '— Selecciona una vacante —'}
            </option>
            {vacantes.map((v) => (
              <option key={v.id} value={v.id}>
                {v.cargo} · {v.empresa} (cupos: {v.cuposDisponibles})
              </option>
            ))}
          </Select>
          {errors.vacanteId && (
            <p className="mt-1 text-sm text-red-600">{errors.vacanteId.message}</p>
          )}
        </div>

        <div>
          <label htmlFor="estudianteId" className="mb-1 block text-sm font-medium text-gray-700">
            Estudiante apto <span className="text-red-600">*</span>
          </label>
          <Select
            id="estudianteId"
            {...register('estudianteId')}
            disabled={!vacanteId || loadingEstudiantes}
          >
            <option value="">
              {!vacanteId
                ? '← Primero selecciona una vacante'
                : loadingEstudiantes
                  ? 'Cargando estudiantes...'
                  : estudiantes.length === 0
                    ? 'No hay estudiantes aptos'
                    : '— Selecciona un estudiante —'}
            </option>
            {estudiantes.map((e) => (
              <option key={e.id} value={e.id}>
                {e.nombre} · {e.codigo} · {e.programa}
              </option>
            ))}
          </Select>
          {errors.estudianteId && (
            <p className="mt-1 text-sm text-red-600">{errors.estudianteId.message}</p>
          )}
        </div>

        {vacanteSeleccionada && (
          <div className="rounded-lg border border-blue-200 bg-blue-50 p-3">
            <p className="mb-2 flex items-center gap-2 text-sm font-semibold text-blue-800">
              <ClipboardList className="h-4 w-4" aria-hidden="true" />
              Resumen de la asignación
            </p>
            <p className="text-sm text-gray-700">
              <strong>Cargo:</strong> {vacanteSeleccionada.cargo}
            </p>
            <p className="text-sm text-gray-700">
              <strong>Empresa:</strong> {vacanteSeleccionada.empresa}
            </p>
            {estudianteSeleccionado && (
              <p className="mt-1 text-sm text-gray-700">
                <strong>Estudiante:</strong> {estudianteSeleccionado.nombre}
              </p>
            )}
          </div>
        )}

        <div className="flex justify-end gap-2 pt-2">
          <Button
            type="button"
            variant="ghost"
            size="sm"
            disabled={isPending}
            onClick={() => {
              reset();
              onClose();
            }}
          >
            Cancelar
          </Button>
          <Button type="submit" size="sm" disabled={isPending} className="bg-blue-600 hover:bg-blue-700">
            {isPending ? 'Guardando...' : 'Crear asignación'}
          </Button>
        </div>
      </form>
    </Modal>
  );
}
