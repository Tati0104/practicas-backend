// src/modules/vinculacion/pages/VinculacionDetallePage.jsx

import { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Lock, Rocket } from 'lucide-react';
import { toast } from 'react-hot-toast';
import { useVinculacionDocumentos } from '../hooks/useVinculacionDocumentos';
import { useVinculacionMutaciones } from '../hooks/useVinculacionMutaciones';
import { usePermisos } from '../../../shared/hooks/usePermisos';
import PanelDocumento from '../components/PanelDocumento';
import ConfirmarFirmaModal from '../components/ConfirmarFirmaModal';
import { Button, PageHeader } from '@/shared/components/ui';

export default function VinculacionDetallePage() {
  const { practicaId } = useParams();
  const navigate = useNavigate();
  const [firmaSeleccionada, setFirmaSeleccionada] = useState(null);

  const { documentos, isLoading, isError, refetch } = useVinculacionDocumentos(practicaId);
  const { subirCarta, subirConvenio, confirmarFirma } = useVinculacionMutaciones({
    practicaId,
    onSuccess: () => {
      refetch();
      setFirmaSeleccionada(null);
    },
  });
  const { canCreate } = usePermisos();

  const documentosFirmados = documentos.filter((d) => d.estado === 'FIRMADO').length;
  const totalDocumentos = documentos.length;
  const puedeActivar = totalDocumentos > 0 && documentosFirmados === totalDocumentos;

  const carta = documentos.find((d) => d.tipo === 'CARTA');
  const convenio = documentos.find((d) => d.tipo === 'CONVENIO');

  const handleSubir = (tipo, asignacionId, archivo) => {
    if (tipo === 'CARTA') {
      subirCarta.mutate({ asignacionId, archivo });
    } else {
      subirConvenio.mutate({ asignacionId, archivo });
    }
  };

  const handleDescargar = async (documentoId, nombre) => {
    try {
      const { default: svc } = await import('../services/vinculacionService');
      const resp = await svc.descargarDocumento(documentoId);
      const url = URL.createObjectURL(resp.data);
      const link = document.createElement('a');
      link.href = url;
      link.download = nombre || `documento_${documentoId}.pdf`;
      link.click();
      URL.revokeObjectURL(url);
    } catch {
      // toast en servicio
    }
  };

  if (isLoading) {
    return (
      <p className="py-16 text-center text-sm text-gray-500">Cargando documentos de la práctica...</p>
    );
  }

  if (isError) {
    return (
      <div className="mx-auto max-w-lg py-16 text-center">
        <div className="rounded-lg border border-red-200 bg-red-50 p-6 text-red-800">
          <p className="mb-4">No se pudieron cargar los documentos. Intenta de nuevo.</p>
          <Button variant="ghost" size="sm" onClick={refetch}>
            Reintentar
          </Button>
        </div>
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-3xl">
      <Button variant="ghost" size="sm" className="mb-4" onClick={() => navigate('/vinculacion')}>
        ← Volver
      </Button>

      <PageHeader
        titulo="Gestión de documentos"
        descripcion={`Práctica #${practicaId} — Sube y firma los documentos requeridos para activar la práctica.`}
      />

      <div
        className={[
          'mb-5 flex flex-wrap items-center gap-3 rounded-lg border px-4 py-3',
          puedeActivar ? 'border-emerald-200 bg-emerald-50' : 'border-gray-200 bg-slate-50',
        ].join(' ')}
      >
        <span
          className={`text-sm font-bold ${puedeActivar ? 'text-emerald-700' : 'text-gray-700'}`}
        >
          {documentosFirmados}/{totalDocumentos} documentos firmados
        </span>
        {puedeActivar && (
          <span className="text-xs font-semibold text-emerald-700">
            Todos los documentos están firmados
          </span>
        )}
      </div>

      <div className="mb-6 grid grid-cols-1 gap-4 md:grid-cols-2">
        {carta ? (
          <PanelDocumento
            documento={carta}
            asignacionId={practicaId}
            onSubir={(asignacionId, archivo) => handleSubir('CARTA', asignacionId, archivo)}
            onDescargar={() => handleDescargar(carta.id, carta.nombre)}
            onFirmar={(tipoFirmante) => setFirmaSeleccionada({ documento: carta, tipoFirmante })}
            isPendingSubir={subirCarta.isPending}
            isPendingFirma={confirmarFirma.isPending}
            puedeSubir={canCreate}
            tipoFirmanteRol="COORDINADOR"
          />
        ) : (
          <div className="rounded-xl border border-dashed border-gray-300 p-6 text-center text-sm text-gray-400">
            Carta de Presentación — no disponible aún
          </div>
        )}

        {convenio ? (
          <PanelDocumento
            documento={convenio}
            asignacionId={practicaId}
            onSubir={(asignacionId, archivo) => handleSubir('CONVENIO', asignacionId, archivo)}
            onDescargar={() => handleDescargar(convenio.id, convenio.nombre)}
            onFirmar={(tipoFirmante) => setFirmaSeleccionada({ documento: convenio, tipoFirmante })}
            isPendingSubir={subirConvenio.isPending}
            isPendingFirma={confirmarFirma.isPending}
            puedeSubir={canCreate}
            tipoFirmanteRol="COORDINADOR"
          />
        ) : (
          <div className="rounded-xl border border-dashed border-gray-300 p-6 text-center text-sm text-gray-400">
            Convenio de Práctica — no disponible aún
          </div>
        )}
      </div>

      <div className="border-t border-gray-200 pt-5">
        {!puedeActivar && (
          <div className="mb-3 flex items-start gap-2 rounded-lg border border-amber-200 bg-amber-50 p-3 text-sm text-amber-900">
            <Lock className="mt-0.5 h-4 w-4 shrink-0" aria-hidden="true" />
            Para activar la práctica se requieren todas las firmas en ambos documentos.
          </div>
        )}
        <Button
          variant="success"
          disabled={!puedeActivar}
          onClick={() => toast.success('Práctica activada exitosamente')}
        >
          <Rocket className="h-4 w-4" aria-hidden="true" />
          Activar práctica
        </Button>
      </div>

      <ConfirmarFirmaModal
        isOpen={!!firmaSeleccionada}
        documento={firmaSeleccionada?.documento}
        tipoFirmante={firmaSeleccionada?.tipoFirmante}
        onClose={() => setFirmaSeleccionada(null)}
        onConfirmar={() => {
          if (!firmaSeleccionada) return;
          confirmarFirma.mutate({
            convenioId: firmaSeleccionada.documento.id,
            tipoFirmante: firmaSeleccionada.tipoFirmante,
          });
        }}
        isPending={confirmarFirma.isPending}
      />
    </div>
  );
}
