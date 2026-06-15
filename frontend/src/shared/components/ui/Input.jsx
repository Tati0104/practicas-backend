/**
 * Campo de texto estándar del sistema.
 */
export default function Input({ className = '', ...props }) {
  return (
    <input
      className={[
        'w-full rounded-lg border border-gray-300 px-3 py-2 text-sm text-gray-900',
        'outline-none transition focus:border-primary focus:ring-2 focus:ring-primary/20',
        'disabled:cursor-not-allowed disabled:bg-gray-50',
        className,
      ].join(' ')}
      {...props}
    />
  );
}
