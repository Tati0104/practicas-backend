import AuthLayout from '@/modules/auth/components/AuthLayout';
import RecuperarForm from '@/modules/auth/components/RecuperarForm';

export default function RecuperarPasswordPage() {
  return (
    <AuthLayout
      titulo="Recuperar contraseña"
      subtitulo="Te enviaremos un enlace a tu correo"
    >
      <RecuperarForm />
    </AuthLayout>
  );
}
