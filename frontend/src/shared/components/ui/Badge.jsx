const variants = {
  success: 'bg-emerald-100 text-emerald-800 dark:bg-emerald-950 dark:text-emerald-200',
  danger: 'bg-red-100 text-red-800 dark:bg-red-950 dark:text-red-200',
  warning: 'bg-amber-100 text-amber-800 dark:bg-amber-950 dark:text-amber-200',
  info: 'bg-blue-100 text-blue-800 dark:bg-blue-950 dark:text-blue-200',
  neutral: 'bg-gray-100 text-gray-700 dark:bg-slate-800 dark:text-slate-200',
  purple: 'bg-violet-100 text-violet-800 dark:bg-violet-950 dark:text-violet-200',
};

/**
 * Etiqueta de estado reutilizable.
 */
export default function Badge({ children, variant = 'neutral', className = '' }) {
  return (
    <span
      className={[
        'inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-semibold',
        variants[variant] ?? variants.neutral,
        className,
      ].join(' ')}
    >
      {children}
    </span>
  );
}
