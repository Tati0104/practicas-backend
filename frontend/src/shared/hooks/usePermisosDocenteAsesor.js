import useAuthStore from '@/store/authStore';

const ROLES_ASIGNAR_DOCENTE_ASESOR = ['COORD_ACADEMICA', 'COORD_PRACTICA', 'ADMIN'];

/**
 * Permiso compartido entre las pantallas de Asignaciones y Vinculación: quién puede
 * ver y cambiar el docente asesor de una práctica (separado del permiso para
 * "Activar Práctica", que sigue siendo exclusivo de COORD_PRACTICA).
 */
export function usePuedeAsignarDocenteAsesor() {
  const rol = useAuthStore((state) => state.rol);
  return ROLES_ASIGNAR_DOCENTE_ASESOR.includes(rol);
}