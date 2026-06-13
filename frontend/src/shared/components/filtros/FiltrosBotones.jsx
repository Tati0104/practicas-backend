import { estilosFiltros } from './estilos';

export default function FiltrosBotones({ onBuscar, onLimpiar, textoBuscar = 'Buscar', mostrarLimpiar = true }) {
  return (
    <>
      <button type="submit" style={estilosFiltros.btnBuscar} onClick={onBuscar}>
        {textoBuscar}
      </button>
      {mostrarLimpiar && (
        <button type="button" style={estilosFiltros.btnLimpiar} onClick={onLimpiar}>
          Limpiar
        </button>
      )}
    </>
  );
}
