const clasesSelect = {
  default: 'ui-input cursor-pointer px-3 py-1.5 text-sm',
  compacto: 'ui-input cursor-pointer px-3 py-2 text-sm',
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
