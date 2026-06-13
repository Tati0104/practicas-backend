import { estilosFiltros } from './estilos';

/**
 * Contenedor reutilizable para barras de filtros.
 * variant: 'card' (fondo gris) | 'inline' (sin fondo)
 */
export default function FiltrosBar({ children, variant = 'card', as: Component = 'div', ...props }) {
  const estilo = variant === 'card' ? estilosFiltros.formCard : estilosFiltros.formInline;
  return (
    <Component style={estilo} {...props}>
      {children}
    </Component>
  );
}
