import { useMemo, useState } from 'react';
import { X } from 'lucide-react';
import FiltrosBar from './FiltrosBar';
import FiltroInput from './FiltroInput';
import { Button, Select } from '@/shared/components/ui';
import {
  chipsDesdeFiltros,
  limpiarCamposFiltro,
  valorActivo,
} from './filtrosActivosUtils';

/**
 * Barra de filtros reutilizable:
 * 1) Elegir tipo de filtro en un select
 * 2) Elegir valor en un segundo control
 * 3) Agregar → aparece como chip con X para quitar
 *
 * @param {object} props
 * @param {{ key: string, label: string, type: 'select'|'text', opciones?: {value:string,label:string}[], placeholder?: string }[]} props.campos
 * @param {object} props.filtros
 * @param {(filtros: object) => void} props.onChange
 */
export default function FiltrosActivos({ campos, filtros, onChange, variant = 'inline' }) {
  const camposDisponibles = useMemo(
    () => campos.filter((c) => !valorActivo(filtros[c.key])),
    [campos, filtros]
  );

  const [campoKey, setCampoKey] = useState('');
  const [valor, setValor] = useState('');

  const campoActivo = campos.find((c) => c.key === campoKey) ?? camposDisponibles[0];
  const keySeleccionada = campoActivo?.key ?? '';

  const chips = useMemo(() => chipsDesdeFiltros(filtros, campos), [filtros, campos]);

  const cambiarCampo = (key) => {
    setCampoKey(key);
    setValor('');
  };

  const aplicarFiltro = (e) => {
    e?.preventDefault?.();
    if (!campoActivo || !valorActivo(valor)) return;

    onChange({ ...filtros, [campoActivo.key]: valor, page: 0 });
    setValor('');
    setCampoKey('');
  };

  const quitarFiltro = (key) => {
    onChange({ ...filtros, [key]: '', page: 0 });
  };

  const limpiarTodos = () => {
    onChange(limpiarCamposFiltro(filtros, campos));
    setValor('');
    setCampoKey('');
  };

  const puedeAgregar = campoActivo && valorActivo(valor);

  return (
    <FiltrosBar variant={variant} className="items-center">
      {chips.map((chip) => (
        <span
          key={chip.key}
          className="inline-flex items-center gap-1 rounded-full border border-primary/20 bg-primary/5 px-2.5 py-1 text-xs font-medium text-primary"
        >
          {chip.texto}
          <button
            type="button"
            onClick={() => quitarFiltro(chip.key)}
            className="rounded-full p-0.5 hover:bg-primary/10"
            aria-label={`Quitar filtro ${chip.label}`}
          >
            <X className="h-3.5 w-3.5" />
          </button>
        </span>
      ))}

      {camposDisponibles.length > 0 ? (
        <form
          onSubmit={aplicarFiltro}
          className="flex flex-wrap items-center gap-2"
        >
          <Select
            value={keySeleccionada}
            onChange={(e) => cambiarCampo(e.target.value)}
            className="min-w-[140px] w-auto"
          >
            <option value="" disabled>
              Filtrar por…
            </option>
            {camposDisponibles.map((c) => (
              <option key={c.key} value={c.key}>
                {c.label}
              </option>
            ))}
          </Select>

          {campoActivo?.type === 'select' ? (
            <Select
              value={valor}
              onChange={(e) => setValor(e.target.value)}
              className="min-w-[160px] w-auto"
              disabled={!campoActivo}
            >
              <option value="">Seleccionar valor…</option>
              {(campoActivo.opciones ?? []).map((op) => (
                <option key={op.value} value={op.value}>
                  {op.label}
                </option>
              ))}
            </Select>
          ) : (
            <FiltroInput
              compacto
              value={valor}
              onChange={(e) => setValor(e.target.value)}
              placeholder={campoActivo?.placeholder ?? 'Valor…'}
              className="min-w-[180px] flex-none"
              disabled={!campoActivo}
            />
          )}

          <Button type="submit" size="sm" disabled={!puedeAgregar}>
            Agregar
          </Button>
        </form>
      ) : (
        chips.length > 0 && (
          <span className="text-xs text-gray-500">Todos los filtros aplicados</span>
        )
      )}

      {chips.length > 0 && (
        <Button type="button" variant="secondary" size="sm" onClick={limpiarTodos}>
          Limpiar todo
        </Button>
      )}
    </FiltrosBar>
  );
}
