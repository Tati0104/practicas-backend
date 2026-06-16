const clasesSelect =
  'ui-input w-full disabled:cursor-not-allowed disabled:bg-gray-50 dark:disabled:bg-dark-base';

export { clasesSelect as clasesCampoFormulario };

export default function Select({ className = '', ...props }) {
  return <select className={`${clasesSelect} ${className}`} {...props} />;
}
