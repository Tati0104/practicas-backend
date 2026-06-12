import useAuthStore from '@/store/authStore';

/**
 * Permisos específicos del módulo de calificaciones según rol.
 */
export default function useCalificacionesPermisos() {
  const rol = useAuthStore((state) => state.rol);

  return {
    puedeVerResumen: [
      'DOCENTE_ASESOR',
      'TUTOR_EMPRESARIAL',
      'COORD_PRACTICA',
      'ESTUDIANTE',
      'ADMIN',
    ].includes(rol),
    puedeRegistrarNotaDocente: rol === 'DOCENTE_ASESOR' || rol === 'ADMIN',
    puedeRegistrarNotaTutor: rol === 'TUTOR_EMPRESARIAL' || rol === 'ADMIN',
    puedeRegistrarNotaFinal: rol === 'COORD_PRACTICA' || rol === 'ADMIN',
    puedeCompletarEncuestaTutor: rol === 'TUTOR_EMPRESARIAL' || rol === 'ADMIN',
    puedeCompletarEncuestaEstudiante: rol === 'ESTUDIANTE' || rol === 'ADMIN',
    puedeEnviarRecordatorio: rol === 'COORD_PRACTICA' || rol === 'ADMIN',
    puedeVerEncuestaTutor: ['TUTOR_EMPRESARIAL', 'COORD_PRACTICA', 'ADMIN'].includes(rol),
    puedeVerEncuestaEstudiante: ['ESTUDIANTE', 'COORD_PRACTICA', 'ADMIN'].includes(rol),
    rol,
  };
}
