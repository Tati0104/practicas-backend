import useAuthStore from '@/store/authStore';
import PracticasSelectorPage from '@/shared/components/PracticasSelectorPage';
import SeguimientoFiltrosEstudiante from '@/modules/seguimiento/components/SeguimientoFiltrosEstudiante';
import CalificacionesFiltros from '../components/CalificacionesFiltros';

export default function CalificacionesListPage() {
  const rol = useAuthStore((state) => state.rol);
  const esEstudiante = rol === 'ESTUDIANTE';
  const esTutor = rol === 'TUTOR_EMPRESARIAL';

  return (
    <PracticasSelectorPage
      titulo="Evaluaciones"
      descripcion={
        esEstudiante
          ? 'Consulta tus notas, resultado final y estado de la encuesta de cierre'
          : esTutor
            ? 'Selecciona un practicante de tu empresa para registrar tu nota de referencia y ver la nota final'
            : 'Selecciona una práctica para registrar notas, consultar calificaciones y gestionar encuestas de cierre.'
      }
      accionLabel={esEstudiante ? 'Ver evaluaciones' : 'Evaluar'}
      construirRuta={(id) => `/evaluaciones/${id}`}
      FiltrosComponent={esEstudiante ? SeguimientoFiltrosEstudiante : CalificacionesFiltros}
    />
  );
}
