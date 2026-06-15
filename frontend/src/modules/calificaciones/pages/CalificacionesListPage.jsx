import PracticasSelectorPage from '@/shared/components/PracticasSelectorPage';

export default function CalificacionesListPage() {
  return (
    <PracticasSelectorPage
      titulo="Calificaciones"
      descripcion="Selecciona una práctica para registrar o consultar evaluaciones."
      accionLabel="Calificar"
      construirRuta={(id) => `/calificaciones/${id}`}
    />
  );
}
