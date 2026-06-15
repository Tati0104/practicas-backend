import { Download, PenLine, Paperclip } from 'lucide-react';
import BadgeDocumento from './BadgeDocumento';
import ProgresoFirmas from './ProgresoFirmas';
import SubirDocumentoDropzone from './SubirDocumentoDropzone';
import { Button, Card } from '@/shared/components/ui';

const TITULOS = {
  CARTA: 'Carta de Presentación',
  CONVENIO: 'Convenio de Práctica',
};

export default function PanelDocumento({
  documento,
  asignacionId,
  onSubir,
  onDescargar,
  onFirmar,
  isPendingSubir = false,
  isPendingFirma = false,
  puedeSubir = false,
  tipoFirmanteRol = null,
}) {
  const tieneArchivo = documento.estado !== 'PENDIENTE';
  const firmaDelRol = tipoFirmanteRol
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

      {tieneArchivo && documento.nombre && (
        <p className="flex items-center gap-1.5 break-all text-xs text-gray-600">
          <Paperclip className="h-3.5 w-3.5 shrink-0" aria-hidden="true" />
          {documento.nombre}
        </p>
      )}

      {tieneArchivo && (
        <Button variant="info" size="sm" className="self-start" onClick={onDescargar}>
          <Download className="h-3.5 w-3.5" aria-hidden="true" />
          Descargar
        </Button>
      )}

      {puedeSubir && !tieneArchivo && (
        <SubirDocumentoDropzone
          titulo={TITULOS[documento.tipo]}
          onSubir={(archivo) => onSubir(asignacionId, archivo)}
          isPending={isPendingSubir}
          deshabilitado={tieneArchivo}
        />
      )}

      <hr className="border-gray-100" />

      <ProgresoFirmas firmas={documento.firmas || []} />

      {firmaDelRol && tieneArchivo && (
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
    </Card>
  );
}
