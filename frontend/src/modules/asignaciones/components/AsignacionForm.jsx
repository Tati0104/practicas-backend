// src/modules/asignaciones/components/AsignacionForm.jsx

/**
 * Formulario modal para crear una nueva asignación.
 *
 * Flujo:
 *   1. El usuario selecciona una vacante activa.
 *   2. Al elegir vacante, se carga la lista de estudiantes aptos para esa vacante.
 *   3. El usuario selecciona el estudiante.
 *   4. Se muestra un resumen y se confirma.
 *
 * Usa React Hook Form + Zod.
 * Llama a useVacantesActivas y useEstudiantesAptos para poblar los selects.
 */
import { useEffect } from 'react';
import { useForm, useWatch } from 'react-hook-form';
import { z } from 'zod';
import { zodResolver } from '@hookform/resolvers/zod';
import { useVacantesActivas } from '../hooks/useVacantesActivas';
import { useEstudiantesAptos } from '../hooks/useEstudiantesAptos';

const schema = z.object({
  vacanteId:    z.string().min(1, 'Selecciona una vacante'),
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

  // Observamos los valores en tiempo real para cargar estudiantes y mostrar resumen
  const vacanteId     = useWatch({ control, name: 'vacanteId' });
  const estudianteId  = useWatch({ control, name: 'estudianteId' });

  const { vacantes, isLoading: loadingVacantes } = useVacantesActivas();
  const { estudiantes, isLoading: loadingEstudiantes } = useEstudiantesAptos({ vacanteId });

  // Limpia el formulario al cerrar
  useEffect(() => {
    if (!isOpen) reset();
  }, [isOpen, reset]);

  const onSubmit = (data) => {
    onCrear({ vacanteId: Number(data.vacanteId), estudianteId: Number(data.estudianteId) });
  };

  if (!isOpen) return null;

  // Vacante y estudiante seleccionados (para mostrar resumen)
  const vacanteSeleccionada    = vacantes.find((v) => String(v.id) === String(vacanteId));
  const estudianteSeleccionado = estudiantes.find((e) => String(e.id) === String(estudianteId));

  return (
    <div style={estilos.overlay}>
      <div style={estilos.modal}>
        <h2 style={estilos.titulo}>Nueva Asignación</h2>

        <form onSubmit={handleSubmit(onSubmit)} style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
          {/* Selector de vacante */}
          <div>
            <label style={estilos.label}>
              Vacante activa <span style={{ color: '#dc2626' }}>*</span>
            </label>
            <select {...register('vacanteId')} style={estilos.select} disabled={loadingVacantes}>
              <option value="">
                {loadingVacantes ? 'Cargando vacantes...' : '— Selecciona una vacante —'}
              </option>
              {vacantes.map((v) => (
                <option key={v.id} value={v.id}>
                  {v.cargo} · {v.empresa} (cupos: {v.cuposDisponibles})
                </option>
              ))}
            </select>
            {errors.vacanteId && <p style={estilos.error}>{errors.vacanteId.message}</p>}
          </div>

          {/* Selector de estudiante (se activa cuando hay vacanteId) */}
          <div>
            <label style={estilos.label}>
              Estudiante apto <span style={{ color: '#dc2626' }}>*</span>
            </label>
            <select
              {...register('estudianteId')}
              style={estilos.select}
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
            </select>
            {errors.estudianteId && <p style={estilos.error}>{errors.estudianteId.message}</p>}
          </div>

          {/* Resumen previo a la confirmación */}
          {vacanteSeleccionada && (
            <div style={estilos.resumen}>
              <p style={estilos.resumenTitulo}>📋 Resumen de la asignación</p>
              <p style={estilos.resumenFila}><strong>Cargo:</strong> {vacanteSeleccionada.cargo}</p>
              <p style={estilos.resumenFila}><strong>Empresa:</strong> {vacanteSeleccionada.empresa}</p>
            </div>
          )}

          <div style={estilos.botones}>
            <button
              type="button"
              onClick={() => { reset(); onClose(); }}
              disabled={isPending}
              style={estilos.btnCancelar}
            >
              Cancelar
            </button>
            <button type="submit" disabled={isPending} style={estilos.btnGuardar}>
              {isPending ? 'Guardando...' : 'Crear asignación'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

const estilos = {
  overlay: {
    position: 'fixed', inset: 0,
    background: 'rgba(0,0,0,0.45)',
    display: 'flex', alignItems: 'center', justifyContent: 'center',
    zIndex: 50, padding: 16,
  },
  modal: {
    background: '#fff', borderRadius: 10,
    width: '100%', maxWidth: 500,
    padding: 24, boxShadow: '0 10px 25px rgba(0,0,0,0.15)',
  },
  titulo: { fontSize: 18, fontWeight: 700, marginBottom: 16, color: '#111827' },
  label:  { display: 'block', fontSize: 13, fontWeight: 600, marginBottom: 4, color: '#374151' },
  select: {
    width: '100%', padding: '8px 12px',
    border: '1px solid #d1d5db', borderRadius: 6,
    fontSize: 13, fontFamily: 'Arial, sans-serif',
    background: '#fff', cursor: 'pointer', boxSizing: 'border-box',
  },
  error: { color: '#dc2626', fontSize: 12, marginTop: 4 },
  resumen: {
    background: '#eff6ff', borderRadius: 8,
    padding: '10px 14px', border: '1px solid #bfdbfe',
  },
  resumenTitulo: { fontWeight: 700, fontSize: 13, marginBottom: 6, color: '#1e40af' },
  resumenFila:   { fontSize: 13, color: '#374151', margin: '3px 0' },
  botones:    { display: 'flex', justifyContent: 'flex-end', gap: 8, marginTop: 8 },
  btnCancelar: {
    padding: '8px 16px', background: '#f1f5f9',
    color: '#374151', border: '1px solid #d1d5db',
    borderRadius: 6, fontSize: 13, cursor: 'pointer',
  },
  btnGuardar: {
    padding: '8px 18px', background: '#2563eb',
    color: '#fff', border: 'none',
    borderRadius: 6, fontSize: 13, cursor: 'pointer', fontWeight: 600,
  },
};
