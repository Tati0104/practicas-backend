import { useEffect, useMemo, useState } from 'react';
import { Loader2, Send } from 'lucide-react';
import EstadoEncuestaBadge from './EstadoEncuestaBadge';

const ESCALA_OPCIONES = [1, 2, 3, 4, 5];

function validarRespuestas(preguntas, respuestas) {
  const errores = {};

  preguntas.forEach((pregunta) => {
    const valor = respuestas[pregunta.id];

    if (!pregunta.requerida) return;

    if (pregunta.tipo === 'ESCALA') {
      if (!valor || valor < 1 || valor > 5) {
        errores[pregunta.id] = 'Selecciona una calificación del 1 al 5';
      }
      return;
    }

    if (!valor || !String(valor).trim()) {
      errores[pregunta.id] = 'Este campo es obligatorio';
    }
  });

  return errores;
}

export default function EncuestaForm({
  titulo,
  encuesta,
  preguntas = [],
  soloLectura = false,
  puedeEnviarRecordatorio = false,
  onGuardarBorrador,
  onEnviar,
  onRecordatorio,
  isGuardando = false,
  isEnviando = false,
  isRecordatorio = false,
}) {
  const [respuestas, setRespuestas] = useState({});
  const [errores, setErrores] = useState({});

  const bloqueada = encuesta?.estado === 'COMPLETADA' || soloLectura;

  useEffect(() => {
    if (encuesta?.respuestas) {
      setRespuestas(encuesta.respuestas);
    }
  }, [encuesta?.respuestas, encuesta?.id]);

  const preguntasVisibles = useMemo(() => preguntas ?? [], [preguntas]);

  const actualizarRespuesta = (id, valor) => {
    setRespuestas((prev) => ({ ...prev, [id]: valor }));
    setErrores((prev) => {
      const next = { ...prev };
      delete next[id];
      return next;
    });
  };

  const handleBorrador = () => {
    onGuardarBorrador?.(respuestas);
  };

  const handleEnviar = () => {
    const validacion = validarRespuestas(preguntasVisibles, respuestas);
    setErrores(validacion);
    if (Object.keys(validacion).length > 0) return;
    onEnviar?.(respuestas);
  };

  if (!encuesta) {
    return (
      <div className="rounded-xl border border-gray-200 bg-white p-4 text-sm text-gray-500">
        Encuesta no disponible.
      </div>
    );
  }

  return (
    <section
      className="rounded-xl border border-gray-200 bg-white p-4 shadow-sm"
      aria-labelledby={`encuesta-${encuesta.tipo}`}
    >
      <div className="mb-4 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h3 id={`encuesta-${encuesta.tipo}`} className="text-base font-semibold text-gray-900">
            {titulo}
          </h3>
          <p className="text-sm text-gray-500">Tipo: {encuesta.tipo}</p>
        </div>
        <EstadoEncuestaBadge estado={encuesta.estado} />
      </div>

      <div className="space-y-5">
        {preguntasVisibles.map((pregunta) => (
          <div key={pregunta.id}>
            <label
              htmlFor={`pregunta-${pregunta.id}`}
              className="mb-2 block text-sm font-medium text-gray-700"
            >
              {pregunta.texto}
              {pregunta.requerida && <span className="text-red-600"> *</span>}
            </label>

            {pregunta.tipo === 'ESCALA' ? (
              <fieldset id={`pregunta-${pregunta.id}`} className="flex flex-wrap gap-2">
                <legend className="sr-only">{pregunta.texto}</legend>
                {ESCALA_OPCIONES.map((opcion) => (
                  <label
                    key={opcion}
                    className={`flex min-w-[2.75rem] cursor-pointer items-center justify-center rounded-lg border px-3 py-2 text-sm font-medium transition ${
                      respuestas[pregunta.id] === opcion
                        ? 'border-blue-600 bg-blue-50 text-blue-700'
                        : 'border-gray-300 text-gray-700 hover:border-blue-400'
                    } ${bloqueada ? 'cursor-not-allowed opacity-70' : ''}`}
                  >
                    <input
                      type="radio"
                      name={pregunta.id}
                      value={opcion}
                      checked={respuestas[pregunta.id] === opcion}
                      disabled={bloqueada}
                      onChange={() => actualizarRespuesta(pregunta.id, opcion)}
                      className="sr-only"
                    />
                    {opcion}
                  </label>
                ))}
              </fieldset>
            ) : (
              <textarea
                id={`pregunta-${pregunta.id}`}
                rows={3}
                disabled={bloqueada}
                value={respuestas[pregunta.id] ?? ''}
                onChange={(e) => actualizarRespuesta(pregunta.id, e.target.value)}
                className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 disabled:bg-gray-50"
              />
            )}

            {errores[pregunta.id] && (
              <p className="mt-1 text-sm text-red-600" role="alert">
                {errores[pregunta.id]}
              </p>
            )}
          </div>
        ))}
      </div>

      {!bloqueada && (
        <div className="mt-6 flex flex-col gap-2 sm:flex-row">
          <button
            type="button"
            onClick={handleBorrador}
            disabled={isGuardando || isEnviando}
            className="inline-flex items-center justify-center gap-2 rounded-lg border border-gray-300 px-4 py-2 text-sm font-semibold text-gray-700 hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-60"
          >
            {isGuardando && <Loader2 className="h-4 w-4 animate-spin" aria-hidden="true" />}
            Guardar borrador
          </button>
          <button
            type="button"
            onClick={handleEnviar}
            disabled={isGuardando || isEnviando}
            className="inline-flex items-center justify-center gap-2 rounded-lg bg-blue-700 px-4 py-2 text-sm font-semibold text-white hover:bg-blue-800 disabled:cursor-not-allowed disabled:bg-gray-400"
          >
            {isEnviando ? (
              <Loader2 className="h-4 w-4 animate-spin" aria-hidden="true" />
            ) : (
              <Send className="h-4 w-4" aria-hidden="true" />
            )}
            Enviar encuesta
          </button>
        </div>
      )}

      {bloqueada && encuesta.estado === 'COMPLETADA' && (
        <p className="mt-4 text-sm text-green-700">Encuesta completada. Solo lectura.</p>
      )}

      {puedeEnviarRecordatorio &&
        (encuesta.estado === 'PENDIENTE' || encuesta.estado === 'EN_BORRADOR') && (
          <button
            type="button"
            onClick={onRecordatorio}
            disabled={isRecordatorio}
            className="mt-4 inline-flex items-center gap-2 text-sm font-medium text-blue-700 hover:underline disabled:opacity-60"
          >
            {isRecordatorio && <Loader2 className="h-4 w-4 animate-spin" aria-hidden="true" />}
            Enviar recordatorio manual
          </button>
        )}
    </section>
  );
}
