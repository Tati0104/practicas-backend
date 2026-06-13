import { USE_MOCKS, etiquetaFuenteDatos } from '../config/dataSource';

export default function MockModeBanner() {
  if (!USE_MOCKS) return null;

  return (
    <div
      role="status"
      style={{
        background: '#fef3c7',
        borderBottom: '1px solid #f59e0b',
        color: '#92400e',
        padding: '8px 28px',
        fontSize: 13,
        fontFamily: 'Arial, sans-serif',
      }}
    >
      Modo <strong>{etiquetaFuenteDatos()}</strong>: los listados usan datos de prueba.
      Para conectar al backend real, pon <code>VITE_USE_MOCKS=false</code> en <code>frontend/.env</code> y reinicia el servidor.
    </div>
  );
}
