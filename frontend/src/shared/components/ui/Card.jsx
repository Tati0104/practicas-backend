/**
 * Contenedor con borde y sombra suave.
 */
export default function Card({ children, className = '', padding = 'p-5' }) {
  return (
    <div
      className={[
        'rounded-xl border border-gray-200 bg-white shadow-sm',
        padding,
        className,
      ].join(' ')}
    >
      {children}
    </div>
  );
}
