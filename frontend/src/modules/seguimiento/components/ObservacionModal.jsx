// src/modules/seguimiento/components/ObservacionModal.jsx
import { useState } from 'react';
import { Button, Modal } from '@/shared/components/ui';

export default function ObservacionModal({
  isOpen,
  practicaId,
  numCortes = 3,
  onClose,
  onGuardar,
  isPending,
}) {
  const [observacion, setObservacion] = useState('');
  const [corte, setCorte] = useState(1);
  const [visibleParaEstudiante, setVisibleParaEstudiante] = useState(true);

  if (!isOpen) return null;

  const handleGuardar = () => {
    if (!observacion.trim()) return;
    onGuardar({ practicaId, corte, observacion, visibleParaEstudiante });
    setObservacion('');
    setCorte(1);
    setVisibleParaEstudiante(true);
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
          <Button size="sm" onClick={handleGuardar} disabled={isPending || !observacion.trim()}>
            {isPending ? 'Guardando...' : 'Guardar'}
          </Button>
        </div>
      }
    >
      <div className="flex flex-col gap-4">
        <div>
          <label className="mb-1.5 block text-sm font-semibold text-gray-700">Corte</label>
          <select
            value={corte}
            onChange={(e) => setCorte(Number(e.target.value))}
            className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm text-gray-900 outline-none focus:border-primary focus:ring-2 focus:ring-primary/20"
          >
            {Array.from({ length: numCortes }, (_, i) => i + 1).map((n) => (
              <option key={n} value={n}>
                Corte {n}
              </option>
            ))}
          </select>
        </div>

        <div>
          <label className="mb-1.5 block text-sm font-semibold text-gray-700">Observación</label>
          <textarea
            value={observacion}
            onChange={(e) => setObservacion(e.target.value)}
            placeholder="Escribe la observación..."
            rows={5}
            className="w-full resize-y rounded-lg border border-gray-300 px-3 py-2 text-sm text-gray-900 outline-none focus:border-primary focus:ring-2 focus:ring-primary/20"
          />
        </div>

        <label className="flex cursor-pointer items-center gap-2 text-sm text-gray-700">
          <input
            type="checkbox"
            checked={visibleParaEstudiante}
            onChange={(e) => setVisibleParaEstudiante(e.target.checked)}
            className="h-4 w-4 rounded border-gray-300 text-primary"
          />
          Visible para el estudiante
        </label>
      </div>
    </Modal>
  );
}
