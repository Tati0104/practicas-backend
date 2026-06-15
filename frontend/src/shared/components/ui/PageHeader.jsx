/**
 * Encabezado reutilizable para páginas internas (consistencia visual).
 */
export default function PageHeader({ titulo, descripcion, acciones }) {
  return (
    <div className="mb-6 flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
      <div>
        <h1 className="text-xl font-bold text-gray-900 sm:text-2xl">{titulo}</h1>
        {descripcion && (
          <p className="mt-1 text-sm text-gray-500">{descripcion}</p>
        )}
      </div>
      {acciones && <div className="flex shrink-0 flex-wrap gap-2">{acciones}</div>}
    </div>
  );
}
