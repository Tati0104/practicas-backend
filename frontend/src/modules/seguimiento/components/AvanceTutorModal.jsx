// src/modules/seguimiento/components/AvanceTutorModal.jsx

/**
 * Modal para que TUTOR_EMPRESARIAL registre un avance con porcentaje.
 */
import { useState } from 'react';

export default function AvanceTutorModal({ isOpen, practicaId, onClose, onGuardar, isPending }) {
  const [descripcion, setDescripcion] = useState('');
  const [porcentaje, setPorcentaje] = useState('');

  if (!isOpen) return null;

  const handleGuardar = () => {
    if (!descripcion.trim() || porcentaje === '') return;
    onGuardar({ practicaId, descripcion, porcentaje: Number(porcentaje) });
    setDescripcion('');
    setPorcentaje('');
  };

  return (
    <div style={overlay}>
      <div style={modal}>
        <h2 style={{ margin: '0 0 16px', fontSize: 18, fontWeight: 700 }}>Registrar avance</h2>
        <div style={{ marginBottom: 12 }}>
          <label style={labelStyle}>Descripción del avance</label>
          <textarea
            value={descripcion}
            onChange={(e) => setDescripcion(e.target.value)}
            placeholder="Describe el avance realizado..."
            rows={4}
            style={textareaStyle}
          />
        </div>
        <div style={{ marginBottom: 16 }}>
          <label style={labelStyle}>Porcentaje de avance (%)</label>
          <input
            type="number"
            min={0}
            max={100}
            value={porcentaje}
            onChange={(e) => setPorcentaje(e.target.value)}
            placeholder="0 - 100"
            style={inputStyle}
          />
        </div>
        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 10 }}>
          <button onClick={onClose} style={btnCancelar}>Cancelar</button>
          <button onClick={handleGuardar} disabled={isPending || !descripcion.trim() || porcentaje === ''} style={btnGuardar}>
            {isPending ? 'Guardando...' : 'Guardar'}
          </button>
        </div>
      </div>
    </div>
  );
}

const overlay = { position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.4)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000 };
const modal = { background: '#fff', borderRadius: 12, padding: 24, width: '100%', maxWidth: 480, boxShadow: '0 8px 32px rgba(0,0,0,0.15)' };
const labelStyle = { display: 'block', fontSize: 13, fontWeight: 600, color: '#374151', marginBottom: 6 };
const textareaStyle = { width: '100%', padding: '10px 12px', border: '1px solid #d1d5db', borderRadius: 8, fontSize: 14, resize: 'vertical', boxSizing: 'border-box' };
const inputStyle = { width: '100%', padding: '8px 12px', border: '1px solid #d1d5db', borderRadius: 8, fontSize: 14, boxSizing: 'border-box' };
const btnCancelar = { padding: '8px 18px', border: '1px solid #d1d5db', borderRadius: 8, background: '#fff', fontSize: 14, cursor: 'pointer' };
const btnGuardar = { padding: '8px 18px', border: 'none', borderRadius: 8, background: '#15803d', color: '#fff', fontSize: 14, fontWeight: 700, cursor: 'pointer' };
