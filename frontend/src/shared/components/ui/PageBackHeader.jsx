import { ArrowLeft } from 'lucide-react';

/**
 * Encabezado de páginas de detalle con botón volver.
 */
export default function PageBackHeader({ titulo, descripcion, onVolver, acciones }) {
  return (
    <header className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
      <div>
        {onVolver && (
          <button
            type="button"
            onClick={onVolver}
            className="mb-2 inline-flex items-center gap-1 text-sm font-medium text-primary hover:underline"
          >
            <ArrowLeft className="h-4 w-4" aria-hidden="true" />
            Volver
          </button>
        )}
        <h1 className="text-xl font-bold text-gray-900 sm:text-2xl">{titulo}</h1>
        {descripcion && <p className="mt-1 text-sm text-gray-500">{descripcion}</p>}
      </div>
      {acciones && <div className="flex shrink-0 flex-wrap gap-2">{acciones}</div>}
    </header>
  );
}
