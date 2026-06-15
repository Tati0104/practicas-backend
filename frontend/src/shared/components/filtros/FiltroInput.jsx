const clasesInput = {
  default: 'min-w-[180px] flex-1 rounded-md border border-gray-300 px-3 py-1.5 text-sm outline-none focus:border-primary focus:ring-2 focus:ring-primary/20',
  compacto: 'min-w-[220px] rounded-lg border border-gray-300 px-3 py-2 text-sm outline-none focus:border-primary focus:ring-2 focus:ring-primary/20',
};

export default function FiltroInput({ compacto = false, className = '', ...props }) {
  const base = compacto ? clasesInput.compacto : clasesInput.default;
  return <input className={`${base} ${className}`} {...props} />;
}
