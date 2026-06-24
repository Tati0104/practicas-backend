const clasesInput = {
  default: 'ui-input min-w-[180px] flex-1 px-3 py-1.5 text-sm',
  compacto: 'ui-input min-w-[220px] px-3 py-2 text-sm',
};

export default function FiltroInput({ compacto = false, className = '', ...props }) {
  const base = compacto ? clasesInput.compacto : clasesInput.default;
  return <input className={`${base} ${className}`} {...props} />;
}
