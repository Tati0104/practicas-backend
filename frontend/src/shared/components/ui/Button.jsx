const base =
  'inline-flex items-center justify-center gap-2 rounded-lg font-semibold transition-colors focus:outline-none focus:ring-2 focus:ring-primary/30 disabled:cursor-not-allowed disabled:opacity-50';

const variants = {
  primary: 'bg-primary text-white hover:bg-primary/90',
  secondary: 'border border-gray-300 bg-white text-gray-700 hover:bg-gray-50 dark:border-dark-border dark:bg-dark-elevated dark:text-slate-200 dark:hover:bg-dark-card',
  success: 'bg-emerald-50 text-emerald-800 hover:bg-emerald-100 dark:bg-emerald-950 dark:text-emerald-200 dark:hover:bg-emerald-900',
  danger: 'bg-red-50 text-red-800 hover:bg-red-100 dark:bg-red-950 dark:text-red-200 dark:hover:bg-red-900',
  warning: 'bg-amber-50 text-amber-800 hover:bg-amber-100 dark:bg-amber-950 dark:text-amber-200 dark:hover:bg-amber-900',
  info: 'bg-blue-50 text-blue-800 hover:bg-blue-100 dark:bg-blue-950 dark:text-blue-200 dark:hover:bg-blue-900',
  ghost: 'bg-gray-100 text-gray-700 hover:bg-gray-200 dark:bg-dark-elevated dark:text-slate-200 dark:hover:bg-dark-card',
};

const sizes = {
  sm: 'px-2.5 py-1 text-xs font-medium',
  md: 'px-4 py-2 text-sm',
};

/**
 * Botón reutilizable con variantes semánticas.
 */
export default function Button({
  variant = 'primary',
  size = 'md',
  className = '',
  type = 'button',
  children,
  ...props
}) {
  return (
    <button
      type={type}
      className={[base, variants[variant] ?? variants.primary, sizes[size] ?? sizes.md, className]
        .filter(Boolean)
        .join(' ')}
      {...props}
    >
      {children}
    </button>
  );
}
