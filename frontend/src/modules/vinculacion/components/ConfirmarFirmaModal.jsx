import { AlertTriangle } from 'lucide-react';
import { Button, Modal } from '@/shared/components/ui';

const ETIQUETAS_FIRMANTE = {
  TUTOR_EMPRESARIAL: 'Tutor Empresarial',
  ESTUDIANTE: 'Estudiante',
};

export default function ConfirmarFirmaModal({
  isOpen,
  documento,
  tipoFirmante,
  onClose,
  onConfirmar,
  isPending = false,
}) {
  if (!isOpen || !documento) return null;

  return (
    <Modal titulo="Confirmar firma" onCerrar={onClose} ancho="max-w-md" acciones={null}>
      <div className="space-y-4">
        <div className="rounded-lg border border-gray-200 bg-slate-50 p-3 text-sm text-gray-700">
          <p>
            <strong>Documento:</strong>{' '}
            {documento.tipo === 'CARTA' ? 'Carta de Presentación' : 'Convenio de Práctica'}
          </p>
          <p>
            <strong>Archivo:</strong> {documento.nombre || '(sin nombre)'}
          </p>
          <p>
            <strong>Firmante:</strong> {ETIQUETAS_FIRMANTE[tipoFirmante] || tipoFirmante}
          </p>
        </div>

        <div className="flex items-start gap-2 rounded-lg border border-amber-200 bg-amber-50 p-3 text-sm text-amber-900">
          <AlertTriangle className="mt-0.5 h-4 w-4 shrink-0" aria-hidden="true" />
          <p>Esta acción es irreversible. Una vez confirmada la firma no podrá deshacerse.</p>
        </div>

        <div className="flex justify-end gap-2">
          <Button variant="ghost" size="sm" onClick={onClose} disabled={isPending}>
            Cancelar
          </Button>
          <Button variant="success" size="sm" onClick={onConfirmar} disabled={isPending}>
            {isPending ? 'Procesando...' : 'Confirmar firma'}
          </Button>
        </div>
      </div>
    </Modal>
  );
}
