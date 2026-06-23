import { useEffect } from 'react';
import { createPortal } from 'react-dom';
import { X } from 'lucide-react';
import Button from './Button';

/**
 * Modal centrado en viewport (portal a document.body).
 * Evita quedar recortado o desalineado por el layout con sidebar.
 */
export default function Modal({
  titulo,
  children,
  onCerrar,
  acciones,
  ancho = 'max-w-md',
  cerrarConBackdrop = true,
}) {
  useEffect(() => {
    const handleEscape = (event) => {
      if (event.key === 'Escape') onCerrar?.();
    };

    document.addEventListener('keydown', handleEscape);
    document.body.style.overflow = 'hidden';

    return () => {
      document.removeEventListener('keydown', handleEscape);
      document.body.style.overflow = '';
    };
  }, [onCerrar]);

  return createPortal(
    <div
      className="fixed inset-0 z-[9999] flex items-center justify-center p-4 sm:p-6"
      role="dialog"
      aria-modal="true"
      aria-labelledby={titulo ? 'modal-titulo' : undefined}
    >
      {cerrarConBackdrop && (
        <button
          type="button"
          className="absolute inset-0 bg-black/45 backdrop-blur-[1px]"
          aria-label="Cerrar modal"
          onClick={onCerrar}
        />
      )}

      <div
        className={[
          'relative z-10 flex max-h-[min(90vh,920px)] w-full flex-col',
          'rounded-2xl bg-white shadow-2xl ring-1 ring-black/5 dark:bg-dark-card dark:ring-white/[0.06]',
          ancho,
        ].join(' ')}
      >
        {(titulo || onCerrar) && (
          <div className="flex shrink-0 items-start justify-between gap-3 border-b ui-border px-5 py-4 sm:px-6">
            {titulo ? (
              <h3 id="modal-titulo" className="text-base font-bold text-primary dark:text-primary-glow sm:text-lg">
                {titulo}
              </h3>
            ) : (
              <span />
            )}
            {onCerrar && (
              <button
                type="button"
                onClick={onCerrar}
                className="rounded-lg p-1.5 text-gray-400 transition-colors hover:bg-gray-100 hover:text-gray-700 dark:text-slate-500 dark:hover:bg-white/[0.06] dark:hover:text-slate-200"
                aria-label="Cerrar"
              >
                <X className="h-5 w-5" />
              </button>
            )}
          </div>
        )}

        <div className="min-h-0 flex-1 overflow-y-auto px-5 py-4 sm:px-6">{children}</div>

        {acciones !== undefined ? (
          <div className="shrink-0 border-t ui-border bg-gray-50/80 px-5 py-4 dark:bg-dark-elevated/80 sm:px-6">
            {acciones}
          </div>
        ) : (
          onCerrar && (
            <div className="shrink-0 border-t ui-border bg-gray-50/80 px-5 py-4 dark:bg-dark-elevated/80 sm:px-6">
              <div className="flex justify-end">
                <Button variant="ghost" size="sm" onClick={onCerrar}>
                  Cancelar
                </Button>
              </div>
            </div>
          )
        )}
      </div>
    </div>,
    document.body
  );
}
