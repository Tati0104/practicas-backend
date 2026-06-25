import { Download, PenLine, Paperclip } from 'lucide-react';
import BadgeDocumento from './BadgeDocumento';
import ProgresoFirmas from './ProgresoFirmas';
import SubirDocumentoDropzone from './SubirDocumentoDropzone';
import { Button, Card } from '@/shared/components/ui';

const TITULOS = {
  HOJA_VIDA: 'Hoja de vida',
  CARTA: 'Carta de presentación',
  PROYECTO: 'Documento del proyecto práctica',
  CONVENIO: 'Convenio de práctica',
};

export default function PanelDocumento({
  documento,
  onSubir,
  onDescargar,
  onFirmar,
  isPendingSubir = false,
  isPendingFirma = false,
  puedeSubir = false,
  tipoFirmanteRol = null,
}) {
  const archivoSubido = Boolean(documento.id);
  const requiereFirmas = documento.tipo === 'CONVENIO';
  const firmaDelRol =
    requiereFirmas && tipoFirmanteRol
      ? documento.firmas?.find((f) => f.tipoFirmante === tipoFirmanteRol && !f.firmado)
      : null;

  return (
    <Card padding="p-5" className="flex flex-col gap-3">
      <div className="flex items-center justify-between gap-2">
        <h3 className="text-base font-bold text-gray-900">
          {TITULOS[documento.tipo] || documento.tipo}
        </h3>
        <BadgeDocumento estado={documento.estado} />
      </div>

      {archivoSubido && documento.nombre && (
        <p className="flex items-center gap-1.5 break-all text-xs text-gray-600">
          <Paperclip className="h-3.5 w-3.5 shrink-0" aria-hidden="true" />
          {documento.nombre}
        </p>
      )}

      {archivoSubido && (
        <Button variant="info" size="sm" className="self-start" onClick={onDescargar}>
          <Download className="h-3.5 w-3.5" aria-hidden="true" />
          Descargar
        </Button>
      )}

      {puedeSubir && !archivoSubido && (
        <SubirDocumentoDropzone
          titulo={TITULOS[documento.tipo]}
          onSubir={(archivo) => onSubir(archivo)}
          isPending={isPendingSubir}
        />
      )}

      {!puedeSubir && !archivoSubido && (
        <p className="rounded-lg border border-dashed border-gray-200 bg-slate-50 px-3 py-4 text-center text-sm text-gray-500">
          Documento pendiente de carga por el coordinador.
        </p>
      )}

      {requiereFirmas && (
        <>
          <hr className="border-gray-100" />
          <ProgresoFirmas firmas={documento.firmas || []} />
          {firmaDelRol && archivoSubido && (
            <Button
              variant="success"
              size="sm"
              className="self-start"
              onClick={() => onFirmar(tipoFirmanteRol)}
              disabled={isPendingFirma}
            >
              <PenLine className="h-3.5 w-3.5" aria-hidden="true" />
              {isPendingFirma ? 'Procesando...' : 'Confirmar mi firma'}
            </Button>
          )}
        </>
      )}
    </Card>
  );
}
