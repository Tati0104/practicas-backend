import { useSearchParams } from 'react-router-dom';
import AuthLayout from '@/modules/auth/components/AuthLayout';
import RestablecerForm from '@/modules/auth/components/RestablecerForm';

export default function RestablecerPasswordPage() {
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token')?.trim() ?? '';

  return (
    <AuthLayout
      titulo="Restablecer contraseña"
      subtitulo="Define tu nueva contraseña de acceso"
    >
      <RestablecerForm token={token} />
    </AuthLayout>
  );
}
