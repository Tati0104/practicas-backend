import { Button } from '../ui';

export default function FiltrosBotones({ onBuscar, onLimpiar, textoBuscar = 'Buscar', mostrarLimpiar = true }) {
  return (
    <>
      <Button type="submit" size="sm" className="bg-blue-600 hover:bg-blue-700" onClick={onBuscar}>
        {textoBuscar}
      </Button>
      {mostrarLimpiar && (
        <Button type="button" variant="secondary" size="sm" onClick={onLimpiar}>
          Limpiar
        </Button>
      )}
    </>
  );
}
