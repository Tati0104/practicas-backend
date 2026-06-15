import useAuthStore from '@/store/authStore';

const ROLES_VER_ENCUESTA_TUTOR = ['TUTOR_EMPRESARIAL', 'DOCENTE_ASESOR', 'COORD_PRACTICA', 'ADMIN'];
const ROLES_VER_ENCUESTA_ESTUDIANTE = ['ESTUDIANTE', 'DOCENTE_ASESOR', 'COORD_PRACTICA', 'ADMIN'];

/**
 * Permisos específicos del módulo de evaluaciones según rol.
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
    puedeRegistrarNotaFinal: rol === 'DOCENTE_ASESOR' || rol === 'ADMIN',
    puedeCompletarEncuestaTutor: rol === 'TUTOR_EMPRESARIAL' || rol === 'ADMIN',
    puedeCompletarEncuestaEstudiante: rol === 'ESTUDIANTE' || rol === 'ADMIN',
    puedeEnviarRecordatorio: rol === 'COORD_PRACTICA' || rol === 'ADMIN',
    puedeVerEncuestaTutor: ROLES_VER_ENCUESTA_TUTOR.includes(rol),
    puedeVerEncuestaEstudiante: ROLES_VER_ENCUESTA_ESTUDIANTE.includes(rol),
    rol,
  };
}
