import { useParams, useNavigate, useSearchParams } from 'react-router-dom';
import useCalificaciones from '../hooks/useCalificaciones';
import useCalificacionesMutaciones from '../hooks/useCalificacionesMutaciones';
import useEncuesta from '../hooks/useEncuesta';
import useCalificacionesPermisos from '../hooks/useCalificacionesPermisos';
import ResumenNotas from '../components/ResumenNotas';
import NotaForm from '../components/NotaForm';
import EncuestaForm from '../components/EncuestaForm';
import { obtenerNotasReferencia } from '../utils/schemas';
import { ErrorState, LoadingState, PageBackHeader } from '@/shared/components/ui';

function EncuestaPanel({
  isLoading,
  isError,
  refetch,
  encuestaHook,
  titulo,
  soloLectura,
  esEstudiante,
  puedeEnviarRecordatorio,
  puedeEnviarInvitacion,
}) {
  if (isLoading) {
    return <LoadingState mensaje={`Cargando ${titulo.toLowerCase()}...`} />;
  }

  if (isError) {
    return (
      <ErrorState
        mensaje={`No se pudo cargar ${titulo.toLowerCase()}`}
        onReintentar={refetch}
      />
    );
  }

  return (
    <EncuestaForm
      titulo={titulo}
      encuesta={encuestaHook.data}
      preguntas={encuestaHook.data?.preguntas}
      soloLectura={soloLectura}
      esEstudiante={esEstudiante}
      puedeEnviarRecordatorio={puedeEnviarRecordatorio}
      puedeEnviarInvitacion={puedeEnviarInvitacion}
      isGuardando={encuestaHook.guardarBorrador.isPending}
      isEnviando={encuestaHook.enviar.isPending}
      isRecordatorio={encuestaHook.enviarRecordatorio.isPending}
      isInvitacion={encuestaHook.enviarInvitacion.isPending}
      onGuardarBorrador={(respuestas) =>
        encuestaHook.guardarBorrador.mutate({
          encuestaId: encuestaHook.data.id,
          respuestas,
        })
      }
      onEnviar={(respuestas) =>
        encuestaHook.enviar.mutate({
          encuestaId: encuestaHook.data.id,
          respuestas,
        })
      }
      onRecordatorio={() => encuestaHook.enviarRecordatorio.mutate()}
      onInvitacion={() => encuestaHook.enviarInvitacion.mutate()}
    />
  );
}

export default function CalificacionesPage() {
  const { practicaId } = useParams();
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const permisos = useCalificacionesPermisos();

  const practicaActiva = searchParams.get('activa') !== 'false';

  const {
    data: resumen,
    isLoading,
    isError,
    error,
    refetch,
  } = useCalificaciones(practicaId, practicaActiva);

  const mutaciones = useCalificacionesMutaciones(practicaId);

  const encuestaTutor = useEncuesta(
    practicaId,
    'TUTOR',
    permisos.puedeVerEncuestaTutor
  );
  const encuestaEstudiante = useEncuesta(
    practicaId,
    'ESTUDIANTE',
    permisos.puedeVerEncuestaEstudiante
  );

  const muestraEncuestas =
    permisos.puedeVerEncuestaTutor || permisos.puedeVerEncuestaEstudiante;

  if (!permisos.puedeVerResumen) {
    return (
      <div className="p-4 sm:p-6">
        <ErrorState mensaje="No tienes permiso para acceder a las evaluaciones de esta práctica." />
      </div>
    );
  }

  if (isLoading) {
    return <LoadingState mensaje="Cargando evaluaciones..." />;
  }

  if (isError) {
    return (
      <div className="p-4 sm:p-6">
        <ErrorState
          mensaje={error?.response?.data?.message ?? error?.message ?? 'Error al cargar evaluaciones'}
          onReintentar={refetch}
        />
      </div>
    );
  }

  if (!resumen) {
    return (
      <div className="p-4 sm:p-6">
        <div className="rounded-xl border border-dashed border-gray-300 bg-gray-50 p-8 text-center text-sm text-gray-600">
          No se encontró información de evaluaciones para esta práctica.
        </div>
      </div>
    );
  }

  const referencia = obtenerNotasReferencia(resumen);
  const referenciasCompletas =
    referencia.notaDocente != null && referencia.notaTutor != null;

  const puedeRegistrarAlgunaNota =
    permisos.puedeRegistrarNotaDocente ||
    permisos.puedeRegistrarNotaTutor ||
    permisos.puedeRegistrarNotaFinal;

  const esEstudiante = permisos.rol === 'ESTUDIANTE';

  return (
    <div className="space-y-6 p-4 sm:p-6">
      <PageBackHeader
        titulo="Evaluaciones"
        descripcion={
          esEstudiante
            ? 'Consulta tus notas de referencia, nota final y encuesta de cierre'
            : `Práctica #${practicaId} — notas de referencia, nota final y encuestas de cierre`
        }
        onVolver={() => navigate(-1)}
      />

      <ResumenNotas resumen={resumen} />

      {muestraEncuestas && (
        <section className="space-y-4">
          <div>
            <h2 className="text-lg font-semibold text-gray-900">
              {esEstudiante ? 'Mi encuesta de cierre' : 'Encuestas de cierre'}
            </h2>
            <p className="mt-1 text-sm text-gray-600">
              {esEstudiante
                ? 'Completa tu autoevaluación. Si ya la enviaste verás el estado "Encuesta realizada".'
                : 'Requisito para el cierre formal (RF-08-05, RF-08-06, RF-09-01). El tutor puede guardar borrador; el estudiante debe completar la suya antes del cierre.'}
            </p>
          </div>

          <div className="grid grid-cols-1 gap-4 xl:grid-cols-2">
            {permisos.puedeVerEncuestaTutor && (
              <EncuestaPanel
                titulo="Encuesta — Tutor empresarial"
                encuestaHook={encuestaTutor}
                isLoading={encuestaTutor.isLoading}
                isError={encuestaTutor.isError}
                refetch={encuestaTutor.refetch}
                soloLectura={!permisos.puedeCompletarEncuestaTutor}
                esEstudiante={false}
                puedeEnviarRecordatorio={permisos.puedeEnviarRecordatorio}
                puedeEnviarInvitacion={permisos.puedeEnviarInvitacion}
              />
            )}

            {permisos.puedeVerEncuestaEstudiante && (
              <EncuestaPanel
                titulo="Autoevaluación — Estudiante"
                encuestaHook={encuestaEstudiante}
                isLoading={encuestaEstudiante.isLoading}
                isError={encuestaEstudiante.isError}
                refetch={encuestaEstudiante.refetch}
                soloLectura={!permisos.puedeCompletarEncuestaEstudiante}
                esEstudiante
                puedeEnviarRecordatorio={permisos.puedeEnviarRecordatorio}
                puedeEnviarInvitacion={permisos.puedeEnviarInvitacion}
              />
            )}
          </div>
        </section>
      )}

      {puedeRegistrarAlgunaNota && (
        <section className="space-y-4">
          <div>
            <h2 className="text-lg font-semibold text-gray-900">Registro de notas</h2>
            <p className="mt-1 text-sm text-gray-600">
              El docente asesor y el tutor registran notas de referencia. El docente asesor registra
              la nota final definitiva.
            </p>
          </div>

          <div className="grid grid-cols-1 gap-4 lg:grid-cols-2">
          {permisos.puedeRegistrarNotaDocente && (
            <NotaForm
              titulo="Nota de referencia — Docente asesor"
              descripcion="Calificación de referencia sobre el desempeño del estudiante."
              tipo="docente"
              notaExistente={referencia.notaDocente}
              practicaActiva={practicaActiva}
              isPending={mutaciones.registrarNotaDocente.isPending}
              onSubmit={(datos) =>
                mutaciones.registrarNotaDocente.mutate({
                  ...datos,
                  corte: referencia.corte,
                })
              }
            />
          )}

          {permisos.puedeRegistrarNotaTutor && (
            <NotaForm
              titulo="Nota de referencia — Tutor empresarial"
              descripcion="Calificación de referencia desde la empresa."
              tipo="tutor"
              notaExistente={referencia.notaTutor}
              practicaActiva={practicaActiva}
              isPending={mutaciones.registrarNotaTutor.isPending}
              onSubmit={(datos) =>
                mutaciones.registrarNotaTutor.mutate({
                  ...datos,
                  corte: referencia.corte,
                })
              }
            />
          )}

          {permisos.puedeRegistrarNotaFinal && (
            <div className="lg:col-span-2">
              {!referenciasCompletas ? (
                <div className="rounded-xl border border-amber-200 bg-amber-50 p-4 text-sm text-amber-800">
                  La nota final estará disponible cuando existan las dos notas de referencia
                  (docente y tutor).
                </div>
              ) : (
                <NotaForm
                  titulo="Nota final definitiva — Docente asesor"
                  descripcion="Nota oficial que determina la aprobación o reprobación. El cierre formal lo ejecuta el coordinador."
                  tipo="final"
                  notaExistente={resumen.notaFinal}
                  practicaActiva={practicaActiva}
                  isPending={mutaciones.registrarNotaFinal.isPending}
                  onSubmit={(datos) => mutaciones.registrarNotaFinal.mutate(datos)}
                />
              )}
            </div>
          )}
          </div>
        </section>
      )}
    </div>
  );
}
