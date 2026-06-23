import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import useAuthStore from '@/store/authStore';
import { tokenExpirado } from '@/modules/auth/utils/jwt';
import { registrarActividad, sesionInactivaExpirada } from '@/modules/auth/utils/sesion';

const EVENTOS_ACTIVIDAD = ['mousedown', 'keydown', 'scroll', 'touchstart', 'click'];

function sesionInvalida(token) {
  return !token || tokenExpirado(token) || sesionInactivaExpirada();
}

export default function VigilanteSesion() {
  const navigate = useNavigate();
  const token = useAuthStore((state) => state.token);

  useEffect(() => {
    if (!token) return undefined;

    const marcarActividad = () => {
      if (useAuthStore.getState().token) {
        registrarActividad();
      }
    };

    EVENTOS_ACTIVIDAD.forEach((evento) => {
      window.addEventListener(evento, marcarActividad, { passive: true });
    });

    const intervalo = window.setInterval(() => {
      const actual = useAuthStore.getState().token;
      if (actual && sesionInvalida(actual)) {
        useAuthStore.getState().cerrarSesion();
        toast.error('Tu sesión expiró por inactividad. Inicia sesión de nuevo.');
        navigate('/login', { replace: true });
      }
    }, 60_000);

    return () => {
      EVENTOS_ACTIVIDAD.forEach((evento) => {
        window.removeEventListener(evento, marcarActividad);
      });
      window.clearInterval(intervalo);
    };
  }, [token, navigate]);

  return null;
}
