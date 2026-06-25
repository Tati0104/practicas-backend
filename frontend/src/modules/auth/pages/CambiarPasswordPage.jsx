import { Link, Navigate } from 'react-router-dom';
import AuthLayout from '@/modules/auth/components/AuthLayout';
import CambiarPasswordForm from '@/modules/auth/components/CambiarPasswordForm';
import useAuthStore from '@/store/authStore';

export default function CambiarPasswordPage() {
  const token = useAuthStore((state) => state.token);

  if (!token) {
    return <Navigate to="/login" replace />;
  }

  return (
    <AuthLayout
      titulo="Cambio de contraseña"
      subtitulo="Actualiza tu contraseña de acceso"
    >
      <CambiarPasswordForm />
      <p className="mt-4 text-center">
        <Link to="/dashboard" className="text-sm font-medium text-primary hover:underline">
          Volver al panel
        </Link>
      </p>
    </AuthLayout>
  );
}
