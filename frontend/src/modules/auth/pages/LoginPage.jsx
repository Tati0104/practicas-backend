import { useLayoutEffect, useState } from 'react';
import AuthLayout from '@/modules/auth/components/AuthLayout';
import LoginForm from '@/modules/auth/components/LoginForm';
import useAuthStore from '@/store/authStore';

export default function LoginPage() {
  const cerrarSesion = useAuthStore((state) => state.cerrarSesion);
  const [listo, setListo] = useState(false);

  useLayoutEffect(() => {
    cerrarSesion();
    setListo(true);
  }, [cerrarSesion]);

  if (!listo) {
    return null;
  }

  return (
    <AuthLayout titulo="PracTI" subtitulo="Universidad Alexander Von Humboldt">
      <LoginForm />
    </AuthLayout>
  );
}
