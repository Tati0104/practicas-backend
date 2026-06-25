import { Loader2 } from 'lucide-react';

/**
 * Estado de carga centrado reutilizable.
 */
export default function LoadingState({ mensaje = 'Cargando...' }) {
  return (
    <div className="flex flex-col items-center justify-center gap-3 p-12 text-gray-600">
      <Loader2 className="h-8 w-8 animate-spin text-primary" aria-hidden="true" />
      <p className="text-sm">{mensaje}</p>
    </div>
  );
}
