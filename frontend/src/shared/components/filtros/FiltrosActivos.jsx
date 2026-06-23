import { useEffect, useMemo, useState } from 'react';
import { Filter, Search, X } from 'lucide-react';
import { Button, Input, Modal, Select } from '@/shared/components/ui';
import {
  chipsDesdeFiltros,
  limpiarCamposFiltro,
  valorActivo,
} from './filtrosActivosUtils';

const CLAVE_BUSQUEDA = 'busqueda';

/**
 * Filtros unificados: búsqueda inline + botón que abre modal con todas las opciones.
 */
export default function FiltrosActivos({ campos, filtros, onChange, tituloModal = 'Filtros' }) {
  const [modalAbierto, setModalAbierto] = useState(false);
  const [borrador, setBorrador] = useState({});

  const campoBusqueda = useMemo(
    () => campos.find((c) => c.key === CLAVE_BUSQUEDA && c.type === 'text'),
    [campos]
  );

  const camposModal = useMemo(
    () => campos.filter((c) => !(c.key === CLAVE_BUSQUEDA && c.type === 'text')),
    [campos]
  );

  const chips = useMemo(() => chipsDesdeFiltros(filtros, campos), [filtros, campos]);
  const chipsModal = useMemo(
    () => chips.filter((chip) => chip.key !== CLAVE_BUSQUEDA),
    [chips]
  );

  const abrirModal = () => {
    const valores = Object.fromEntries(camposModal.map((c) => [c.key, filtros[c.key] ?? '']));
    setBorrador(valores);
    setModalAbierto(true);
  };

  const cerrarModal = () => setModalAbierto(false);

  const aplicarModal = () => {
    onChange({ ...filtros, ...borrador, page: 0 });
    cerrarModal();
  };

  const limpiarModal = () => {
    setBorrador(Object.fromEntries(camposModal.map((c) => [c.key, ''])));
  };

  const limpiarTodos = () => {
    onChange(limpiarCamposFiltro(filtros, campos));
    cerrarModal();
  };

  const quitarFiltro = (key) => {
    onChange({ ...filtros, [key]: '', page: 0 });
  };

  const actualizarBusqueda = (valor) => {
    onChange({ ...filtros, [CLAVE_BUSQUEDA]: valor, page: 0 });
  };

  useEffect(() => {
    if (!modalAbierto) return;
    setBorrador(Object.fromEntries(camposModal.map((c) => [c.key, filtros[c.key] ?? ''])));
  }, [modalAbierto, filtros, camposModal]);

  const totalActivos = chips.length;
  const activosModal = chipsModal.length;

  return (
    <div className="mb-4">
      <div className="flex flex-col gap-2 sm:flex-row sm:items-stretch">
        {campoBusqueda && (
          <div className="relative min-w-0 flex-1">
            <Search
              className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-gray-400"
              aria-hidden="true"
            />
            <Input
              type="search"
              value={filtros[CLAVE_BUSQUEDA] ?? ''}
              onChange={(e) => actualizarBusqueda(e.target.value)}
              placeholder={campoBusqueda.placeholder ?? 'Buscar…'}
              className="pl-9"
              aria-label={campoBusqueda.label}
            />
          </div>
        )}

        {camposModal.length > 0 && (
          <Button
            type="button"
            variant="primary"
            onClick={abrirModal}
            className="relative shrink-0 px-3.5 sm:min-w-[3rem]"
            aria-label="Abrir filtros"
          >
            <Filter className="h-4 w-4" />
            {activosModal > 0 && (
              <span className="absolute -right-1.5 -top-1.5 flex h-5 min-w-[1.25rem] items-center justify-center rounded-full bg-white px-1 text-[10px] font-bold text-primary">
                {activosModal}
              </span>
            )}
          </Button>
        )}
      </div>

      {totalActivos > 0 && (
        <div className="mt-2.5 flex flex-wrap items-center gap-2 rounded-xl border border-gray-200 bg-slate-50/80 px-3 py-2.5 dark:border-dark-border dark:bg-dark-elevated/50">
          {chips.map((chip) => (
            <span
              key={chip.key}
              className="inline-flex items-center gap-1.5 rounded-full border border-primary/15 bg-white px-3 py-1 text-xs font-medium text-gray-800 shadow-sm dark:bg-dark-card"
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

      {modalAbierto && (
        <Modal
          titulo={tituloModal}
          onCerrar={cerrarModal}
          ancho="max-w-lg"
          acciones={
            <div className="flex flex-wrap justify-end gap-2">
              <Button type="button" variant="ghost" size="sm" onClick={limpiarModal}>
                Limpiar
              </Button>
              <Button type="button" variant="secondary" size="sm" onClick={cerrarModal}>
                Cancelar
              </Button>
              <Button type="button" size="sm" onClick={aplicarModal}>
                Aplicar filtros
              </Button>
            </div>
          }
        >
          <div className="space-y-4">
            {camposModal.map((campo) => (
              <div key={campo.key}>
                <label
                  htmlFor={`filtro-modal-${campo.key}`}
                  className="mb-1.5 block text-sm font-semibold text-gray-700 dark:text-slate-200"
                >
                  {campo.label}
                </label>
                {campo.type === 'select' ? (
                  <Select
                    id={`filtro-modal-${campo.key}`}
                    value={borrador[campo.key] ?? ''}
                    onChange={(e) =>
                      setBorrador((prev) => ({ ...prev, [campo.key]: e.target.value }))
                    }
                    className="w-full"
                  >
                    <option value="">
                      {campo.placeholder ?? `Filtrar por ${campo.label.toLowerCase()}…`}
                    </option>
                    {(campo.opciones ?? [])
                      .filter((op) => op.value !== '')
                      .map((op) => (
                        <option key={op.value} value={op.value}>
                          {op.label}
                        </option>
                      ))}
                  </Select>
                ) : (
                  <Input
                    id={`filtro-modal-${campo.key}`}
                    type={campo.inputType ?? 'text'}
                    value={borrador[campo.key] ?? ''}
                    onChange={(e) =>
                      setBorrador((prev) => ({ ...prev, [campo.key]: e.target.value }))
                    }
                    placeholder={campo.placeholder ?? `Filtrar por ${campo.label.toLowerCase()}…`}
                  />
                )}
              </div>
            ))}
          </div>
        </Modal>
      )}
    </div>
  );
}
