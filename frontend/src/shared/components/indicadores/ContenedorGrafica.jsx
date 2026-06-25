export default function ContenedorGrafica({ titulo, descripcion, children, className = '' }) {
  return (
    <div className={`ui-panel p-5 ${className}`}>
      <div className="mb-4">
        <h3 className="text-sm font-semibold ui-text-title">{titulo}</h3>
        {descripcion && <p className="mt-0.5 text-xs ui-text-muted">{descripcion}</p>}
      </div>
      {children}
    </div>
  );
}
