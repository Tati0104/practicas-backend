// src/modules/seguimiento/components/BitacoraModal.jsx

/**
 * Modal para que ESTUDIANTE registre una entrada de bitácora.
 * Campos: actividades realizadas y aprendizajes obtenidos.
 */
import { useState } from 'react';

export default function BitacoraModal({ isOpen, practicaId, onClose, onGuardar, isPending }) {
  const [actividades, setActividades] = useState('');
  const [aprendizajes, setAprendizajes] = useState('');

  if (!isOpen) return null;

  const handleGuardar = () => {
    if (!actividades.trim() || !aprendizajes.trim()) return;
    onGuardar({ practicaId, actividades, aprendizajes });
    setActividades('');
    setAprendizajes('');
  };

  return (
    <div style={overlay}>
      <div style={modal}>
        <h2 style={{ margin: '0 0 16px', fontSize: 18, fontWeight: 700 }}>Nueva entrada de bitácora</h2>
        <div style={{ marginBottom: 12 }}>
          <label style={labelStyle}>Actividades realizadas</label>
          <textarea
            value={actividades}
            onChange={(e) => setActividades(e.target.value)}
            placeholder="¿Qué hiciste esta semana?"
            rows={4}
            style={textareaStyle}
          />
        </div>
        <div style={{ marginBottom: 16 }}>
          <label style={labelStyle}>Aprendizajes obtenidos</label>
          <textarea
            value={aprendizajes}
            onChange={(e) => setAprendizajes(e.target.value)}
            placeholder="¿Qué aprendiste?"
            rows={4}
            style={textareaStyle}
          />
        </div>
        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 10 }}>
          <button onClick={onClose} style={btnCancelar}>Cancelar</button>
          <button onClick={handleGuardar} disabled={isPending || !actividades.trim() || !aprendizajes.trim()} style={btnGuardar}>
            {isPending ? 'Guardando...' : 'Guardar entrada'}
          </button>
        </div>
      </div>
    </div>
  );
}

const overlay = { position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.4)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000 };
const modal = { background: '#fff', borderRadius: 12, padding: 24, width: '100%', maxWidth: 500, boxShadow: '0 8px 32px rgba(0,0,0,0.15)' };
const labelStyle = { display: 'block', fontSize: 13, fontWeight: 600, color: '#374151', marginBottom: 6 };
const textareaStyle = { width: '100%', padding: '10px 12px', border: '1px solid #d1d5db', borderRadius: 8, fontSize: 14, resize: 'vertical', boxSizing: 'border-box' };
const btnCancelar = { padding: '8px 18px', border: '1px solid #d1d5db', borderRadius: 8, background: '#fff', fontSize: 14, cursor: 'pointer' };
const btnGuardar = { padding: '8px 18px', border: 'none', borderRadius: 8, background: '#7c3aed', color: '#fff', fontSize: 14, fontWeight: 700, cursor: 'pointer' };
