import useAuthStore from '@/store/authStore';
import PracticasSelectorPage from '@/shared/components/PracticasSelectorPage';
import SeguimientoFiltrosEstudiante from '@/modules/seguimiento/components/SeguimientoFiltrosEstudiante';
import CalificacionesFiltros from '../components/CalificacionesFiltros';

export default function CalificacionesListPage() {
  const esEstudiante = useAuthStore((state) => state.rol) === 'ESTUDIANTE';

  return (
    <PracticasSelectorPage
      titulo="Evaluaciones"
      descripcion={
        esEstudiante
          ? 'Consulta tus notas, resultado final y estado de la encuesta de cierre'
          : 'Selecciona una práctica para registrar notas, consultar calificaciones y gestionar encuestas de cierre.'
      }
      accionLabel={esEstudiante ? 'Ver evaluaciones' : 'Evaluar'}
      construirRuta={(id) => `/calificaciones/${id}`}
      FiltrosComponent={esEstudiante ? SeguimientoFiltrosEstudiante : CalificacionesFiltros}
    />
  );
}
