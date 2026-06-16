export default function ContenedorGrafica({ titulo, descripcion, children, className = '' }) {
  return (
    <div
      className={`rounded-2xl border border-gray-100 bg-white p-5 shadow-sm ${className}`}
    >
      <div className="mb-4">
        <h3 className="text-sm font-semibold text-gray-800">{titulo}</h3>
        {descripcion && <p className="mt-0.5 text-xs text-gray-500">{descripcion}</p>}
      </div>
      {children}
    </div>
  );
}
