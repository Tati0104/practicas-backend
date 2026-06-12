import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Link } from 'react-router-dom';
import { Eye, EyeOff, Loader2, AlertCircle } from 'lucide-react';
import { resetearSchema } from '@/modules/auth/utils/schemas';
import useAuthMutaciones from '@/modules/auth/hooks/useAuth';

export default function RestablecerForm({ token }) {
  const [verNueva, setVerNueva] = useState(false);
  const [verConfirmar, setVerConfirmar] = useState(false);
  const { resetear } = useAuthMutaciones();

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm({
    resolver: zodResolver(resetearSchema),
    defaultValues: { nuevaPassword: '', confirmarPassword: '' },
  });

  if (!token) {
    return (
      <div className="space-y-4 text-center">
        <div className="mx-auto flex h-12 w-12 items-center justify-center rounded-full bg-red-50 text-red-600">
          <AlertCircle className="h-6 w-6" aria-hidden="true" />
        </div>
        <p className="text-sm text-red-600" role="alert">
          El enlace no es válido. Solicita uno nuevo.
        </p>
        <Link
          to="/recuperar-password"
          className="text-sm font-medium text-blue-700 hover:underline"
        >
          Solicitar nuevo enlace
        </Link>
      </div>
    );
  }

  const onSubmit = ({ nuevaPassword }) => {
    resetear.mutate({ token, nuevaPassword });
  };

  const cargando = isSubmitting || resetear.isPending;

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4" noValidate>
      <p className="text-sm text-gray-600">
        Elige una contraseña segura con al menos 8 caracteres, una mayúscula y un número.
      </p>

      <div>
        <label htmlFor="nueva-password" className="mb-1 block text-sm font-medium text-gray-700">
          Nueva contraseña
        </label>
        <div className="relative">
          <input
            id="nueva-password"
            type={verNueva ? 'text' : 'password'}
            autoComplete="new-password"
            className="w-full rounded-lg border border-gray-300 px-3 py-2.5 pr-10 text-sm outline-none transition focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20"
            {...register('nuevaPassword')}
          />
          <button
            type="button"
            onClick={() => setVerNueva((prev) => !prev)}
            className="absolute right-2 top-1/2 -translate-y-1/2 rounded p-1 text-gray-400 hover:text-gray-600"
            aria-label={verNueva ? 'Ocultar contraseña' : 'Mostrar contraseña'}
          >
            {verNueva ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
          </button>
        </div>
        {errors.nuevaPassword && (
          <p className="mt-1 text-sm text-red-600" role="alert">
            {errors.nuevaPassword.message}
          </p>
        )}
      </div>

      <div>
        <label htmlFor="confirmar-password" className="mb-1 block text-sm font-medium text-gray-700">
          Confirmar contraseña
        </label>
        <div className="relative">
          <input
            id="confirmar-password"
            type={verConfirmar ? 'text' : 'password'}
            autoComplete="new-password"
            className="w-full rounded-lg border border-gray-300 px-3 py-2.5 pr-10 text-sm outline-none transition focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20"
            {...register('confirmarPassword')}
          />
          <button
            type="button"
            onClick={() => setVerConfirmar((prev) => !prev)}
            className="absolute right-2 top-1/2 -translate-y-1/2 rounded p-1 text-gray-400 hover:text-gray-600"
            aria-label={verConfirmar ? 'Ocultar confirmación' : 'Mostrar confirmación'}
          >
            {verConfirmar ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
          </button>
        </div>
        {errors.confirmarPassword && (
          <p className="mt-1 text-sm text-red-600" role="alert">
            {errors.confirmarPassword.message}
          </p>
        )}
      </div>

      <button
        type="submit"
        disabled={cargando}
        className="flex w-full items-center justify-center gap-2 rounded-lg bg-blue-700 px-4 py-2.5 text-sm font-semibold text-white transition hover:bg-blue-800 disabled:cursor-not-allowed disabled:bg-gray-400"
      >
        {cargando && <Loader2 className="h-4 w-4 animate-spin" aria-hidden="true" />}
        {cargando ? 'Guardando...' : 'Restablecer contraseña'}
      </button>
    </form>
  );
}
