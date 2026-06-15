export default function ProgresoCierre({
  itemsCompletados = 0,
  totalItems = 0,
  progresoGlobal = 0,
}) {
  const porcentaje = Math.min(
    100,
    Math.max(0, Math.round((progresoGlobal ?? 0) * 100))
  );

  return (
    <section
      className="rounded-xl border border-gray-200 bg-white p-4 sm:p-5"
      aria-label="Progreso del cierre de práctica"
    >
      <div className="mb-3 flex flex-col gap-1 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <h2 className="text-lg font-semibold text-gray-900">Progreso del cierre</h2>
          <p className="text-sm text-gray-500">
            {itemsCompletados} de {totalItems} requisitos completados
          </p>
        </div>
        <p className="text-2xl font-bold text-primary">{porcentaje}%</p>
      </div>

      <div className="h-3 w-full overflow-hidden rounded-full bg-gray-200">
        <div
          className="h-full rounded-full bg-primary transition-all duration-300"
          style={{ width: `${porcentaje}%` }}
          role="progressbar"
          aria-valuenow={porcentaje}
          aria-valuemin={0}
          aria-valuemax={100}
          aria-label={`${porcentaje}% de requisitos completados`}
        />
      </div>
    </section>
  );
}
