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
        <ErrorState mensaje="No tienes permiso para acceder a las calificaciones de esta práctica." />
      </div>
    );
  }

  if (isLoading) {
    return <LoadingState mensaje="Cargando calificaciones..." />;
  }

  if (isError) {
    return (
      <div className="p-4 sm:p-6">
        <ErrorState
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
      <PageBackHeader
        titulo="Calificaciones"
        descripcion={`Práctica #${practicaId}`}
        onVolver={() => navigate(-1)}
      />

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
                  <LoadingState mensaje="Cargando encuesta del tutor..." />
                ) : encuestaTutor.isError ? (
                  <ErrorState
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
                  <LoadingState mensaje="Cargando encuesta del estudiante..." />
                ) : encuestaEstudiante.isError ? (
                  <ErrorState
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
