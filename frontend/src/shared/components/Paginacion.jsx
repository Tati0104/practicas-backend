import { Button } from './ui';

/**
 * Componente reutilizable de paginación server-side (page base 0).
 */
export default function Paginacion({ pagina = 0, totalPaginas = 1, onCambiarPagina, className = '' }) {
  if (totalPaginas <= 1) return null;

  return (
    <div className={`mt-5 flex items-center justify-center gap-4 ${className}`}>
      <Button
        variant="secondary"
        size="sm"
        onClick={() => onCambiarPagina(pagina - 1)}
        disabled={pagina === 0}
        aria-label="Página anterior"
      >
        ← Anterior
      </Button>
      <span className="text-sm text-gray-600 dark:text-slate-400">
        Página {pagina + 1} de {totalPaginas}
      </span>
      <Button
        variant="secondary"
        size="sm"
        onClick={() => onCambiarPagina(pagina + 1)}
        disabled={pagina >= totalPaginas - 1}
        aria-label="Página siguiente"
      >
        Siguiente →
      </Button>
    </div>
  );
}
