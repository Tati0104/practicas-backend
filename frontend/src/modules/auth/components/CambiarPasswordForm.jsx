import { useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Eye, EyeOff, Loader2, AlertCircle } from 'lucide-react';
import { nuevaPasswordSchema } from '@/modules/auth/utils/schemas';
import useAuthMutaciones from '@/modules/auth/hooks/useAuth';
import useAuthStore from '@/store/authStore';

export default function CambiarPasswordForm() {
  const [verNueva, setVerNueva] = useState(false);
  const [verConfirmar, setVerConfirmar] = useState(false);
  const { cambiarPassword } = useAuthMutaciones();
  const usuario = useAuthStore((state) => state.usuario);

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm({
    resolver: zodResolver(nuevaPasswordSchema),
    defaultValues: { nuevaPassword: '', confirmarPassword: '' },
  });

  useEffect(() => {
    return () => reset();
  }, [reset]);

  const onSubmit = ({ nuevaPassword }) => {
    if (!usuario?.id) {
      return;
    }

    cambiarPassword.mutate(
      { id: usuario.id, nuevaPassword },
      { onSuccess: () => reset() }
    );
  };

  const cargando = isSubmitting || cambiarPassword.isPending;

  if (!usuario?.id) {
    return (
      <div className="space-y-3 text-center">
        <div className="mx-auto flex h-12 w-12 items-center justify-center rounded-full bg-amber-50 text-amber-600">
          <AlertCircle className="h-6 w-6" aria-hidden="true" />
        </div>
        <p className="text-sm text-gray-600">
          No se pudo identificar tu usuario. Cierra sesión e intenta iniciar sesión nuevamente.
        </p>
      </div>
    );
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4" noValidate>
      <p className="text-sm text-gray-600">
        Por seguridad, debes establecer una nueva contraseña antes de acceder al sistema.
      </p>

      <div>
        <label htmlFor="cambiar-nueva" className="mb-1 block text-sm font-medium text-gray-700">
          Nueva contraseña
        </label>
        <div className="relative">
          <input
            id="cambiar-nueva"
            type={verNueva ? 'text' : 'password'}
            autoComplete="new-password"
            className="w-full rounded-lg border border-gray-300 px-3 py-2.5 pr-10 text-sm outline-none transition focus:border-primary focus:ring-2 focus:ring-primary/20"
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
        <label htmlFor="cambiar-confirmar" className="mb-1 block text-sm font-medium text-gray-700">
          Confirmar contraseña
        </label>
        <div className="relative">
          <input
            id="cambiar-confirmar"
            type={verConfirmar ? 'text' : 'password'}
            autoComplete="new-password"
            className="w-full rounded-lg border border-gray-300 px-3 py-2.5 pr-10 text-sm outline-none transition focus:border-primary focus:ring-2 focus:ring-primary/20"
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
        className="flex w-full items-center justify-center gap-2 rounded-lg bg-primary px-4 py-2.5 text-sm font-semibold text-white transition hover:bg-primary/90 disabled:cursor-not-allowed disabled:bg-gray-400"
      >
        {cargando && <Loader2 className="h-4 w-4 animate-spin" aria-hidden="true" />}
        {cargando ? 'Guardando...' : 'Cambiar contraseña'}
      </button>
    </form>
  );
}
