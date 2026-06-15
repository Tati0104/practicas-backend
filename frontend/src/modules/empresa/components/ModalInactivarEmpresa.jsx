import { useState } from 'react';
import { Button, Modal } from '@/shared/components/ui';

export default function ModalInactivarEmpresa({ empresa, onConfirmar, onCerrar, guardando = false }) {
  const [motivo, setMotivo] = useState('');
  const [error, setError] = useState('');

  const confirmar = () => {
    if (!motivo.trim()) {
      setError('Debe escribir un comentario o motivo de inactivación');
      return;
    }
    setError('');
    onConfirmar(motivo.trim());
  };

  return (
    <Modal
      titulo="Inactivar empresa"
      onCerrar={onCerrar}
      ancho="max-w-lg"
      acciones={
        <div className="flex justify-end gap-2">
          <Button variant="secondary" size="sm" onClick={onCerrar} disabled={guardando}>
            Cancelar
          </Button>
          <Button variant="danger" size="sm" onClick={confirmar} disabled={guardando}>
            {guardando ? 'Inactivando…' : 'Confirmar inactivación'}
          </Button>
        </div>
      }
    >
      <p className="text-sm text-gray-600">
        Va a inactivar <strong>{empresa?.razonSocial}</strong>. Los tutores empresariales
        vinculados también quedarán inactivos. Puede reactivar la empresa más adelante si lo necesita.
      </p>

      {error && (
        <p className="mt-3 rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
          {error}
        </p>
      )}

      <div className="mt-4">
        <label className="mb-1 block text-xs font-semibold text-gray-700">
          Comentario / motivo <span className="text-red-500">*</span>
        </label>
        <textarea
          value={motivo}
          onChange={(e) => setMotivo(e.target.value)}
          rows={4}
          maxLength={500}
          placeholder="Ej: Contrato vencido, solicitud de la empresa, incumplimiento…"
          className="w-full resize-none rounded-lg border border-gray-300 px-3 py-2 text-sm outline-none focus:border-primary focus:ring-2 focus:ring-primary/20"
        />
        <p className="mt-1 text-xs text-gray-400">{motivo.length}/500</p>
      </div>
    </Modal>
  );
}
