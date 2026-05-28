import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import authService from '../services/authService';
import useAuth from '../../../shared/hooks/useAuth';

export default function useLogin() {
  const [cargando, setCargando] = useState(false);
  const [error, setError]       = useState(null);
  const { login }               = useAuth();
  const navigate                = useNavigate();

  const iniciarSesion = async (correo, password) => {
    setCargando(true);
    setError(null);
    try {
      const { data } = await authService.login(correo, password);
      login(data.data.token, data.data);

      // Redirige según si es primera vez
      if (data.data.primeraVez) {
        navigate('/cambiar-password');
      } else {
        navigate('/dashboard');
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Credenciales incorrectas');
    } finally {
      setCargando(false);
    }
  };

  return { iniciarSesion, cargando, error };
}