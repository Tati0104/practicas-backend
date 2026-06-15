const variants = {
  success: 'bg-emerald-100 text-emerald-800',
  danger: 'bg-red-100 text-red-800',
  warning: 'bg-amber-100 text-amber-800',
  info: 'bg-blue-100 text-blue-800',
  neutral: 'bg-gray-100 text-gray-700',
  purple: 'bg-violet-100 text-violet-800',
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
