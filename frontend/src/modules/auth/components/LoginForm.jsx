import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Eye, EyeOff, Loader2 } from 'lucide-react';
import { loginSchema } from '@/modules/auth/utils/schemas';
import useAuthMutaciones from '@/modules/auth/hooks/useAuth';

export default function LoginForm() {
  const [verPassword, setVerPassword] = useState(false);
  const { login } = useAuthMutaciones();

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm({
    resolver: zodResolver(loginSchema),
    defaultValues: { correo: '', password: '' },
  });

  const onSubmit = (datos) => {
    login.mutate(datos);
  };

  const cargando = isSubmitting || login.isPending;

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4" noValidate>
      <div>
        <label htmlFor="correo" className="mb-1 block text-sm font-medium text-gray-700">
          Correo electrónico
        </label>
        <input
          id="correo"
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

      <div>
        <label htmlFor="password" className="mb-1 block text-sm font-medium text-gray-700">
          Contraseña
        </label>
        <div className="relative">
          <input
            id="password"
            type={verPassword ? 'text' : 'password'}
            autoComplete="current-password"
            placeholder="••••••••"
            className="w-full rounded-lg border border-gray-300 px-3 py-2.5 pr-10 text-sm text-gray-900 outline-none transition focus:border-primary focus:ring-2 focus:ring-primary/20"
            {...register('password')}
          />
          <button
            type="button"
            onClick={() => setVerPassword((prev) => !prev)}
            className="absolute right-2 top-1/2 -translate-y-1/2 rounded p-1 text-gray-400 hover:text-gray-600"
            aria-label={verPassword ? 'Ocultar contraseña' : 'Mostrar contraseña'}
          >
            {verPassword ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
          </button>
        </div>
        {errors.password && (
          <p className="mt-1 text-sm text-red-600" role="alert">
            {errors.password.message}
          </p>
        )}
      </div>

      <button
        type="submit"
        disabled={cargando}
        className="flex w-full items-center justify-center gap-2 rounded-lg bg-primary px-4 py-2.5 text-sm font-semibold text-white transition hover:bg-primary/90 disabled:cursor-not-allowed disabled:bg-gray-400"
      >
        {cargando && <Loader2 className="h-4 w-4 animate-spin" aria-hidden="true" />}
        {cargando ? 'Iniciando sesión...' : 'Iniciar sesión'}
      </button>

      <div className="text-center">
        <Link
          to="/recuperar-password"
          className="text-sm font-medium text-primary hover:text-primary/80 hover:underline"
        >
          ¿Olvidaste tu contraseña?
        </Link>
      </div>
    </form>
  );
}
