import { RefreshCw } from 'lucide-react';
import Button from './Button';

/**
 * Estado de error con opción de reintentar.
 */
export default function ErrorState({ mensaje, onReintentar }) {
  return (
    <div className="rounded-xl border border-red-200 bg-red-50 p-6 text-center">
      <p className="text-sm text-red-700" role="alert">
        {mensaje}
      </p>
      {onReintentar && (
        <Button
          variant="danger"
          size="sm"
          className="mt-4 bg-red-600 text-white hover:bg-red-700"
          onClick={onReintentar}
        >
          <RefreshCw className="h-4 w-4" aria-hidden="true" />
          Reintentar
        </Button>
      )}
    </div>
  );
}
