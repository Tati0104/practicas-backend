const clasesBar = {
  card: 'mb-4 flex flex-wrap gap-2.5 rounded-lg border border-gray-200 bg-slate-50 p-3 sm:gap-3 sm:p-4',
  inline: 'mb-4 flex flex-wrap gap-2.5 sm:gap-3',
};

/**
 * Contenedor reutilizable para barras de filtros.
 * variant: 'card' (fondo gris) | 'inline' (sin fondo)
 */
export default function FiltrosBar({ children, variant = 'card', as: Component = 'div', className = '', ...props }) {
  return (
    <Component className={`${clasesBar[variant] ?? clasesBar.card} ${className}`} {...props}>
      {children}
    </Component>
  );
}
