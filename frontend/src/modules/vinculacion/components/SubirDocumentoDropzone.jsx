import { useState, useRef } from 'react';
import { FileText, Upload } from 'lucide-react';
import { Button } from '@/shared/components/ui';

const TIPOS_ACEPTADOS = [
  'application/pdf',
  'image/jpeg',
  'image/jpg',
  'image/png',
];

const EXTENSIONES_LEGIBLES = '.pdf, .jpg, .jpeg, .png';

export default function SubirDocumentoDropzone({
  titulo,
  onSubir,
  isPending = false,
  deshabilitado = false,
  maxMB = 10,
}) {
  const [archivo, setArchivo] = useState(null);
  const [errorLocal, setErrorLocal] = useState('');
  const [arrastrando, setArrastrando] = useState(false);
  const inputRef = useRef(null);
  const maxBytes = maxMB * 1024 * 1024;

  const procesarArchivo = (file) => {
    setErrorLocal('');
    if (!TIPOS_ACEPTADOS.includes(file.type)) {
      setErrorLocal(`Solo se permiten archivos ${EXTENSIONES_LEGIBLES}`);
      return;
    }
    if (file.size > maxBytes) {
      setErrorLocal(`El archivo supera el límite de ${maxMB} MB`);
      return;
    }
    setArchivo(file);
  };

  const inactivo = deshabilitado || isPending;

  return (
    <div className="flex flex-col gap-2">
      <div
        onClick={() => !inactivo && inputRef.current?.click()}
        onDragOver={
          !inactivo
            ? (e) => {
                e.preventDefault();
                setArrastrando(true);
              }
            : undefined
        }
        onDragLeave={() => setArrastrando(false)}
        onDrop={
          !inactivo
            ? (e) => {
                e.preventDefault();
                setArrastrando(false);
                const file = e.dataTransfer.files[0];
                if (file) procesarArchivo(file);
              }
            : undefined
        }
        className={[
          'flex cursor-pointer flex-col items-center rounded-xl border-2 border-dashed px-4 py-5 text-center transition-colors',
          arrastrando ? 'border-blue-500 bg-blue-50' : 'border-gray-300 bg-white',
          inactivo ? 'cursor-not-allowed opacity-60' : 'hover:border-blue-400 hover:bg-slate-50',
        ].join(' ')}
      >
        <input
          ref={inputRef}
          type="file"
          accept={EXTENSIONES_LEGIBLES}
          onChange={(e) => {
            const file = e.target.files[0];
            if (file) procesarArchivo(file);
            e.target.value = '';
          }}
          className="hidden"
          aria-label={`Seleccionar ${titulo}`}
        />

        {archivo ? (
          <FileText className="mb-2 h-8 w-8 text-blue-600" aria-hidden="true" />
        ) : (
          <Upload className="mb-2 h-8 w-8 text-gray-400" aria-hidden="true" />
        )}

        {archivo ? (
          <div className="text-sm text-blue-800">
            <p className="font-semibold">{archivo.name}</p>
            <p className="text-xs text-gray-500">{(archivo.size / 1024).toFixed(0)} KB</p>
          </div>
        ) : (
          <div className="text-sm">
            <p className="font-semibold text-gray-700">Arrastra aquí o haz clic para seleccionar</p>
            <p className="text-xs text-gray-400">
              {EXTENSIONES_LEGIBLES} · máx. {maxMB} MB
            </p>
          </div>
        )}
      </div>

      {errorLocal && <p className="text-sm text-red-600">{errorLocal}</p>}

      {archivo && !deshabilitado && (
        <Button
          size="sm"
          className="self-start bg-blue-600 hover:bg-blue-700"
          onClick={() => archivo && onSubir?.(archivo)}
          disabled={isPending}
        >
          {isPending ? 'Subiendo...' : `Subir ${titulo}`}
        </Button>
      )}
    </div>
  );
}
