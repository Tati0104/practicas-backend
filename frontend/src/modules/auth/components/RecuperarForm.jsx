import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Link } from 'react-router-dom';
import { ArrowLeft, Loader2, MailCheck } from 'lucide-react';
import { recuperarSchema } from '@/modules/auth/utils/schemas';
import useAuthMutaciones from '@/modules/auth/hooks/useAuth';

export default function RecuperarForm() {
  const { recuperar } = useAuthMutaciones();

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
    reset,
  } = useForm({
    resolver: zodResolver(recuperarSchema),
    defaultValues: { correo: '' },
  });

  const onSubmit = (datos) => {
    recuperar.mutate(datos, {
      onSuccess: () => reset(),
    });
  };

  const cargando = isSubmitting || recuperar.isPending;
  const enviado = recuperar.isSuccess;

  if (enviado) {
    return (
      <div className="space-y-4 text-center">
        <div className="mx-auto flex h-12 w-12 items-center justify-center rounded-full bg-green-50 text-green-600">
          <MailCheck className="h-6 w-6" aria-hidden="true" />
        </div>
        <p className="text-sm text-gray-600">
          Si el correo está registrado, recibirás instrucciones para recuperar tu contraseña.
        </p>
        <Link
          to="/login"
          className="inline-flex items-center gap-1 text-sm font-medium text-primary hover:underline"
        >
          <ArrowLeft className="h-4 w-4" />
          Volver al inicio de sesión
        </Link>
      </div>
    );
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4" noValidate>
      <p className="text-sm text-gray-600">
        Ingresa tu correo institucional y te enviaremos un enlace para restablecer tu contraseña.
      </p>

      <div>
        <label htmlFor="correo-recuperar" className="mb-1 block text-sm font-medium text-gray-700">
          Correo electrónico
        </label>
        <input
          id="correo-recuperar"
          type="email"
          autoComplete="email"
          placeholder="correo@avh.edu.co"
          className="w-full rounded-lg border border-gray-300 px-3 py-2.5 text-sm text-gray-900 outline-none transition focus:border-primary focus:ring-2 focus:ring-primary/20"
          {...register('correo')}
        />
        {errors.correo && (
          <p className="mt-1 text-sm text-red-600" role="alert">
            {errors.correo.message}
          </p>
        )}
      </div>

      <button
        type="submit"
        disabled={cargando}
        className="flex w-full items-center justify-center gap-2 rounded-lg bg-primary px-4 py-2.5 text-sm font-semibold text-white transition hover:bg-primary/90 disabled:cursor-not-allowed disabled:bg-gray-400"
      >
        {cargando && <Loader2 className="h-4 w-4 animate-spin" aria-hidden="true" />}
        {cargando ? 'Enviando...' : 'Enviar enlace'}
      </button>

      <div className="text-center">
        <Link
          to="/login"
          className="inline-flex items-center gap-1 text-sm font-medium text-primary hover:underline"
        >
          <ArrowLeft className="h-4 w-4" />
          Volver al inicio de sesión
        </Link>
      </div>
    </form>
  );
}
