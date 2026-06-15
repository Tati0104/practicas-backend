import { BookMarked, GraduationCap, Star, Trophy } from 'lucide-react';
import { NOTA_MINIMA_APROBACION, obtenerNotasReferencia, obtenerResultadoEstimado } from '../utils/schemas';

function ResultadoBadge({ resultado, esDefinitivo }) {
  if (!resultado) {
    return (
      <span className="inline-flex rounded-full bg-gray-100 px-2.5 py-0.5 text-xs font-semibold text-gray-600">
        Sin calcular
      </span>
    );
  }

  const aprobada = resultado === 'APROBADA';
  return (
    <span
      className={`inline-flex rounded-full px-2.5 py-0.5 text-xs font-semibold ${
        aprobada ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'
      }`}
    >
      {aprobada ? 'APROBADA' : 'REPROBADA'}
      {!esDefinitivo && ' (estimado)'}
    </span>
  );
}

function NotaCard({ titulo, nota, observacion, icono: Icono, subtitulo }) {
  return (
    <div className="rounded-xl border border-gray-200 bg-white p-4 shadow-sm">
      <div className="mb-3 flex items-start justify-between gap-2">
        <div className="flex items-center gap-2">
          <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-blue-50 text-blue-700">
            <Icono className="h-4 w-4" aria-hidden="true" />
          </div>
          <div>
            <h3 className="text-sm font-semibold text-gray-900">{titulo}</h3>
            {subtitulo && <p className="text-xs text-gray-500">{subtitulo}</p>}
          </div>
        </div>
        {nota != null ? (
          <span className="text-2xl font-bold text-gray-900">{nota.toFixed(1)}</span>
        ) : (
          <span className="text-sm font-medium text-gray-400">Pendiente</span>
        )}
      </div>
      {observacion && (
        <p className="text-sm text-gray-600">
          <span className="font-medium text-gray-700">Observación: </span>
          {observacion}
        </p>
      )}
    </div>
  );
}

export default function ResumenNotas({ resumen }) {
  const referencia = obtenerNotasReferencia(resumen);
  const resultado = obtenerResultadoEstimado(resumen);
  const esDefinitivo = resumen?.notaFinal != null;

  return (
    <section aria-labelledby="resumen-notas-titulo">
      <h2 id="resumen-notas-titulo" className="mb-4 text-lg font-semibold text-gray-900">
        Resumen de evaluaciones
      </h2>

      <div className="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-4">
        <NotaCard
          titulo="Nota docente"
          subtitulo="Referencia"
          nota={referencia.notaDocente}
          observacion={referencia.obsDocente}
          icono={GraduationCap}
        />
        <NotaCard
          titulo="Nota tutor"
          subtitulo="Referencia"
          nota={referencia.notaTutor}
          observacion={referencia.obsTutor}
          icono={BookMarked}
        />
        <NotaCard
          titulo="Nota final"
          subtitulo="Definitiva"
          nota={resumen?.notaFinal ?? null}
          icono={Star}
        />
        <div className="rounded-xl border border-gray-200 bg-white p-4 shadow-sm">
          <div className="mb-3 flex items-center gap-2">
            <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-blue-50 text-blue-700">
              <Trophy className="h-4 w-4" aria-hidden="true" />
            </div>
            <div>
              <h3 className="text-sm font-semibold text-gray-900">Resultado</h3>
              <p className="text-xs text-gray-500">
                Mínimo aprobación: {NOTA_MINIMA_APROBACION.toFixed(1)}
              </p>
            </div>
          </div>
          <ResultadoBadge resultado={resultado} esDefinitivo={esDefinitivo} />
          {!esDefinitivo && resumen?.promedioEstimado > 0 && (
            <p className="mt-2 text-sm text-gray-600">
              Promedio estimado:{' '}
              <span className="font-semibold text-gray-900">
                {resumen.promedioEstimado.toFixed(1)}
              </span>
            </p>
          )}
        </div>
      </div>

      {resumen?.notasCortes?.length > 1 && (
        <div className="mt-6 overflow-x-auto rounded-xl border border-gray-200 bg-white">
          <table className="min-w-full text-sm">
            <thead className="bg-blue-700 text-left text-white">
              <tr>
                <th scope="col" className="px-4 py-3 font-semibold">
                  Corte
                </th>
                <th scope="col" className="px-4 py-3 font-semibold">
                  Docente
                </th>
                <th scope="col" className="px-4 py-3 font-semibold">
                  Tutor
                </th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-200">
              {resumen.notasCortes.map((corte) => (
                <tr key={corte.corte}>
                  <td className="px-4 py-3 font-medium text-gray-900">{corte.corte}</td>
                  <td className="px-4 py-3 text-gray-700">
                    {corte.notaDocente != null ? corte.notaDocente.toFixed(1) : '—'}
                  </td>
                  <td className="px-4 py-3 text-gray-700">
                    {corte.notaTutor != null ? corte.notaTutor.toFixed(1) : '—'}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </section>
  );
}
