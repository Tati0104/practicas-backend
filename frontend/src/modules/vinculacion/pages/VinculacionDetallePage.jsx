import { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { Lock, Rocket } from 'lucide-react';
import { toast } from 'react-hot-toast';
import useAuthStore from '@/store/authStore';
import { useVinculacionDocumentos } from '../hooks/useVinculacionDocumentos';
import { useVinculacionMutaciones } from '../hooks/useVinculacionMutaciones';
import { usePermisos } from '../../../shared/hooks/usePermisos';
import PanelDocumento from '../components/PanelDocumento';
import ConfirmarFirmaModal from '../components/ConfirmarFirmaModal';
import ActivarPracticaModal from '../components/ActivarPracticaModal';
import { Button, PageHeader, Card } from '@/shared/components/ui';
import docentesAsesoresService from '../../docentes/services/docentesAsesoresService';

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
  const { subirDocumento, confirmarFirma, activarPractica, asignarDocenteAsesor } = useVinculacionMutaciones({
    asignacionId,
    onSuccess: () => {
      setFirmaSeleccionada(null);
      setModalActivarOpen(false);
    },
  });
  const { canUploadDocumentos } = usePermisos();
  const rol = useAuthStore((state) => state.rol);
  const esTutor = rol === 'TUTOR_EMPRESARIAL';
  const isCoordinadorAcademico = rol === 'COORD_PRACTICA' || rol === 'ADMIN';

  const { data: docentes = [] } = useQuery({
    queryKey: ['docentes-asesores-activos'],
    queryFn: () => docentesAsesoresService.listarPorPrograma().then((res) => res.filter((d) => d.activo)),
    enabled: !!isCoordinadorAcademico,
  });
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
          No se pudieron cargar los documentos guardados.
          {!esTutor && ' Aun así puedes subir archivos nuevos.'}
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
              puedeSubir={canUploadDocumentos}
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
        titulo={esTutor ? 'Documentos y firma de convenio' : 'Gestión de documentos'}
        descripcion={
          estudiante
            ? `${estudiante.nombre} · ${vacante?.cargo ?? 'Práctica'} · ${vacante?.empresa ?? ''} · Tutor: ${detalle?.tutorEmpresarial || 'Pendiente'}`
            : esTutor
              ? `Asignación #${asignacionId} — Revisa los documentos y firma el convenio cuando esté listo.`
              : `Asignación #${asignacionId} — Sube los documentos requeridos para activar la práctica.`
        }
      />

      <div className="mb-6 rounded-lg border border-gray-200 bg-white p-5 shadow-sm">
        <h3 className="mb-4 text-lg font-semibold text-gray-800">Detalles de la Asignación</h3>
        <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
          <div>
            <p className="text-sm font-medium text-gray-500">Estudiante</p>
            <p className="text-sm font-semibold text-gray-900">{estudiante?.nombre || '-'}</p>
          </div>
          <div>
            <p className="text-sm font-medium text-gray-500">Empresa / Vacante</p>
            <p className="text-sm font-semibold text-gray-900">
              {vacante?.empresa || '-'} / {vacante?.cargo || '-'}
            </p>
          </div>
          <div>
            <p className="text-sm font-medium text-gray-500">Tutor Empresarial</p>
            <p className="text-sm font-semibold text-gray-900">{detalle?.tutorEmpresarial || '-'}</p>
          </div>
          <div>
            <p className="text-sm font-medium text-gray-500">Fechas</p>
            <p className="text-sm font-semibold text-gray-900">
              {detalle?.fechaInicio || 'Pendiente'} al {detalle?.fechaFin || 'Pendiente'}
            </p>
          </div>
          <div>
            <p className="text-sm font-medium text-gray-500">Estado de Vinculación</p>
            <p className="text-sm font-semibold text-gray-900">{detalle?.estadoVinculacion || 'ASIGNADA'}</p>
          </div>
          <div>
            <p className="text-sm font-medium text-gray-500 mb-1">Docente Asesor</p>
            {isCoordinadorAcademico ? (
              <select
                className="block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm p-1.5 border"
                value={detalle?.docenteAsesorId || ''}
                onChange={(e) => {
                  const val = e.target.value;
                  if (val) asignarDocenteAsesor.mutate({ asignacionId, docenteAsesorId: Number(val) });
                }}
                disabled={asignarDocenteAsesor.isPending}
              >
                <option value="">Seleccione un docente...</option>
                {docentes.map((d) => (
                  <option key={d.id} value={d.id}>
                    {d.nombreCompleto || d.nombre}
                  </option>
                ))}
              </select>
            ) : (
              <p className="text-sm font-semibold text-gray-900">
                {detalle?.docenteAsesorId
                  ? docentes.find((d) => d.id === detalle.docenteAsesorId)?.nombre || `Docente #${detalle.docenteAsesorId}`
                  : 'Pendiente'}
              </p>
            )}
          </div>
        </div>
      </div>

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
            puedeSubir={canUploadDocumentos}
            tipoFirmanteRol={tipoFirmanteRol}
          />
        ))}
      </div>

      <div className="border-t border-gray-200 pt-5">
        {!esTutor && (
          <>
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
          </>
        )}
        {esTutor && (
          <p className="text-sm text-gray-600">
            Como tutor empresarial puedes revisar los documentos y registrar tu firma en el convenio.
            La activación de la práctica la realiza el coordinador.
          </p>
        )}
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
        isOpen={modalActivarOpen && !esTutor}
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
