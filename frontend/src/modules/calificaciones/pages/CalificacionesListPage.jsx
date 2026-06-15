import PracticasSelectorPage from '@/shared/components/PracticasSelectorPage';
import CalificacionesFiltros from '../components/CalificacionesFiltros';

export default function CalificacionesListPage() {
  return (
    <PracticasSelectorPage
      titulo="Evaluaciones"
      descripcion="Selecciona una práctica para registrar notas, consultar calificaciones y gestionar encuestas de cierre."
      accionLabel="Evaluar"
      construirRuta={(id) => `/calificaciones/${id}`}
      FiltrosComponent={CalificacionesFiltros}
    />
  );
}
