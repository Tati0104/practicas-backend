// src/modules/seguimiento/components/AvanceTutorModal.jsx

/**
 * Modal para que TUTOR_EMPRESARIAL registre un avance con porcentaje.
 */
import { useState } from 'react';
import { Button, Input, Modal } from '@/shared/components/ui';

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

  const puedeGuardar = descripcion.trim() && porcentaje !== '';

  return (
    <Modal
      titulo="Registrar avance"
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
            disabled={isPending || !puedeGuardar}
          >
            {isPending ? 'Guardando...' : 'Guardar'}
          </Button>
        </div>
      }
    >
      <div>
        <label className="mb-1.5 block text-sm font-semibold text-gray-700">
          Descripción del avance
        </label>
        <textarea
          value={descripcion}
          onChange={(e) => setDescripcion(e.target.value)}
          placeholder="Describe el avance realizado..."
          rows={4}
          className="w-full resize-y rounded-lg border border-gray-300 px-3 py-2 text-sm text-gray-900 outline-none focus:border-primary focus:ring-2 focus:ring-primary/20"
        />
      </div>

      <div>
        <label className="mb-1.5 block text-sm font-semibold text-gray-700">
          Porcentaje de avance (%)
        </label>
        <Input
          type="number"
          min={0}
          max={100}
          value={porcentaje}
          onChange={(e) => setPorcentaje(e.target.value)}
          placeholder="0 - 100"
        />
      </div>
    </Modal>
  );
}
