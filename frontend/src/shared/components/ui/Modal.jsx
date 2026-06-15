import Button from './Button';

/**
 * Modal centrado reutilizable (patrón overlay + panel).
 */
export default function Modal({ titulo, children, onCerrar, acciones, ancho = 'max-w-md' }) {
  return (
    <div
      className="fixed inset-0 z-[200] flex items-center justify-center bg-black/40 p-4"
      role="dialog"
      aria-modal="true"
      aria-labelledby="modal-titulo"
    >
      <div className={`w-full ${ancho} rounded-xl bg-white p-6 shadow-xl`}>
        {titulo && (
          <h3 id="modal-titulo" className="mb-4 text-base font-bold text-primary">
            {titulo}
          </h3>
        )}
        <div className="space-y-3">{children}</div>
        {acciones !== undefined ? (
          acciones
        ) : (
          <div className="mt-5 flex justify-end gap-2">
            <Button variant="ghost" size="sm" onClick={onCerrar}>
              Cancelar
            </Button>
          </div>
        )}
      </div>
    </div>
  );
}
