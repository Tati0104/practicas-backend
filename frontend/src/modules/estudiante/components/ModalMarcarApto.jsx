import { useState } from 'react';
import { Button, Modal } from '@/shared/components/ui';

const OPCIONES_PRACTICA = [1, 2, 3, 4, 5];

export default function ModalMarcarApto({ estudiante, onConfirmar, onCerrar, guardando = false }) {
  const [numeroPractica, setNumeroPractica] = useState('1');

  return (
    <Modal
      titulo="Marcar estudiante como APTO"
      onCerrar={onCerrar}
      ancho="max-w-md"
      acciones={
        <div className="flex justify-end gap-2">
          <Button variant="secondary" size="sm" onClick={onCerrar} disabled={guardando}>
            Cancelar
          </Button>
          <Button
            size="sm"
            onClick={() => onConfirmar(Number(numeroPractica))}
            disabled={guardando}
          >
            {guardando ? 'Guardando…' : 'Confirmar'}
          </Button>
        </div>
      }
    >
      <p className="mb-4 text-sm text-gray-600">
        Indica a qué práctica de la carrera quedará habilitado{' '}
        <strong>{estudiante?.nombre}</strong>. Coordinación de Prácticas podrá asignarlo a
        una vacante de ese nivel.
      </p>
      <label className="mb-1 block text-xs font-semibold uppercase tracking-wide text-gray-500">
        Número de práctica
      </label>
      <select
        className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm"
        value={numeroPractica}
        onChange={(e) => setNumeroPractica(e.target.value)}
      >
        {OPCIONES_PRACTICA.map((n) => (
          <option key={n} value={String(n)}>
            Práctica {n}
          </option>
        ))}
      </select>
    </Modal>
  );
}
