// src/modules/seguimiento/components/ObservacionModal.jsx

/**
 * Modal para que COORD_PRACTICA / DOCENTE_ASESOR registren una observación.
 */
import { useState } from 'react';
import { Button, Modal } from '@/shared/components/ui';

export default function ObservacionModal({ isOpen, practicaId, onClose, onGuardar, isPending }) {
  const [texto, setTexto] = useState('');

  if (!isOpen) return null;

  const handleGuardar = () => {
    if (!texto.trim()) return;
    onGuardar({ practicaId, texto });
    setTexto('');
  };

  return (
    <Modal
      titulo="Registrar observación"
      onCerrar={onClose}
      ancho="max-w-lg"
      acciones={
        <div className="mt-5 flex justify-end gap-2">
          <Button variant="secondary" size="sm" onClick={onClose}>
            Cancelar
          </Button>
          <Button size="sm" onClick={handleGuardar} disabled={isPending || !texto.trim()}>
            {isPending ? 'Guardando...' : 'Guardar'}
          </Button>
        </div>
      }
    >
      <textarea
        value={texto}
        onChange={(e) => setTexto(e.target.value)}
        placeholder="Escribe la observación..."
        rows={5}
        className="w-full resize-y rounded-lg border border-gray-300 px-3 py-2 text-sm text-gray-900 outline-none focus:border-primary focus:ring-2 focus:ring-primary/20"
      />
    </Modal>
  );
}
