import { useNavigate } from 'react-router-dom';
import TablaBase from '../../../shared/components/TablaBase';
import BadgeDocumento from './BadgeDocumento';
import { Button, Badge } from '@/shared/components/ui';

const COLUMNAS_DOC = [
  { key: 'HOJA_VIDA', titulo: 'Hoja de vida' },
  { key: 'CARTA', titulo: 'Carta' },
  { key: 'PROYECTO', titulo: 'Proyecto' },
  { key: 'CONVENIO', titulo: 'Convenio' },
];

function buscarDoc(documentos, tipo) {
  return documentos?.find((d) => d.tipo === tipo);
}

function contarCompletos(docs = []) {
  return docs.filter((d) => {
    if (d.estado === 'PENDIENTE') return false;
    if (d.tipo === 'CONVENIO') return d.estado === 'FIRMADO';
    return true;
  }).length;
}

export default function VinculacionTabla({ vinculaciones, isLoading, onGestionar, ocultarEstudiante = false }) {
  const navigate = useNavigate();

  const columnas = [
    ...(ocultarEstudiante
      ? [
          {
            key: 'practica',
            titulo: 'Práctica',
            render: (fila) => (
              <div>
                <div className="text-sm font-semibold text-gray-900">
                  Práctica {fila.numeroPractica ?? '—'}
                </div>
                {fila.estadoPractica && (
                  <Badge variant={fila.estadoPractica === 'COMPLETADA' ? 'success' : 'neutral'}>
                    {fila.estadoPractica.replace(/_/g, ' ')}
                  </Badge>
                )}
              </div>
            ),
          },
        ]
      : [
          {
            key: 'estudiante',
            titulo: 'Estudiante',
            render: (fila) => (
              <div>
                <div className="text-sm font-semibold text-gray-900">{fila.estudiante?.nombre || '—'}</div>
                <div className="text-xs text-gray-500">
                  {fila.estudiante?.codigo} · {fila.estudiante?.programa}
                </div>
              </div>
            ),
          },
        ]),
    {
      key: 'vacante',
      titulo: 'Cargo / Empresa',
      render: (fila) => (
        <div>
          <div className="text-sm font-semibold text-gray-900">{fila.vacante?.cargo || '—'}</div>
          <div className="text-xs text-gray-500">{fila.vacante?.empresa}</div>
        </div>
      ),
    },
    ...COLUMNAS_DOC.map(({ key, titulo }) => ({
      key,
      titulo,
      render: (fila) => {
        const doc = buscarDoc(fila.documentos, key);
        return <BadgeDocumento estado={doc?.estado || 'PENDIENTE'} />;
      },
    })),
    {
      key: 'progreso',
      titulo: 'Progreso',
      render: (fila) => {
        const completos = contarCompletos(fila.documentos);
        const total = fila.documentos?.length || 4;
        return (
          <span
            className={`text-sm font-semibold ${completos === total ? 'text-emerald-600' : 'text-gray-700'}`}
          >
            {completos}/{total}
          </span>
        );
      },
    },
    {
      key: 'acciones',
      titulo: 'Acciones',
      render: (fila) => (
        <Button
          size="sm"
          className="bg-blue-600 hover:bg-blue-700"
          onClick={() =>
            onGestionar
              ? onGestionar(fila)
              : navigate(`/vinculacion/${fila.asignacionId}`)
          }
          aria-label={`Gestionar vinculación de ${fila.estudiante?.nombre}`}
        >
          Gestionar
        </Button>
      ),
    },
  ];

  return (
    <TablaBase
      columnas={columnas}
      datos={vinculaciones}
      cargando={isLoading}
      sinDatos="No hay procesos de vinculación que coincidan con los filtros."
    />
  );
}
