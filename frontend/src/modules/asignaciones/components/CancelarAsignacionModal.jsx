// src/modules/asignaciones/components/CancelarAsignacionModal.jsx

import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { zodResolver } from '@hookform/resolvers/zod';
import { Button, Modal } from '@/shared/components/ui';

const schema = z.object({
  motivo: z
    .string()
    .min(10, 'El motivo debe tener al menos 10 caracteres')
    .max(500, 'Máximo 500 caracteres'),
});

export default function CancelarAsignacionModal({
  isOpen,
  asignacion,
  onClose,
  onConfirmar,
  isPending,
}) {
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm({ resolver: zodResolver(schema) });

  const onSubmit = ({ motivo }) => {
    onConfirmar(motivo);
    reset();
  };

  const handleClose = () => {
    reset();
    onClose();
  };

  if (!isOpen || !asignacion) return null;

  return (
    <Modal titulo="Cancelar asignación" onCerrar={handleClose} ancho="max-w-lg" acciones={null}>
      <div className="mb-4 rounded-lg border border-gray-200 bg-slate-50 p-3 text-sm text-gray-700">
        <p>
          <strong>Estudiante:</strong> {asignacion.estudiante?.nombre}
        </p>
        <p>
          <strong>Cargo:</strong> {asignacion.vacante?.cargo}
        </p>
        <p>
          <strong>Empresa:</strong> {asignacion.vacante?.empresa}
        </p>
      </div>

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-3">
        <div>
          <label htmlFor="motivo-cancelacion" className="mb-1 block text-sm font-medium text-gray-700">
            Motivo de cancelación <span className="text-red-600">*</span>
          </label>
          <textarea
            id="motivo-cancelacion"
            {...register('motivo')}
            rows={4}
            placeholder="Describe el motivo (mínimo 10 caracteres)..."
            className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm outline-none focus:border-primary focus:ring-2 focus:ring-primary/20"
          />
          {errors.motivo && (
            <p className="mt-1 text-sm text-red-600">{errors.motivo.message}</p>
          )}
        </div>

        <div className="flex justify-end gap-2 pt-2">
          <Button type="button" variant="ghost" size="sm" disabled={isPending} onClick={handleClose}>
            Cerrar
          </Button>
          <Button type="submit" variant="danger" size="sm" disabled={isPending}>
            {isPending ? 'Procesando...' : 'Confirmar cancelación'}
          </Button>
        </div>
      </form>
    </Modal>
  );
}
