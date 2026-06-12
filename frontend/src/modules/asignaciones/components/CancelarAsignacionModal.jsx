// src/modules/asignaciones/components/CancelarAsignacionModal.jsx

/**
 * Modal de confirmación para cancelar una asignación.
 *
 * Campos:
 *   - motivo (requerido, mínimo 10 caracteres)
 *
 * Usa React Hook Form + Zod para validación.
 * Al confirmar llama a la mutación cancelar() y cierra el modal.
 */
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { zodResolver } from '@hookform/resolvers/zod';

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
  onConfirmar, // recibe el motivo como string
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
    /* Overlay oscuro */
    <div style={estilos.overlay}>
      {/* Caja del modal */}
      <div style={estilos.modal}>
        <h2 style={estilos.titulo}>Cancelar Asignación</h2>

        {/* Resumen de la asignación */}
        <div style={estilos.resumen}>
          <p style={estilos.resumenFila}>
            <strong>Estudiante:</strong> {asignacion.estudiante?.nombre}
          </p>
          <p style={estilos.resumenFila}>
            <strong>Cargo:</strong> {asignacion.vacante?.cargo}
          </p>
          <p style={estilos.resumenFila}>
            <strong>Empresa:</strong> {asignacion.vacante?.empresa}
          </p>
        </div>

        <form onSubmit={handleSubmit(onSubmit)}>
          <label style={estilos.label}>
            Motivo de cancelación <span style={{ color: '#dc2626' }}>*</span>
          </label>
          <textarea
            {...register('motivo')}
            rows={4}
            placeholder="Describe el motivo (mínimo 10 caracteres)..."
            style={estilos.textarea}
          />
          {errors.motivo && (
            <p style={estilos.error}>{errors.motivo.message}</p>
          )}

          <div style={estilos.botones}>
            <button
              type="button"
              onClick={handleClose}
              disabled={isPending}
              style={estilos.btnCancelar}
            >
              Cerrar
            </button>
            <button
              type="submit"
              disabled={isPending}
              style={estilos.btnConfirmar}
            >
              {isPending ? 'Procesando...' : 'Confirmar cancelación'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

const estilos = {
  overlay: {
    position: 'fixed',
    inset: 0,
    background: 'rgba(0,0,0,0.45)',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    zIndex: 50,
    padding: 16,
  },
  modal: {
    background: '#fff',
    borderRadius: 10,
    width: '100%',
    maxWidth: 480,
    padding: 24,
    boxShadow: '0 10px 25px rgba(0,0,0,0.15)',
  },
  titulo: { fontSize: 18, fontWeight: 700, marginBottom: 12, color: '#111827' },
  resumen: {
    background: '#f8fafc',
    borderRadius: 8,
    padding: '10px 14px',
    marginBottom: 16,
    border: '1px solid #e5e7eb',
  },
  resumenFila: { fontSize: 13, color: '#374151', margin: '4px 0' },
  label: { display: 'block', fontSize: 13, fontWeight: 600, marginBottom: 6, color: '#374151' },
  textarea: {
    width: '100%',
    padding: '8px 12px',
    border: '1px solid #d1d5db',
    borderRadius: 6,
    fontSize: 13,
    fontFamily: 'Arial, sans-serif',
    resize: 'vertical',
    boxSizing: 'border-box',
    outline: 'none',
  },
  error: { color: '#dc2626', fontSize: 12, marginTop: 4 },
  botones: { display: 'flex', justifyContent: 'flex-end', gap: 8, marginTop: 16 },
  btnCancelar: {
    padding: '8px 16px',
    background: '#f1f5f9',
    color: '#374151',
    border: '1px solid #d1d5db',
    borderRadius: 6,
    fontSize: 13,
    cursor: 'pointer',
  },
  btnConfirmar: {
    padding: '8px 18px',
    background: '#dc2626',
    color: '#fff',
    border: 'none',
    borderRadius: 6,
    fontSize: 13,
    cursor: 'pointer',
    fontWeight: 600,
  },
};
