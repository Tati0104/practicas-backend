import { USE_MOCKS, etiquetaFuenteDatos } from '../config/dataSource';

export default function MockModeBanner() {
  if (!USE_MOCKS) return null;

  return (
    <div
      role="status"
      className="border-b border-amber-400 bg-amber-50 px-4 py-2 text-sm text-amber-900 sm:px-6"
    >
      Modo <strong>{etiquetaFuenteDatos()}</strong>: los listados usan datos de prueba.
      Para conectar al backend real, pon <code className="rounded bg-amber-100 px-1">VITE_USE_MOCKS=false</code> en{' '}
      <code className="rounded bg-amber-100 px-1">frontend/.env</code> y reinicia el servidor.
    </div>
  );
}
