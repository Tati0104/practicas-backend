import { useState } from 'react';
import toast from 'react-hot-toast';
import useEstudiantes from '../hooks/useEstudiantes';
import { extraerMensajeError } from '@/modules/auth/utils/schemas';
import FiltrosEstudiante from './FiltrosEstudiante';
import ModalRegistroEstudiante from './ModalRegistroEstudiante';
import ModalMarcarApto from './ModalMarcarApto';
import ModalExpedienteEstudiante from './ModalExpedienteEstudiante';
import ImportarExcel from './ImportarExcel';
import TablaBase from '../../../shared/components/TablaBase';
import Paginacion from '../../../shared/components/Paginacion';
import { Badge, Button, PageHeader } from '@/shared/components/ui';
import { Check, X } from 'lucide-react';

const variantAptitud = {
  APTO: 'success',
  NO_APTO: 'danger',
  SIN_EVALUAR: 'warning',
};

function BadgeDocumentoBase({ activo, label }) {
  return (
    <span
      className={[
        'inline-block rounded-full px-2.5 py-0.5 text-[11px] font-semibold',
        activo ? 'bg-emerald-100 text-emerald-800' : 'bg-gray-100 text-gray-500',
      ].join(' ')}
    >
      {label}
    </span>
  );
}

export default function EstudiantesPage() {
  const {
    estudiantes,
    isLoading,
    isError,
    filtros,
    setFiltros,
    totalPaginas,
    irAPagina,
    registrar,
    editar,
    marcarApto,
    marcarNoApto,
    eliminar,
  } = useEstudiantes();
  const [modalRegistro, setModalRegistro] = useState(false);
  const [editando, setEditando] = useState(null);
  const [modalImportar, setModalImportar] = useState(false);
  const [estudianteApto, setEstudianteApto] = useState(null);
  const [estudianteExpediente, setEstudianteExpediente] = useState(null);

  const abrirRegistrar = () => {
    setEditando(null);
    setModalRegistro(true);
  };

  const abrirEditar = (estudiante) => {
    setEditando(estudiante);
    setModalRegistro(true);
  };

  const cerrarModal = () => {
    setModalRegistro(false);
    setEditando(null);
  };

  const guardando = registrar.isPending || editar.isPending;

  const confirmarEliminar = (estudiante) => {
    if (
      !window.confirm(
        `¿Eliminar al estudiante ${estudiante.nombre}? Esta acción no se puede deshacer.`,
      )
    ) {
      return;
    }
    eliminar.mutate(estudiante.id, {
      onError: (error) => toast.error(extraerMensajeError(error)),
    });
  };

  const columnas = [
    { key: 'nombre', titulo: 'Nombre' },
    { key: 'identificacion', titulo: 'Identificación' },
    { key: 'programa', titulo: 'Programa', render: (e) => e.programa?.nombre ?? '—' },
    { key: 'semestre', titulo: 'Semestre' },
    {
      key: 'estadoAptitud',
      titulo: 'Apto',
      render: (e) => (
        <Badge variant={variantAptitud[e.estadoAptitud] ?? 'warning'}>
          {e.estadoAptitud}
        </Badge>
      ),
    },
    {
      key: 'documentos',
      titulo: 'Documentos base',
      render: (e) => {
        const tieneHojaVida = e.documentos?.some((d) => d.tipo === 'HOJA_DE_VIDA');
        const tienePazSalvo = e.documentos?.some((d) => d.tipo === 'PAZ_Y_SALVO');
        return (
          <div className="flex flex-wrap gap-1">
            <BadgeDocumentoBase activo={tieneHojaVida} label="HV" />
            <BadgeDocumentoBase activo={tienePazSalvo} label="P&S" />
          </div>
        );
      },
    },
    {
      key: 'acciones',
      titulo: 'Acciones',
      sticky: true,
      className: 'min-w-[320px] whitespace-nowrap',
      render: (e) => (
        <div className="flex flex-nowrap items-center gap-1.5">
          <Button variant="ghost" size="sm" onClick={() => setEstudianteExpediente(e)}>
            Prácticas
          </Button>
          <Button variant="info" size="sm" onClick={() => abrirEditar(e)}>
            Editar
          </Button>
          <Button variant="danger" size="sm" onClick={() => confirmarEliminar(e)}>
            Eliminar
          </Button>
          <Button
            variant="success"
            size="sm"
            title={e.estadoAptitud === 'APTO' ? 'Habilitar otra práctica' : 'Marcar como Apto'}
            onClick={() => setEstudianteApto(e)}
          >
            <Check size={16} />
          </Button>
          {e.estadoAptitud !== 'NO_APTO' && (
            <Button
              variant="ghost"
              size="sm"
              title="Marcar como No Apto"
              onClick={() => marcarNoApto.mutate({ id: e.id, motivo: 'Sin requisitos' })}
            >
              <X size={16} />
            </Button>
          )}
        </div>
      ),
    },
  ];

  return (
    <div>
      <PageHeader
        titulo="Gestión de estudiantes"
        acciones={
          <>
            <Button variant="success" onClick={() => setModalImportar(true)}>
              Importar Excel
            </Button>
            <Button onClick={abrirRegistrar}>+ Registrar</Button>
          </>
        }
      />

      <FiltrosEstudiante filtros={filtros} onChange={setFiltros} />
      {isError && (
        <div className="mb-4 rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-800">
          No se pudieron cargar los estudiantes. Verifica tu sesión y que tu usuario tenga facultad
          asignada.
        </div>
      )}
      <TablaBase columnas={columnas} datos={estudiantes} cargando={isLoading} />
      <Paginacion pagina={filtros.page} totalPaginas={totalPaginas} onCambiarPagina={irAPagina} />

      {modalRegistro && (
        <ModalRegistroEstudiante
          estudiante={editando}
          guardando={guardando}
          onGuardar={(dto) => {
            if (editando) {
              editar.mutate({ id: editando.id, dto }, { onSuccess: cerrarModal });
            } else {
              registrar.mutate(dto, { onSuccess: cerrarModal });
            }
          }}
          onCerrar={cerrarModal}
        />
      )}
      {modalImportar && (
        <ImportarExcel
          onImportar={() => setModalImportar(false)}
          onCerrar={() => setModalImportar(false)}
        />
      )}
      {estudianteApto && (
        <ModalMarcarApto
          estudiante={estudianteApto}
          guardando={marcarApto.isPending}
          onCerrar={() => setEstudianteApto(null)}
          onConfirmar={(numeroPractica) =>
            marcarApto.mutate(
              { id: estudianteApto.id, numeroPractica },
              { onSuccess: () => setEstudianteApto(null) }
            )
          }
        />
      )}
      {estudianteExpediente && (
        <ModalExpedienteEstudiante
          estudiante={estudianteExpediente}
          onCerrar={() => setEstudianteExpediente(null)}
        />
      )}
    </div>
  );
}
