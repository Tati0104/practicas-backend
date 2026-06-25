import { useEffect, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { toast } from 'react-hot-toast';
import { Button, Card, Input, LoadingState, PageHeader } from '@/shared/components/ui';
import plantillaService from '../services/plantillaService';
import { TIPOS_EVENTO_CORREO } from '../config/tiposEvento';

export default function PlantillasCorreoPage() {
  const [tipoSeleccionado, setTipoSeleccionado] = useState(TIPOS_EVENTO_CORREO[0].codigo);
  const [asunto, setAsunto] = useState('');
  const [cuerpo, setCuerpo] = useState('');
  const queryClient = useQueryClient();

  const { data: plantilla, isLoading, isError } = useQuery({
    queryKey: ['plantilla-correo', tipoSeleccionado],
    queryFn: () => plantillaService.obtener(tipoSeleccionado).then((r) => r.data),
  });

  useEffect(() => {
    if (plantilla) {
      setAsunto(plantilla.asunto ?? '');
      setCuerpo(plantilla.cuerpo ?? '');
    }
  }, [plantilla, tipoSeleccionado]);

  const guardar = useMutation({
    mutationFn: () => plantillaService.guardar(tipoSeleccionado, { asunto, cuerpo }),
    onSuccess: () => {
      queryClient.invalidateQueries(['plantilla-correo', tipoSeleccionado]);
      toast.success('Plantilla guardada correctamente');
    },
    onError: () => toast.error('No se pudo guardar la plantilla'),
  });

  return (
    <div>
      <PageHeader
        titulo="Plantillas de correo"
        descripcion="Administra los mensajes automáticos enviados por el sistema."
      />

      <div className="grid gap-6 lg:grid-cols-[240px_1fr]">
        <Card padding="p-3">
          <p className="mb-2 px-2 text-xs font-semibold uppercase tracking-wide text-gray-400">
            Eventos
          </p>
          <ul className="space-y-0.5">
            {TIPOS_EVENTO_CORREO.map(({ codigo, nombre }) => (
              <li key={codigo}>
                <button
                  type="button"
                  onClick={() => setTipoSeleccionado(codigo)}
                  className={[
                    'w-full rounded-lg px-3 py-2 text-left text-sm transition-colors',
                    tipoSeleccionado === codigo
                      ? 'bg-primary font-medium text-white'
                      : 'text-gray-700 hover:bg-gray-100',
                  ].join(' ')}
                >
                  {nombre}
                </button>
              </li>
            ))}
          </ul>
        </Card>

        <Card>
          {isLoading && <LoadingState mensaje="Cargando plantilla..." />}
          {isError && <p className="text-sm text-red-600">Error al cargar la plantilla.</p>}

          {!isLoading && !isError && (
            <form
              onSubmit={(e) => {
                e.preventDefault();
                guardar.mutate();
              }}
              className="space-y-4"
            >
              <div>
                <label htmlFor="asunto" className="mb-1 block text-sm font-medium text-gray-700">
                  Asunto
                </label>
                <Input
                  id="asunto"
                  name="asunto"
                  type="text"
                  value={asunto}
                  onChange={(e) => setAsunto(e.target.value)}
                  required
                />
              </div>

              <div>
                <label htmlFor="cuerpo" className="mb-1 block text-sm font-medium text-gray-700">
                  Cuerpo (HTML permitido)
                </label>
                <textarea
                  id="cuerpo"
                  name="cuerpo"
                  rows={12}
                  value={cuerpo}
                  onChange={(e) => setCuerpo(e.target.value)}
                  className="w-full rounded-lg border border-gray-300 px-3 py-2 font-mono text-sm outline-none focus:border-primary focus:ring-2 focus:ring-primary/20"
                  required
                />
              </div>

              <Button type="submit" disabled={guardar.isPending}>
                {guardar.isPending ? 'Guardando...' : 'Guardar plantilla'}
              </Button>
            </form>
          )}
        </Card>
      </div>
    </div>
  );
}
