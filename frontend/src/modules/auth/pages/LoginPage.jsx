import { Navigate } from 'react-router-dom';
import AuthLayout from '@/modules/auth/components/AuthLayout';
import LoginForm from '@/modules/auth/components/LoginForm';
import useAuthStore from '@/store/authStore';

export default function LoginPage() {
  const sesionValida = useAuthStore((state) => state.sesionValida);

  if (sesionValida()) {
    return <Navigate to="/dashboard" replace />;
  }

  return (
    <AuthLayout titulo="PracTI" subtitulo="Universidad Alexander Von Humboldt">
      <LoginForm />
    </AuthLayout>
  );
}
