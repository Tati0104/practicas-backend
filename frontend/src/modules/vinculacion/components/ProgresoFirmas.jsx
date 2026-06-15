import { Check, Circle } from 'lucide-react';

const ETIQUETAS = {
  DOCENTE_ASESOR: 'Docente',
  TUTOR_EMPRESARIAL: 'Tutor',
};

export default function ProgresoFirmas({ firmas = [] }) {
  const totalFirmadas = firmas.filter((f) => f.firmado).length;
  const total = firmas.length;

  return (
    <div className="flex flex-col gap-2">
      <p className="text-xs font-semibold text-gray-700">
        {totalFirmadas}/{total} firmas
      </p>
      <div className="flex gap-2">
        {firmas.map((f) => (
          <div key={f.tipoFirmante} className="flex flex-col items-center gap-1">
            <div
              title={
                f.firmado
                  ? `Firmado el ${new Date(f.fechaFirma).toLocaleDateString('es-CO')}`
                  : 'Pendiente de firma'
              }
              className={[
                'flex h-7 w-7 items-center justify-center rounded-full border-2',
                f.firmado
                  ? 'border-emerald-600 bg-emerald-50 text-emerald-700'
                  : 'border-gray-300 bg-gray-50 text-gray-400',
              ].join(' ')}
            >
              {f.firmado ? (
                <Check className="h-3.5 w-3.5" aria-hidden="true" />
              ) : (
                <Circle className="h-3 w-3" aria-hidden="true" />
              )}
            </div>
            <span className="text-[10px] text-gray-500">
              {ETIQUETAS[f.tipoFirmante] || f.tipoFirmante}
            </span>
          </div>
        ))}
      </div>
    </div>
  );
}
