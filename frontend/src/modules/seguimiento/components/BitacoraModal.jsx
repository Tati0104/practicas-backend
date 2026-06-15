// src/modules/seguimiento/components/BitacoraModal.jsx

/**
 * Modal para que ESTUDIANTE registre una entrada de bitácora.
 * Campos: actividades realizadas y aprendizajes obtenidos.
 */
import { useState } from 'react';
import { Button, Modal } from '@/shared/components/ui';

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

  const puedeGuardar = actividades.trim() && aprendizajes.trim();

  return (
    <Modal
      titulo="Nueva entrada de bitácora"
      onCerrar={onClose}
      ancho="max-w-lg"
      acciones={
        <div className="mt-5 flex justify-end gap-2">
          <Button variant="secondary" size="sm" onClick={onClose}>
            Cancelar
          </Button>
          <Button
            size="sm"
            className="bg-violet-600 hover:bg-violet-700"
            onClick={handleGuardar}
            disabled={isPending || !puedeGuardar}
          >
            {isPending ? 'Guardando...' : 'Guardar entrada'}
          </Button>
        </div>
      }
    >
      <div>
        <label className="mb-1.5 block text-sm font-semibold text-gray-700">
          Actividades realizadas
        </label>
        <textarea
          value={actividades}
          onChange={(e) => setActividades(e.target.value)}
          placeholder="¿Qué hiciste esta semana?"
          rows={4}
          className="w-full resize-y rounded-lg border border-gray-300 px-3 py-2 text-sm text-gray-900 outline-none focus:border-primary focus:ring-2 focus:ring-primary/20"
        />
      </div>

      <div>
        <label className="mb-1.5 block text-sm font-semibold text-gray-700">
          Aprendizajes obtenidos
        </label>
        <textarea
          value={aprendizajes}
          onChange={(e) => setAprendizajes(e.target.value)}
          placeholder="¿Qué aprendiste?"
          rows={4}
          className="w-full resize-y rounded-lg border border-gray-300 px-3 py-2 text-sm text-gray-900 outline-none focus:border-primary focus:ring-2 focus:ring-primary/20"
        />
      </div>
    </Modal>
  );
}
