import { useState, useRef } from 'react';
import { FileSpreadsheet, Upload } from 'lucide-react';
import { Button, Modal } from '@/shared/components/ui';

export default function ImportarExcel({ onImportar, onCerrar }) {
  const [archivo, setArchivo] = useState(null);
  const [resultado, setResultado] = useState(null);
  const [cargando, setCargando] = useState(false);
  const inputRef = useRef();

  const seleccionar = (e) => {
    const file = e.target.files[0];
    if (file && file.name.endsWith('.xlsx')) {
      setArchivo(file);
      setResultado(null);
    } else {
      alert('Solo se permiten archivos .xlsx');
    }
  };

  const importar = async () => {
    if (!archivo) return;
    setCargando(true);
    await new Promise((r) => setTimeout(r, 1200));
    setResultado({
      exitosos: 15,
      errores: 2,
      total: 17,
      detalleErrores: [
        { fila: 5, descripcion: 'Correo duplicado' },
        { fila: 12, descripcion: 'Identificación ya registrada' },
      ],
    });
    setCargando(false);
  };

  return (
    <Modal
      titulo="Importar estudiantes desde Excel"
      onCerrar={onCerrar}
      ancho="max-w-lg"
      acciones={
        <div className="mt-5 flex justify-end gap-2">
          <Button variant="secondary" size="sm" onClick={onCerrar}>
            Cerrar
          </Button>
          {!resultado && (
            <Button size="sm" onClick={importar} disabled={!archivo || cargando}>
              {cargando ? 'Importando...' : 'Importar'}
            </Button>
          )}
        </div>
      }
    >
      <button
        type="button"
        onClick={() => inputRef.current.click()}
        className={[
          'w-full rounded-xl border-2 border-dashed px-5 py-8 text-center transition-colors',
          archivo
            ? 'border-emerald-300 bg-emerald-50 text-emerald-700'
            : 'border-gray-300 bg-gray-50 text-gray-500 hover:border-gray-400 hover:bg-gray-100',
        ].join(' ')}
      >
        {archivo ? (
          <span className="inline-flex items-center gap-2 text-sm font-medium">
            <FileSpreadsheet className="h-5 w-5" aria-hidden="true" />
            {archivo.name}
          </span>
        ) : (
          <span className="inline-flex items-center gap-2 text-sm">
            <Upload className="h-5 w-5" aria-hidden="true" />
            Clic para seleccionar archivo .xlsx
          </span>
        )}
      </button>
      <input
        ref={inputRef}
        type="file"
        accept=".xlsx"
        onChange={seleccionar}
        className="hidden"
      />

      {resultado && (
        <div className="rounded-lg bg-gray-50 p-4">
          <p className="text-sm font-semibold text-gray-700">
            Resultado: {resultado.exitosos}/{resultado.total} importados correctamente
          </p>
          {resultado.detalleErrores.length > 0 && (
            <div className="mt-2">
              <p className="text-xs font-semibold text-red-600">Errores:</p>
              {resultado.detalleErrores.map((e, i) => (
                <p key={i} className="text-xs text-red-600">
                  Fila {e.fila}: {e.descripcion}
                </p>
              ))}
            </div>
          )}
        </div>
      )}
    </Modal>
  );
}
