import { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Lock, Rocket } from 'lucide-react';
import { toast } from 'react-hot-toast';
import useAuthStore from '@/store/authStore';
import { useVinculacionDocumentos } from '../hooks/useVinculacionDocumentos';
import { useVinculacionMutaciones } from '../hooks/useVinculacionMutaciones';
import { usePermisos } from '../../../shared/hooks/usePermisos';
import PanelDocumento from '../components/PanelDocumento';
import ConfirmarFirmaModal from '../components/ConfirmarFirmaModal';
import ActivarPracticaModal from '../components/ActivarPracticaModal';
import { Button, PageHeader } from '@/shared/components/ui';

const ORDEN_TIPOS = ['HOJA_VIDA', 'CARTA', 'PROYECTO', 'CONVENIO'];

const ROL_A_FIRMANTE = {
  TUTOR_EMPRESARIAL: 'TUTOR_EMPRESARIAL',
  ESTUDIANTE: 'ESTUDIANTE',
};

function documentoCompleto(doc) {
  if (!doc || doc.estado === 'PENDIENTE') return false;
  if (doc.tipo === 'CONVENIO') return doc.estado === 'FIRMADO';
  return doc.estado === 'SUBIDO' || doc.estado === 'FIRMADO';
}

export default function VinculacionDetallePage() {
  const { asignacionId } = useParams();
  const navigate = useNavigate();
  const [firmaSeleccionada, setFirmaSeleccionada] = useState(null);
  const [modalActivarOpen, setModalActivarOpen] = useState(false);

  const { documentos, convenioId, detalle, isLoading, isError, refetch } =
    useVinculacionDocumentos(asignacionId);
  const { subirDocumento, confirmarFirma, activarPractica } = useVinculacionMutaciones({
    asignacionId,
    onSuccess: () => {
      setFirmaSeleccionada(null);
      setModalActivarOpen(false);
    },
  });
  const { canCreate } = usePermisos();
  const rol = useAuthStore((state) => state.rol);
  const tipoFirmanteRol = ROL_A_FIRMANTE[rol] ?? null;

  const documentosOrdenados = ORDEN_TIPOS.map(
    (tipo) => documentos.find((d) => d.tipo === tipo) ?? { tipo, estado: 'PENDIENTE', firmas: [] }
  );

  const completos = documentosOrdenados.filter(documentoCompleto).length;
  const totalDocumentos = documentosOrdenados.length;
  const puedeActivar = completos === totalDocumentos;

  const estudiante = detalle?.estudiante;
  const vacante = detalle?.vacante;

  const handleDescargar = async (documentoId, nombre) => {
    if (!documentoId) return;
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
      toast.error('No se pudo descargar el documento');
    }
  };

  if (isLoading) {
    return (
      <p className="py-16 text-center text-sm text-gray-500">Cargando documentos de la práctica...</p>
    );
  }

  if (isError) {
    return (
      <div className="mx-auto max-w-5xl">
        <Button variant="ghost" size="sm" className="mb-4" onClick={() => navigate('/vinculacion')}>
          ← Volver
        </Button>
        <div className="mb-4 rounded-lg border border-amber-200 bg-amber-50 p-4 text-sm text-amber-900">
          No se pudieron cargar los documentos guardados. Aun así puedes subir archivos nuevos.
          <Button variant="ghost" size="sm" className="ml-2" onClick={refetch}>
            Reintentar
          </Button>
        </div>
        <div className="mb-6 grid grid-cols-1 gap-4 md:grid-cols-2">
          {documentosOrdenados.map((documento) => (
            <PanelDocumento
              key={documento.tipo}
              documento={documento}
              asignacionId={asignacionId}
              onSubir={(archivo) => subirDocumento.mutate({ tipo: documento.tipo, archivo })}
              onDescargar={() => {}}
              onFirmar={() => {}}
              isPendingSubir={subirDocumento.isPending}
              puedeSubir={canCreate}
            />
          ))}
        </div>
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-5xl">
      <Button variant="ghost" size="sm" className="mb-4" onClick={() => navigate('/vinculacion')}>
        ← Volver
      </Button>

      <PageHeader
        titulo="Gestión de documentos"
        descripcion={
          estudiante
            ? `${estudiante.nombre} · ${vacante?.cargo ?? 'Práctica'} · ${vacante?.empresa ?? ''} · Tutor: ${detalle?.tutorEmpresarial || 'Pendiente'}`
            : `Asignación #${asignacionId} — Sube los documentos requeridos para activar la práctica.`
        }
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
          {completos}/{totalDocumentos} documentos completos
        </span>
        {puedeActivar && (
          <span className="text-xs font-semibold text-emerald-700">
            Todos los documentos están listos
          </span>
        )}
      </div>

      <div className="mb-6 grid grid-cols-1 gap-4 md:grid-cols-2">
        {documentosOrdenados.map((documento) => (
          <PanelDocumento
            key={documento.tipo}
            documento={documento}
            asignacionId={asignacionId}
            onSubir={(archivo) => subirDocumento.mutate({ tipo: documento.tipo, archivo })}
            onDescargar={() => handleDescargar(documento.id, documento.nombre)}
            onFirmar={(tipoFirmante) => setFirmaSeleccionada({ documento, tipoFirmante })}
            isPendingSubir={subirDocumento.isPending}
            isPendingFirma={confirmarFirma.isPending}
            puedeSubir={canCreate}
            tipoFirmanteRol={tipoFirmanteRol}
          />
        ))}
      </div>

      <div className="border-t border-gray-200 pt-5">
        {!puedeActivar && (
          <div className="mb-3 flex items-start gap-2 rounded-lg border border-amber-200 bg-amber-50 p-3 text-sm text-amber-900">
            <Lock className="mt-0.5 h-4 w-4 shrink-0" aria-hidden="true" />
            Para activar la práctica se requieren los cuatro documentos y las firmas del convenio.
          </div>
        )}
        <Button
          variant="success"
          disabled={!puedeActivar}
          onClick={() => setModalActivarOpen(true)}
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
          if (!firmaSeleccionada || !convenioId) return;
          confirmarFirma.mutate({
            convenioId,
            tipoFirmante: firmaSeleccionada.tipoFirmante,
          });
        }}
        isPending={confirmarFirma.isPending}
      />

      <ActivarPracticaModal
        isOpen={modalActivarOpen}
        onClose={() => setModalActivarOpen(false)}
        isPending={activarPractica.isPending}
        programaNombre={estudiante?.programa}
        onConfirmar={(payload) => {
          activarPractica.mutate({ practicaId: detalle.practicaId, payload });
        }}
      />
    </div>
  );
}
