import { useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Loader2 } from 'lucide-react';
import { notaFinalSchema, notaSchema } from '../utils/schemas';

export default function NotaForm({
  titulo,
  descripcion,
  tipo = 'referencia',
  notaExistente = null,
  practicaActiva = true,
  isPending = false,
  onSubmit,
}) {
  const schema = tipo === 'final' ? notaFinalSchema : notaSchema;
  const ocultarFormulario = notaExistente != null || !practicaActiva;

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm({
    resolver: zodResolver(schema),
    defaultValues: {
      nota: '',
      observaciones: '',
    },
  });

  useEffect(() => {
    if (ocultarFormulario) {
      reset();
    }
  }, [ocultarFormulario, reset]);

  if (notaExistente != null) {
    return (
      <div className="rounded-xl border border-green-200 bg-green-50 p-4">
        <p className="text-sm font-medium text-green-800">
          {titulo}: nota registrada ({notaExistente.toFixed(1)}). No puede modificarse.
        </p>
      </div>
    );
  }

  if (!practicaActiva) {
    return (
      <div className="rounded-xl border border-amber-200 bg-amber-50 p-4">
        <p className="text-sm text-amber-800">
          La práctica no está en seguimiento activo. No es posible registrar notas.
        </p>
      </div>
    );
  }

  const procesarEnvio = (datos) => {
    onSubmit({
      nota: Number(datos.nota),
      observaciones: datos.observaciones?.trim() || undefined,
    });
    reset();
  };

  const cargando = isSubmitting || isPending;

  return (
    <form
      onSubmit={handleSubmit(procesarEnvio)}
      className="rounded-xl border border-gray-200 bg-white p-4 shadow-sm"
      noValidate
    >
      <h3 className="text-base font-semibold text-gray-900">{titulo}</h3>
      {descripcion && <p className="mt-1 text-sm text-gray-600">{descripcion}</p>}

      <div className="mt-4 grid grid-cols-1 gap-4 md:grid-cols-2">
        <div>
          <label htmlFor={`nota-${tipo}`} className="mb-1 block text-sm font-medium text-gray-700">
            Nota (0.0 – 5.0)
          </label>
          <input
            id={`nota-${tipo}`}
            type="number"
            step="0.1"
            min="0"
            max="5"
            placeholder="4.5"
            className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20"
            {...register('nota', { valueAsNumber: true })}
          />
          {errors.nota && (
            <p className="mt-1 text-sm text-red-600" role="alert">
              {errors.nota.message}
            </p>
          )}
        </div>

        {tipo !== 'final' && (
          <div className="md:col-span-2">
            <label
              htmlFor={`observaciones-${tipo}`}
              className="mb-1 block text-sm font-medium text-gray-700"
            >
              Observación (opcional)
            </label>
            <textarea
              id={`observaciones-${tipo}`}
              rows={3}
              maxLength={500}
              placeholder="Comentarios sobre el desempeño..."
              className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20"
              {...register('observaciones')}
            />
            {errors.observaciones && (
              <p className="mt-1 text-sm text-red-600" role="alert">
                {errors.observaciones.message}
              </p>
            )}
          </div>
        )}
      </div>

      <button
        type="submit"
        disabled={cargando}
        className="mt-4 inline-flex items-center gap-2 rounded-lg bg-blue-700 px-4 py-2 text-sm font-semibold text-white hover:bg-blue-800 disabled:cursor-not-allowed disabled:bg-gray-400"
      >
        {cargando && <Loader2 className="h-4 w-4 animate-spin" aria-hidden="true" />}
        {cargando ? 'Guardando...' : 'Registrar nota'}
      </button>
    </form>
  );
}
