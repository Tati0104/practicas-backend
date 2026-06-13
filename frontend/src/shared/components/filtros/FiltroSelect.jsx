import { estilosFiltros } from './estilos';

/**
 * @param {{ opciones: { value: string, label: string }[], compacto?: boolean }} props
 */
export default function FiltroSelect({ opciones, compacto = false, style, ...props }) {
  const base = compacto ? estilosFiltros.selectCompacto : estilosFiltros.select;
  return (
    <select style={{ ...base, ...style }} {...props}>
      {opciones.map((op) => (
        <option key={op.value || 'all'} value={op.value}>
          {op.label}
        </option>
      ))}
    </select>
  );
}
