// src/modules/seguimiento/components/ObservacionModal.jsx

/**
 * Modal para que COORD_PRACTICA / DOCENTE_ASESOR registren una observación.
 */
import { useState } from 'react';

export default function ObservacionModal({ isOpen, practicaId, onClose, onGuardar, isPending }) {
  const [texto, setTexto] = useState('');

  if (!isOpen) return null;

  const handleGuardar = () => {
    if (!texto.trim()) return;
    onGuardar({ practicaId, texto });
    setTexto('');
  };

  return (
    <div style={overlay}>
      <div style={modal}>
        <h2 style={{ margin: '0 0 16px', fontSize: 18, fontWeight: 700 }}>Registrar observación</h2>
        <textarea
          value={texto}
          onChange={(e) => setTexto(e.target.value)}
          placeholder="Escribe la observación..."
          rows={5}
          style={{ width: '100%', padding: '10px 12px', border: '1px solid #d1d5db', borderRadius: 8, fontSize: 14, resize: 'vertical', boxSizing: 'border-box' }}
        />
        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 10, marginTop: 16 }}>
          <button onClick={onClose} style={btnCancelar}>Cancelar</button>
          <button onClick={handleGuardar} disabled={isPending || !texto.trim()} style={btnGuardar}>
            {isPending ? 'Guardando...' : 'Guardar'}
          </button>
        </div>
      </div>
    </div>
  );
}

const overlay = { position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.4)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000 };
const modal = { background: '#fff', borderRadius: 12, padding: 24, width: '100%', maxWidth: 480, boxShadow: '0 8px 32px rgba(0,0,0,0.15)' };
const btnCancelar = { padding: '8px 18px', border: '1px solid #d1d5db', borderRadius: 8, background: '#fff', fontSize: 14, cursor: 'pointer' };
const btnGuardar = { padding: '8px 18px', border: 'none', borderRadius: 8, background: '#2563eb', color: '#fff', fontSize: 14, fontWeight: 700, cursor: 'pointer' };
