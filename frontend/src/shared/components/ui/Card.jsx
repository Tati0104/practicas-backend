/**
 * Contenedor con borde y sombra suave.
 */
export default function Card({ children, className = '', padding = 'p-5' }) {
  return (
    <div className={['ui-panel', padding, className].join(' ')}>
      {children}
    </div>
  );
}
