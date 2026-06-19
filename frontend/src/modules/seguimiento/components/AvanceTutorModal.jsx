// src/modules/seguimiento/components/AvanceTutorModal.jsx

import { useState } from 'react';
import { Button, Modal, Select } from '@/shared/components/ui';

const CORTES = [1, 2, 3, 4];

export default function AvanceTutorModal({ isOpen, practicaId, onClose, onGuardar, isPending }) {
  const [avance, setAvance] = useState('');
  const [corte, setCorte] = useState('1');

  if (!isOpen) return null;

  const handleGuardar = () => {
    if (!avance.trim()) return;
    onGuardar({ practicaId, avance: avance.trim(), corte: Number(corte) });
    setAvance('');
    setCorte('1');
  };

  return (
    <Modal
      titulo="Registrar observación de corte"
      onCerrar={onClose}
      ancho="max-w-lg"
      acciones={
        <div className="mt-5 flex justify-end gap-2">
          <Button variant="secondary" size="sm" onClick={onClose}>
            Cancelar
          </Button>
          <Button
            size="sm"
            className="bg-emerald-600 hover:bg-emerald-700"
            onClick={handleGuardar}
            disabled={isPending || !avance.trim()}
          >
            {isPending ? 'Guardando...' : 'Guardar'}
          </Button>
        </div>
      }
    >
      <div>
        <label className="mb-1.5 block text-sm font-semibold text-gray-700">
          Corte
        </label>
        <Select value={corte} onChange={(e) => setCorte(e.target.value)}>
          {CORTES.map((c) => (
            <option key={c} value={c}>
              Corte {c}
            </option>
          ))}
        </Select>
      </div>

      <div>
        <label className="mb-1.5 block text-sm font-semibold text-gray-700">
          Observación / avance del practicante
        </label>
        <textarea
          value={avance}
          onChange={(e) => setAvance(e.target.value)}
          placeholder="Describe el avance o escribe tu observación sobre el practicante..."
          rows={5}
          className="w-full resize-y rounded-lg border border-gray-300 px-3 py-2 text-sm text-gray-900 outline-none focus:border-primary focus:ring-2 focus:ring-primary/20"
        />
      </div>
    </Modal>
  );
}
