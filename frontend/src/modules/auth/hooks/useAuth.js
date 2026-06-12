import { useMutation } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import http from '@/shared/services/http';
import useAuthStore from '@/store/authStore';
import {
  extraerMensajeError,
  mensajeErrorLogin,
  mensajeErrorResetear,
  obtenerRutaPorRol,
} from '@/modules/auth/utils/schemas';

export default function useAuthMutaciones() {
  const navigate = useNavigate();
  const iniciarSesionStore = useAuthStore((state) => state.iniciarSesion);
  const cerrarSesionStore = useAuthStore((state) => state.cerrarSesion);
  const usuario = useAuthStore((state) => state.usuario);

  const login = useMutation({
    mutationFn: async ({ correo, password }) => {
      const { data } = await http.post('/auth/login', { correo, password });
      return data;
    },
    onSuccess: (data) => {
      const token = data.token;
      iniciarSesionStore(token, data);

      if (data.primeraVez) {
        toast.success('Debes cambiar tu contraseña antes de continuar');
        navigate('/cambiar-password', { replace: true });
        return;
      }

      toast.success(`Bienvenido, ${data.nombre ?? 'usuario'}`);
      navigate(obtenerRutaPorRol(data.rol), { replace: true });
    },
    onError: (error) => {
      toast.error(mensajeErrorLogin(error));
    },
  });

  const recuperar = useMutation({
    mutationFn: async ({ correo }) => {
      const { data } = await http.post('/auth/recuperar', { correo });
      return data;
    },
    onError: (error) => {
      toast.error(extraerMensajeError(error));
    },
  });

  const resetear = useMutation({
    mutationFn: async ({ token, nuevaPassword }) => {
      const { data } = await http.post('/auth/resetear', { token, nuevaPassword });
      return data;
    },
    onSuccess: () => {
      toast.success('Contraseña restablecida correctamente');
      navigate('/login', { replace: true });
    },
    onError: (error) => {
      toast.error(mensajeErrorResetear(error));
    },
  });

  const cambiarPassword = useMutation({
    mutationFn: async ({ id, nuevaPassword }) => {
      const { data } = await http.post('/auth/cambiar-password', { id, nuevaPassword });
      return data;
    },
    onSuccess: () => {
      toast.success('Contraseña actualizada correctamente');
      const rol = useAuthStore.getState().rol;
      navigate(obtenerRutaPorRol(rol), { replace: true });
    },
    onError: (error) => {
      toast.error(extraerMensajeError(error));
    },
  });

  const cerrarSesion = () => {
    cerrarSesionStore();
    toast.success('Sesión cerrada');
    navigate('/login', { replace: true });
  };

  return {
    login,
    recuperar,
    resetear,
    cambiarPassword,
    cerrarSesion,
    usuario,
  };
}
