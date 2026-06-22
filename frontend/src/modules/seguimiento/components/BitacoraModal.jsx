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
  const [archivo, setArchivo] = useState(null);

  if (!isOpen) return null;

  const handleGuardar = () => {
    if (!actividades.trim() || !aprendizajes.trim()) return;
    const descripcion = [
      'Actividades realizadas:',
      actividades.trim(),
      '',
      'Aprendizajes obtenidos:',
      aprendizajes.trim(),
      archivo ? `\n\nArchivo adjunto: ${archivo.name}` : '',
    ].join('\n');
    onGuardar({ practicaId, actividades, aprendizajes, descripcion, archivo });
    setActividades('');
    setAprendizajes('');
    setArchivo(null);
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

      <div>
        <label className="mb-1.5 block text-sm font-semibold text-gray-700">
          Archivo de soporte (opcional)
        </label>
        <input
          type="file"
          accept=".pdf,.doc,.docx,.png,.jpg,.jpeg"
          onChange={(e) => setArchivo(e.target.files?.[0] ?? null)}
          className="block w-full text-sm text-gray-700 file:mr-3 file:rounded-lg file:border-0 file:bg-violet-50 file:px-3 file:py-2 file:text-sm file:font-semibold file:text-violet-700"
        />
        <p className="mt-1 text-xs text-gray-500">
          PDF, Word o imagen. El docente asesor revisará tu entrega en el historial.
        </p>
      </div>
    </Modal>
  );
}
