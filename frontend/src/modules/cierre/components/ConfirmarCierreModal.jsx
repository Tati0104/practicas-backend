import { useEffect, useId, useState } from 'react';
import { createPortal } from 'react-dom';
import { AlertTriangle, Loader2, X } from 'lucide-react';

const TEXTO_CONFIRMACION = 'CERRAR';

export default function ConfirmarCierreModal({
  isOpen,
  onClose,
  onConfirm,
  isPending = false,
}) {
  const tituloId = useId();
  const inputId = useId();
  const [confirmacion, setConfirmacion] = useState('');

  const confirmacionValida = confirmacion.trim().toUpperCase() === TEXTO_CONFIRMACION;

  useEffect(() => {
    if (!isOpen) {
      setConfirmacion('');
    }
  }, [isOpen]);

  useEffect(() => {
    if (!isOpen) return undefined;

    const handleEscape = (event) => {
      if (event.key === 'Escape' && !isPending) {
        setConfirmacion('');
        onClose?.();
      }
    };

    document.addEventListener('keydown', handleEscape);
    document.body.style.overflow = 'hidden';

    return () => {
      document.removeEventListener('keydown', handleEscape);
      document.body.style.overflow = '';
    };
  }, [isOpen, isPending, onClose]);

  if (!isOpen) return null;

  const handleClose = () => {
    if (isPending) return;
    setConfirmacion('');
    onClose?.();
  };

  const handleConfirm = () => {
    if (!confirmacionValida || isPending) return;
    onConfirm?.();
  };

  return createPortal(
    <div
      className="fixed inset-0 z-50 flex items-center justify-center p-4"
      role="presentation"
    >
      <button
        type="button"
        className="absolute inset-0 bg-black/40"
        aria-label="Cerrar modal"
        onClick={handleClose}
        disabled={isPending}
      />

      <div
        role="dialog"
        aria-modal="true"
        aria-labelledby={tituloId}
        className="relative z-10 w-full max-w-lg rounded-2xl bg-white p-6 shadow-xl"
      >
        <button
          type="button"
          onClick={handleClose}
          disabled={isPending}
          className="absolute right-4 top-4 rounded-lg p-1 text-gray-400 hover:bg-gray-100 hover:text-gray-600 disabled:opacity-50"
          aria-label="Cerrar"
        >
          <X className="h-5 w-5" aria-hidden="true" />
        </button>

        <div className="flex items-start gap-3 pr-8">
          <div className="rounded-full bg-red-100 p-2">
            <AlertTriangle className="h-5 w-5 text-red-600" aria-hidden="true" />
          </div>
          <div className="flex-1">
            <h2 id={tituloId} className="text-lg font-semibold text-gray-900">
              Confirmar cierre de práctica
            </h2>
            <p className="mt-2 text-sm font-medium text-red-600">
              Esta acción es irreversible. Una vez cerrada, la práctica quedará archivada y los
              documentos serán inmutables. No podrás subir archivos ni registrar firmas
              adicionales.
            </p>
            <p className="mt-2 text-sm text-gray-600">
              El estado final será COMPLETADA o REPROBADA según la nota final registrada.
            </p>
          </div>
        </div>

        <div className="mt-5">
          <label htmlFor={inputId} className="text-sm font-medium text-gray-700">
            Escribe <span className="font-bold text-gray-900">{TEXTO_CONFIRMACION}</span> para
            confirmar
          </label>
          <input
            id={inputId}
            type="text"
            value={confirmacion}
            onChange={(event) => setConfirmacion(event.target.value)}
            autoComplete="off"
            disabled={isPending}
            className="mt-2 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm text-gray-900 focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:bg-gray-100"
            placeholder={TEXTO_CONFIRMACION}
          />
        </div>

        <div className="mt-6 flex flex-col-reverse gap-2 sm:flex-row sm:justify-end">
          <button
            type="button"
            onClick={handleClose}
            disabled={isPending}
            className="rounded-lg border border-gray-300 bg-white px-4 py-2 text-sm font-semibold text-gray-700 hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-60"
          >
            Cancelar
          </button>
          <button
            type="button"
            onClick={handleConfirm}
            disabled={!confirmacionValida || isPending}
            className="inline-flex items-center justify-center gap-2 rounded-lg bg-red-600 px-4 py-2 text-sm font-semibold text-white hover:bg-red-700 disabled:cursor-not-allowed disabled:bg-red-300"
          >
            {isPending && <Loader2 className="h-4 w-4 animate-spin" aria-hidden="true" />}
            Ejecutar cierre
          </button>
        </div>
      </div>
    </div>,
    document.body
  );
}
