import { estilosFiltros } from './estilos';

export default function FiltroInput({ compacto = false, style, ...props }) {
  const base = compacto ? estilosFiltros.inputCompacto : estilosFiltros.input;
  return <input style={{ ...base, ...style }} {...props} />;
}
