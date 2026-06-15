const clasesSelect = {
  default: 'cursor-pointer rounded-md border border-gray-300 bg-white px-3 py-1.5 text-sm outline-none focus:border-primary focus:ring-2 focus:ring-primary/20',
  compacto: 'cursor-pointer rounded-lg border border-gray-300 bg-white px-3 py-2 text-sm outline-none focus:border-primary focus:ring-2 focus:ring-primary/20',
};

/**
 * @param {{ opciones: { value: string, label: string }[], compacto?: boolean }} props
 */
export default function FiltroSelect({ opciones, compacto = false, className = '', ...props }) {
  const base = compacto ? clasesSelect.compacto : clasesSelect.default;
  return (
    <select className={`${base} ${className}`} {...props}>
      {opciones.map((op) => (
        <option key={op.value || 'all'} value={op.value}>
          {op.label}
        </option>
      ))}
    </select>
  );
}
