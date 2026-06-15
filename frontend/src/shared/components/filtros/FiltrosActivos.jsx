import { useEffect, useMemo, useRef, useState } from 'react';
import { ChevronDown, ChevronLeft, Filter, X } from 'lucide-react';
import { Button, Input, Select } from '@/shared/components/ui';
import {
  chipsDesdeFiltros,
  limpiarCamposFiltro,
  valorActivo,
} from './filtrosActivosUtils';

/**
 * Filtros con UX unificada:
 * - Un solo campo/botón abre un panel para elegir tipo y valor
 * - Al elegir valor se aplica al instante (sin botón Agregar)
 * - Chips activos debajo, cada uno con X para quitar
 */
export default function FiltrosActivos({ campos, filtros, onChange }) {
  const contenedorRef = useRef(null);

  const [panelAbierto, setPanelAbierto] = useState(false);
  const [paso, setPaso] = useState('tipo');
  const [campoKey, setCampoKey] = useState('');
  const [valorTexto, setValorTexto] = useState('');

  const camposDisponibles = useMemo(
    () => campos.filter((c) => !valorActivo(filtros[c.key])),
    [campos, filtros]
  );

  const chips = useMemo(() => chipsDesdeFiltros(filtros, campos), [filtros, campos]);
  const campoActivo = campos.find((c) => c.key === campoKey);

  const cerrarPanel = () => {
    setPanelAbierto(false);
    setPaso('tipo');
    setCampoKey('');
    setValorTexto('');
  };

  const abrirPanel = () => {
    if (camposDisponibles.length === 0) return;
    setPanelAbierto(true);
    setPaso('tipo');
    setCampoKey('');
    setValorTexto('');
  };

  const elegirTipo = (key) => {
    setCampoKey(key);
    setPaso('valor');
    setValorTexto('');
  };

  const aplicarFiltro = (key, valor) => {
    if (!valorActivo(valor)) return;
    onChange({ ...filtros, [key]: valor, page: 0 });
    cerrarPanel();
  };

  const aplicarSelect = (value) => {
    if (!campoActivo || !valorActivo(value)) return;
    aplicarFiltro(campoActivo.key, value);
  };

  const aplicarTexto = (e) => {
    e?.preventDefault?.();
    if (!campoActivo || !valorTexto.trim()) return;
    aplicarFiltro(campoActivo.key, valorTexto.trim());
  };

  const quitarFiltro = (key) => {
    onChange({ ...filtros, [key]: '', page: 0 });
  };

  const limpiarTodos = () => {
    onChange(limpiarCamposFiltro(filtros, campos));
    cerrarPanel();
  };

  useEffect(() => {
    if (!panelAbierto) return;

    const handleClickFuera = (event) => {
      if (contenedorRef.current && !contenedorRef.current.contains(event.target)) {
        cerrarPanel();
      }
    };

    const handleEscape = (event) => {
      if (event.key === 'Escape') cerrarPanel();
    };

    document.addEventListener('mousedown', handleClickFuera);
    document.addEventListener('keydown', handleEscape);
    return () => {
      document.removeEventListener('mousedown', handleClickFuera);
      document.removeEventListener('keydown', handleEscape);
    };
  }, [panelAbierto]);

  const textoBoton =
    chips.length > 0
      ? `${chips.length} filtro${chips.length > 1 ? 's' : ''} activo${chips.length > 1 ? 's' : ''}`
      : 'Agregar filtros…';

  return (
    <div className="mb-4">
      <div ref={contenedorRef} className="relative max-w-md">
        <button
          type="button"
          onClick={() => (panelAbierto ? cerrarPanel() : abrirPanel())}
          disabled={!panelAbierto && camposDisponibles.length === 0 && chips.length === 0}
          className={[
            'flex w-full items-center gap-2 rounded-lg border bg-white px-3 py-2.5 text-left text-sm shadow-sm transition-colors',
            panelAbierto
              ? 'border-primary ring-2 ring-primary/20'
              : 'border-gray-300 hover:border-gray-400',
            !panelAbierto && camposDisponibles.length === 0 && chips.length > 0
              ? 'cursor-default opacity-70'
              : 'cursor-pointer',
          ].join(' ')}
        >
          <Filter className="h-4 w-4 shrink-0 text-primary" />
          <span className={`flex-1 truncate ${chips.length ? 'font-medium text-gray-800' : 'text-gray-500'}`}>
            {textoBoton}
          </span>
          {camposDisponibles.length > 0 && (
            <ChevronDown
              className={`h-4 w-4 shrink-0 text-gray-400 transition-transform ${panelAbierto ? 'rotate-180' : ''}`}
            />
          )}
        </button>

        {panelAbierto && camposDisponibles.length > 0 && (
          <div className="absolute left-0 right-0 top-[calc(100%+6px)] z-50 overflow-hidden rounded-xl border border-gray-200 bg-white shadow-lg">
            {paso === 'tipo' ? (
              <div className="p-1">
                <p className="px-3 py-2 text-xs font-semibold uppercase tracking-wide text-gray-500">
                  Filtrar por
                </p>
                {camposDisponibles.map((campo) => (
                  <button
                    key={campo.key}
                    type="button"
                    onClick={() => elegirTipo(campo.key)}
                    className="flex w-full items-center justify-between rounded-lg px-3 py-2.5 text-left text-sm text-gray-800 hover:bg-slate-50"
                  >
                    {campo.label}
                    <ChevronDown className="-rotate-90 h-4 w-4 text-gray-400" />
                  </button>
                ))}
              </div>
            ) : (
              <div className="p-4">
                <button
                  type="button"
                  onClick={() => setPaso('tipo')}
                  className="mb-3 inline-flex items-center gap-1 text-xs font-medium text-primary hover:underline"
                >
                  <ChevronLeft className="h-3.5 w-3.5" />
                  Volver
                </button>

                <p className="mb-1 text-xs font-semibold uppercase tracking-wide text-gray-500">
                  {campoActivo?.label}
                </p>

                {campoActivo?.type === 'select' ? (
                  <Select
                    autoFocus
                    defaultValue=""
                    onChange={(e) => aplicarSelect(e.target.value)}
                    className="w-full"
                  >
                    <option value="" disabled>
                      Seleccionar…
                    </option>
                    {(campoActivo.opciones ?? []).map((op) => (
                      <option key={op.value} value={op.value}>
                        {op.label}
                      </option>
                    ))}
                  </Select>
                ) : (
                  <form onSubmit={aplicarTexto} className="space-y-2">
                    <Input
                      autoFocus
                      value={valorTexto}
                      onChange={(e) => setValorTexto(e.target.value)}
                      placeholder={campoActivo?.placeholder ?? 'Escribir valor…'}
                    />
                    <p className="text-xs text-gray-500">Presiona Enter para aplicar</p>
                  </form>
                )}
              </div>
            )}
          </div>
        )}
      </div>

      {chips.length > 0 && (
        <div className="mt-2.5 flex flex-wrap items-center gap-2 rounded-lg border border-gray-200 bg-slate-50 px-3 py-2.5">
          {chips.map((chip) => (
            <span
              key={chip.key}
              className="inline-flex items-center gap-1.5 rounded-full border border-primary/15 bg-white px-3 py-1 text-xs font-medium text-gray-800 shadow-sm"
            >
              <span className="text-gray-500">{chip.label}:</span>
              <span>{chip.texto.split(': ')[1] ?? chip.texto}</span>
              <button
                type="button"
                onClick={() => quitarFiltro(chip.key)}
                className="rounded-full p-0.5 text-gray-400 hover:bg-red-50 hover:text-red-600"
                aria-label={`Quitar filtro ${chip.label}`}
              >
                <X className="h-3.5 w-3.5" />
              </button>
            </span>
          ))}
          <Button type="button" variant="ghost" size="sm" onClick={limpiarTodos} className="ml-auto">
            Limpiar todo
          </Button>
        </div>
      )}
    </div>
  );
}
