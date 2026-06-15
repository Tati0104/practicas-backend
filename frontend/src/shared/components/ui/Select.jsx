const clasesSelect =
  'w-full rounded-lg border border-gray-300 bg-white px-3 py-2 text-sm outline-none focus:border-primary focus:ring-2 focus:ring-primary/20 disabled:cursor-not-allowed disabled:bg-gray-50';

export { clasesSelect as clasesCampoFormulario };

export default function Select({ className = '', ...props }) {
  return <select className={`${clasesSelect} ${className}`} {...props} />;
}
