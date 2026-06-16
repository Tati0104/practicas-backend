import useAuthStore from '@/store/authStore';

const ROLES_VER_ENCUESTA_TUTOR = ['TUTOR_EMPRESARIAL', 'DOCENTE_ASESOR', 'COORD_PRACTICA', 'ADMIN'];
const ROLES_VER_ENCUESTA_ESTUDIANTE = ['ESTUDIANTE', 'DOCENTE_ASESOR', 'COORD_PRACTICA', 'ADMIN'];
const ROLES_GESTION = ['COORD_PRACTICA', 'ADMIN'];

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
    puedeRegistrarNotaDocente:
      rol === 'DOCENTE_ASESOR' || ROLES_GESTION.includes(rol),
    puedeRegistrarNotaTutor:
      rol === 'TUTOR_EMPRESARIAL' || ROLES_GESTION.includes(rol),
    puedeRegistrarNotaFinal:
      rol === 'DOCENTE_ASESOR' || ROLES_GESTION.includes(rol),
    puedeCompletarEncuestaTutor:
      rol === 'TUTOR_EMPRESARIAL' || ROLES_GESTION.includes(rol),
    puedeCompletarEncuestaEstudiante:
      rol === 'ESTUDIANTE' || ROLES_GESTION.includes(rol),
    puedeEnviarRecordatorio: ROLES_GESTION.includes(rol),
    puedeEnviarInvitacion: ROLES_GESTION.includes(rol),
    puedeVerEncuestaTutor: ROLES_VER_ENCUESTA_TUTOR.includes(rol),
    puedeVerEncuestaEstudiante: ROLES_VER_ENCUESTA_ESTUDIANTE.includes(rol),
    rol,
  };
}
