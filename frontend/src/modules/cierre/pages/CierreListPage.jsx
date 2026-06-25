import PracticasSelectorPage from '@/shared/components/PracticasSelectorPage';

export default function CierreListPage() {
  return (
    <PracticasSelectorPage
      titulo="Cierre de práctica"
      descripcion="Selecciona una práctica para completar el checklist de cierre."
      accionLabel="Cerrar práctica"
      construirRuta={(id) => `/cierre/${id}`}
    />
  );
}
