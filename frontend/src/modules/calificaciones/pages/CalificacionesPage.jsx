import { useParams, useNavigate, useSearchParams } from 'react-router-dom';
import { ArrowLeft, Loader2, RefreshCw } from 'lucide-react';
import useCalificaciones from '../hooks/useCalificaciones';
import useCalificacionesMutaciones from '../hooks/useCalificacionesMutaciones';
import useEncuesta from '../hooks/useEncuesta';
import useCalificacionesPermisos from '../hooks/useCalificacionesPermisos';
import ResumenNotas from '../components/ResumenNotas';
import NotaForm from '../components/NotaForm';
import EncuestaForm from '../components/EncuestaForm';
import { obtenerNotasReferencia } from '../utils/schemas';

function Spinner({ mensaje }) {
  return (
    <div className="flex flex-col items-center justify-center gap-3 p-12 text-gray-600">
      <Loader2 className="h-8 w-8 animate-spin text-blue-700" aria-hidden="true" />
      <p className="text-sm">{mensaje}</p>
    </div>
  );
}

function ErrorEstado({ mensaje, onReintentar }) {
  return (
    <div className="rounded-xl border border-red-200 bg-red-50 p-6 text-center">
      <p className="text-sm text-red-700" role="alert">
        {mensaje}
      </p>
      {onReintentar && (
        <button
          type="button"
          onClick={onReintentar}
          className="mt-4 inline-flex items-center gap-2 rounded-lg bg-red-600 px-4 py-2 text-sm font-semibold text-white hover:bg-red-700"
        >
          <RefreshCw className="h-4 w-4" aria-hidden="true" />
          Reintentar
        </button>
      )}
    </div>
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

  if (!permisos.puedeVerResumen) {
    return (
      <div className="p-4 sm:p-6">
        <ErrorEstado mensaje="No tienes permiso para acceder a las calificaciones de esta práctica." />
      </div>
    );
  }

  if (isLoading) {
    return <Spinner mensaje="Cargando calificaciones..." />;
  }

  if (isError) {
    return (
      <div className="p-4 sm:p-6">
        <ErrorEstado
          mensaje={error?.response?.data?.message ?? error?.message ?? 'Error al cargar calificaciones'}
          onReintentar={refetch}
        />
      </div>
    );
  }

  if (!resumen) {
    return (
      <div className="p-4 sm:p-6">
        <div className="rounded-xl border border-dashed border-gray-300 bg-gray-50 p-8 text-center text-sm text-gray-600">
          No se encontró información de calificaciones para esta práctica.
        </div>
      </div>
    );
  }

  const referencia = obtenerNotasReferencia(resumen);
  const referenciasCompletas =
    referencia.notaDocente != null && referencia.notaTutor != null;

  return (
    <div className="space-y-6 p-4 sm:p-6">
      <header className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <button
            type="button"
            onClick={() => navigate(-1)}
            className="mb-2 inline-flex items-center gap-1 text-sm font-medium text-blue-700 hover:underline"
          >
            <ArrowLeft className="h-4 w-4" aria-hidden="true" />
            Volver
          </button>
          <h1 className="text-2xl font-bold text-gray-900">Calificaciones</h1>
          <p className="text-sm text-gray-500">Práctica #{practicaId}</p>
        </div>
      </header>

      <ResumenNotas resumen={resumen} />

      <section className="space-y-4">
        <h2 className="text-lg font-semibold text-gray-900">Registro de notas</h2>

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
                  titulo="Nota final definitiva"
                  descripcion="Nota oficial que determina la aprobación o reprobación de la práctica."
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

      {(permisos.puedeVerEncuestaTutor || permisos.puedeVerEncuestaEstudiante) && (
        <section className="space-y-4">
          <h2 className="text-lg font-semibold text-gray-900">Encuestas de satisfacción</h2>

          <div className="grid grid-cols-1 gap-4 xl:grid-cols-2">
            {permisos.puedeVerEncuestaTutor && (
              <>
                {encuestaTutor.isLoading ? (
                  <Spinner mensaje="Cargando encuesta del tutor..." />
                ) : encuestaTutor.isError ? (
                  <ErrorEstado
                    mensaje="No se pudo cargar la encuesta del tutor"
                    onReintentar={encuestaTutor.refetch}
                  />
                ) : (
                  <EncuestaForm
                    titulo="Encuesta — Tutor empresarial"
                    encuesta={encuestaTutor.data}
                    preguntas={encuestaTutor.data?.preguntas}
                    soloLectura={!permisos.puedeCompletarEncuestaTutor}
                    puedeEnviarRecordatorio={permisos.puedeEnviarRecordatorio}
                    isGuardando={encuestaTutor.guardarBorrador.isPending}
                    isEnviando={encuestaTutor.enviar.isPending}
                    isRecordatorio={encuestaTutor.enviarRecordatorio.isPending}
                    onGuardarBorrador={(respuestas) =>
                      encuestaTutor.guardarBorrador.mutate({
                        encuestaId: encuestaTutor.data.id,
                        respuestas,
                      })
                    }
                    onEnviar={(respuestas) =>
                      encuestaTutor.enviar.mutate({
                        encuestaId: encuestaTutor.data.id,
                        respuestas,
                      })
                    }
                    onRecordatorio={() => encuestaTutor.enviarRecordatorio.mutate()}
                  />
                )}
              </>
            )}

            {permisos.puedeVerEncuestaEstudiante && (
              <>
                {encuestaEstudiante.isLoading ? (
                  <Spinner mensaje="Cargando encuesta del estudiante..." />
                ) : encuestaEstudiante.isError ? (
                  <ErrorEstado
                    mensaje="No se pudo cargar la encuesta del estudiante"
                    onReintentar={encuestaEstudiante.refetch}
                  />
                ) : (
                  <EncuestaForm
                    titulo="Encuesta — Estudiante"
                    encuesta={encuestaEstudiante.data}
                    preguntas={encuestaEstudiante.data?.preguntas}
                    soloLectura={!permisos.puedeCompletarEncuestaEstudiante}
                    puedeEnviarRecordatorio={permisos.puedeEnviarRecordatorio}
                    isGuardando={encuestaEstudiante.guardarBorrador.isPending}
                    isEnviando={encuestaEstudiante.enviar.isPending}
                    isRecordatorio={encuestaEstudiante.enviarRecordatorio.isPending}
                    onGuardarBorrador={(respuestas) =>
                      encuestaEstudiante.guardarBorrador.mutate({
                        encuestaId: encuestaEstudiante.data.id,
                        respuestas,
                      })
                    }
                    onEnviar={(respuestas) =>
                      encuestaEstudiante.enviar.mutate({
                        encuestaId: encuestaEstudiante.data.id,
                        respuestas,
                      })
                    }
                    onRecordatorio={() => encuestaEstudiante.enviarRecordatorio.mutate()}
                  />
                )}
              </>
            )}
          </div>
        </section>
      )}
    </div>
  );
}
