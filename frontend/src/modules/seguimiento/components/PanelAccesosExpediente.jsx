import { Link } from 'react-router-dom';
import { FileText, Star, CheckSquare } from 'lucide-react';
import { Card } from '@/shared/components/ui';

const ACCESOS = [
  {
    titulo: 'Documentos de vinculación',
    descripcion: 'Convenio, carta, hoja de vida y demás documentos del proceso.',
    ruta: (id) => `/vinculacion/practica/${id}`,
    icono: FileText,
  },
  {
    titulo: 'Evaluaciones',
    descripcion: 'Encuestas, notas por corte y calificación final.',
    ruta: (id) => `/evaluaciones/${id}`,
    icono: Star,
  },
  {
    titulo: 'Checklist de cierre',
    descripcion: 'Requisitos de cierre y estado de finalización.',
    ruta: (id) => `/cierre/${id}`,
    icono: CheckSquare,
  },
];

export default function PanelAccesosExpediente({ practicaId, soloLectura = false }) {
  return (
    <Card padding="p-5" className="xl:w-72 xl:shrink-0">
      <h2 className="mb-1 text-base font-bold text-gray-900">Expediente de la práctica</h2>
      <p className="mb-4 text-sm text-gray-600">
        {soloLectura
          ? 'Consulta la información completa de esta práctica finalizada.'
          : 'Accede a los módulos relacionados con esta práctica.'}
      </p>
      <ul className="flex flex-col gap-3">
        {ACCESOS.map(({ titulo, descripcion, ruta, icono: Icono }) => (
          <li key={titulo}>
            <Link
              to={ruta(practicaId)}
              className="group flex gap-3 rounded-lg border border-gray-200 bg-white p-3 transition hover:border-primary/40 hover:bg-primary/5"
            >
              <span className="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg bg-slate-100 text-slate-600 group-hover:bg-primary/10 group-hover:text-primary">
                <Icono className="h-4 w-4" aria-hidden="true" />
              </span>
              <span>
                <span className="block text-sm font-semibold text-gray-900">{titulo}</span>
                <span className="mt-0.5 block text-xs text-gray-500">{descripcion}</span>
              </span>
            </Link>
          </li>
        ))}
      </ul>
    </Card>
  );
}
