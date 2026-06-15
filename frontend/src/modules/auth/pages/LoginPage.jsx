import { Navigate } from 'react-router-dom';
import AuthLayout from '@/modules/auth/components/AuthLayout';
import LoginForm from '@/modules/auth/components/LoginForm';
import useAuthStore from '@/store/authStore';
import { obtenerRutaPorRol } from '@/modules/auth/utils/schemas';

export default function LoginPage() {
  const token = useAuthStore((state) => state.token);
  const rol = useAuthStore((state) => state.rol);

  if (token) {
    return <Navigate to={obtenerRutaPorRol(rol)} replace />;
  }

  return (
    <AuthLayout
      titulo="PracTI"
      subtitulo="Universidad Alexander Von Humboldt"
    >
      <LoginForm />
    </AuthLayout>
  );
}
