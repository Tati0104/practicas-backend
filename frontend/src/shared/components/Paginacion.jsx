/**
 * Componente reutilizable de paginación server-side (page base 0).
 */
export default function Paginacion({ pagina = 0, totalPaginas = 1, onCambiarPagina, className }) {
  if (totalPaginas <= 1) return null;

  return (
    <div style={estilos.contenedor} className={className}>
      <button
        type="button"
        onClick={() => onCambiarPagina(pagina - 1)}
        disabled={pagina === 0}
        style={estilos.btn}
        aria-label="Página anterior"
      >
        ← Anterior
      </button>
      <span style={estilos.indicador}>
        Página {pagina + 1} de {totalPaginas}
      </span>
      <button
        type="button"
        onClick={() => onCambiarPagina(pagina + 1)}
        disabled={pagina >= totalPaginas - 1}
        style={estilos.btn}
        aria-label="Página siguiente"
      >
        Siguiente →
      </button>
    </div>
  );
}

const estilos = {
  contenedor: {
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'center',
    gap: 16,
    marginTop: 20,
  },
  btn: {
    padding: '6px 14px',
    border: '1px solid #d1d5db',
    borderRadius: 6,
    background: '#fff',
    fontSize: 13,
    cursor: 'pointer',
  },
  indicador: {
    fontSize: 13,
    color: '#374151',
  },
};
