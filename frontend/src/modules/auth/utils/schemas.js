import { z } from 'zod';

export const loginSchema = z.object({
  correo: z.string().email('Ingresa un correo válido'),
  password: z.string().min(1, 'La contraseña es requerida'),
});

export const recuperarSchema = z.object({
  correo: z.string().email('Ingresa un correo válido'),
});

export const nuevaPasswordSchema = z
  .object({
    nuevaPassword: z
      .string()
      .min(8, 'Mínimo 8 caracteres')
      .regex(/[A-Z]/, 'Debe tener al menos una mayúscula')
      .regex(/[0-9]/, 'Debe tener al menos un número'),
    confirmarPassword: z.string(),
  })
  .refine((data) => data.nuevaPassword === data.confirmarPassword, {
    message: 'Las contraseñas no coinciden',
    path: ['confirmarPassword'],
  });

export const resetearSchema = z
  .object({
    token: z.string().min(1, 'El token es requerido'),
    nuevaPassword: z
      .string()
      .min(8, 'Mínimo 8 caracteres')
      .regex(/[A-Z]/, 'Debe tener al menos una mayúscula')
      .regex(/[0-9]/, 'Debe tener al menos un número'),
    confirmarPassword: z.string(),
  })
  .refine((data) => data.nuevaPassword === data.confirmarPassword, {
    message: 'Las contraseñas no coinciden',
    path: ['confirmarPassword'],
  });

export const RUTAS_POR_ROL = {
  ADMIN: '/dashboard',
  DIRECCION: '/dashboard',
  COORD_ACADEMICA: '/dashboard',
  COORD_PRACTICA: '/dashboard',
  DOCENTE_ASESOR: '/seguimiento',
  ESTUDIANTE: '/vinculacion',
  EMPRESA: '/vacantes',
  TUTOR_EMPRESARIAL: '/seguimiento',
  COMITE_PRACTICAS: '/dashboard',
};

/**
 * @param {string|null|undefined} rol
 * @returns {string}
 */
export function obtenerRutaPorRol(rol) {
  return RUTAS_POR_ROL[rol] ?? '/dashboard';
}

/**
 * @param {import('axios').AxiosError} error
 * @param {string} fallback
 */
export function extraerMensajeError(error, fallback = 'Error inesperado. Intenta de nuevo.') {
  const data = error.response?.data;
  if (!data) return fallback;

  if (typeof data === 'string') return data;
  if (data.mensaje) return data.mensaje;
  if (data.message) return data.message;

  return fallback;
}

/**
 * @param {import('axios').AxiosError} error
 */
export function mensajeErrorResetear(error) {
  const mensaje = extraerMensajeError(error, '');
  const texto = mensaje.toLowerCase();

  if (texto.includes('expirado')) {
    return 'El enlace ha expirado. Solicita uno nuevo.';
  }

  if (texto.includes('inválido') || texto.includes('invalido')) {
    return 'El enlace no es válido. Solicita uno nuevo.';
  }

  return mensaje || 'No se pudo restablecer la contraseña. Intenta de nuevo.';
}

/**
 * @param {import('axios').AxiosError} error
 */
export function mensajeErrorLogin(error) {
  if (!error.response) {
    return 'No se pudo conectar con el servidor. Verifica que el backend esté activo y que uses http://localhost:5173 (no otro puerto).';
  }

  if (error.response?.status === 401) {
    return 'Correo o contraseña incorrectos';
  }

  const mensaje = extraerMensajeError(error, 'Correo o contraseña incorrectos');
  if (mensaje.toLowerCase().includes('credenciales')) {
    return 'Correo o contraseña incorrectos';
  }

  return mensaje;
}
