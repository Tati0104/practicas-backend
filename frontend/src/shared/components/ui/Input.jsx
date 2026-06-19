/**
 * Campo de texto estándar del sistema.
 */
export default function Input({ className = '', ...props }) {
  return (
    <input
      className={['ui-input w-full disabled:cursor-not-allowed disabled:bg-gray-50 dark:disabled:bg-dark-base', className]
        .filter(Boolean)
        .join(' ')}
      {...props}
    />
  );
}
