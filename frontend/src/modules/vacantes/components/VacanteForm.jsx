import { useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { zodResolver } from '@hookform/resolvers/zod';
import { toast } from 'react-hot-toast';
import { useQuery } from '@tanstack/react-query';
import useAuthStore from '@/store/authStore';
import { useVacantesMutaciones } from '../hooks/useVacantesMutaciones';
import empresaService from '../../empresa/services/empresaService';
import configuracionService from '../../configuracion/services/configuracionService';
import http from '../../../shared/services/http';

const vacanteSchema = z.object({
  empresaId: z.preprocess(
    (value) => (value === '' || value == null ? undefined : value),
    z.coerce.number().int().positive('Empresa es requerida').optional()
  ),
  programaId: z.coerce.number().int().positive('Programa es requerido'),
  cargo: z.string().min(1, 'Cargo es requerido'),
  descripcionPerfil: z.string().min(1, 'Descripcion del perfil es requerida'),
  modalidad: z.enum(['PRESENCIAL', 'REMOTO', 'HÍBRIDO'], {
    errorMap: () => ({ message: 'Modalidad es requerida' }),
  }),
  cuposTotales: z.coerce.number().int().min(1, 'Debe ser al menos 1'),
  catalogoPracticaId: z.preprocess(
    (v) => (v === '' || v == null ? null : v),
    z.coerce.number().int().positive().nullable().optional()
  ),
  area: z.string().optional(),
  requisitos: z.string().optional(),
  fechaInicioDisponibilidad: z.string().optional(),
  fechaFinDisponibilidad: z.string().optional(),
});

export default function VacanteForm({ isOpen, onClose, vacante }) {
  const isEdit = !!vacante?.id;
  const rol = useAuthStore((state) => state.rol);
  const esEmpresa = rol === 'EMPRESA';
  const puedeVerCatalogo = rol === 'COORD_PRACTICA' || rol === 'ADMIN';

  const { data: empresas = [] } = useQuery({
    queryKey: ['empresas-select-vacante'],
    queryFn: async () => {
      const r = await empresaService.listar({ page: 0, size: 500, activo: true });
      return r.data?.content ?? [];
    },
    enabled: isOpen,
    staleTime: 60_000,
  });

  const { data: programas = [] } = useQuery({
    queryKey: ['programas-select'],
    queryFn: () => http.get('/programas').then(r => r.data ?? []),
    enabled: isOpen,
  });

  const { register, handleSubmit, reset, setError, watch, formState: { errors, isSubmitting } } = useForm({
    resolver: zodResolver(vacanteSchema),
    defaultValues: vacante ?? {},
  });

  const programaIdActual = watch('programaId');
  const { data: catalogoPracticas = [] } = useQuery({
    queryKey: ['catalogo-practicas-select', programaIdActual],
    queryFn: () =>
      configuracionService
        .listarCatalogoPracticas(programaIdActual)
        .then((r) => (r.data ?? []).filter((p) => p.activo)),
    enabled: isOpen && !!programaIdActual && puedeVerCatalogo,
    staleTime: 60_000,
  });

  const { crear, editar } = useVacantesMutaciones({
    onSuccess: () => {
      toast.success(isEdit ? 'Vacante actualizada' : 'Vacante creada');
      onClose();
    },
    onError: err => toast.error(err?.response?.data?.message ?? err?.message ?? 'Error al guardar'),
  });

  useEffect(() => {
    if (!isOpen) return;
    const empresaPropia = esEmpresa ? empresas[0] : null;
    reset({
      ...(vacante ?? {}),
      ...(empresaPropia ? { empresaId: empresaPropia.id } : {}),
    });
  }, [isOpen, vacante, reset, esEmpresa, empresas]);

  const onSubmit = data => {
    if (!esEmpresa && !data.empresaId) {
      setError('empresaId', { type: 'manual', message: 'Empresa es requerida' });
      return;
    }

    const payload = {
      ...data,
      catalogoPracticaId: data.catalogoPracticaId ? Number(data.catalogoPracticaId) : null,
      fechaInicioDisponibilidad: data.fechaInicioDisponibilidad || null,
      fechaFinDisponibilidad: data.fechaFinDisponibilidad || null,
      ...(esEmpresa && empresas[0] ? { empresaId: empresas[0].id } : {}),
    };
    if (isEdit) {
      editar.mutate({ id: vacante.id, dto: payload });
    } else {
      crear.mutate(payload);
    }
  };

  if (!isOpen) return null;

  const fldError = (key) => errors[key] && (
    <p className="text-red-600 text-sm mt-1">{errors[key].message}</p>
  );

  const nombreEmpresa = empresas[0]?.razonSocial ?? empresas[0]?.nombre ?? (empresas[0]?.id ? `Empresa ${empresas[0].id}` : 'Empresa asociada');

  return (
    <div className="fixed inset-0 flex items-center justify-center bg-black/40 z-50">
      <div className="bg-white rounded-lg w-full max-w-lg p-6 shadow-lg overflow-y-auto max-h-[90vh]">
        <h2 className="text-xl font-semibold mb-4">
          {isEdit ? 'Editar vacante' : 'Crear vacante'}
        </h2>

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div>
            <label className="block text-sm font-medium mb-1">Empresa</label>
            {esEmpresa ? (
              <>
                <input type="hidden" {...register('empresaId')} />
                <div className="w-full border rounded px-3 py-2 bg-gray-50 text-gray-700">
                  {nombreEmpresa}
                </div>
              </>
            ) : (
              <select {...register('empresaId')} className="w-full border rounded px-3 py-2">
                <option value="">- Selecciona empresa -</option>
                {empresas.map(e => (
                  <option key={e.id} value={e.id}>{e.razonSocial ?? e.nombre ?? `Empresa ${e.id}`}</option>
                ))}
              </select>
            )}
            {fldError('empresaId')}
          </div>

          <div>
            <label className="block text-sm font-medium mb-1">Programa</label>
            <select {...register('programaId')} className="w-full border rounded px-3 py-2">
              <option value="">- Selecciona programa -</option>
              {programas.map(p => (
                <option key={p.id} value={p.id}>{p.nombre}</option>
              ))}
            </select>
            {fldError('programaId')}
          </div>

          {puedeVerCatalogo && (
            <div>
              <label className="block text-sm font-medium mb-1">
                Práctica <span className="text-gray-400 font-normal">(opcional)</span>
              </label>
              <select
                {...register('catalogoPracticaId')}
                disabled={!programaIdActual}
                className="w-full border rounded px-3 py-2 disabled:bg-gray-50 disabled:text-gray-400"
              >
                <option value="">- Selecciona práctica -</option>
                {catalogoPracticas.map((p) => (
                  <option key={p.id} value={p.id}>
                    Práctica {p.numeroPractica} — {p.nombre}
                  </option>
                ))}
              </select>
              {!programaIdActual && (
                <p className="text-gray-400 text-xs mt-1">Selecciona un programa primero</p>
              )}
            </div>
          )}

          <div>
            <label className="block text-sm font-medium mb-1">Cargo</label>
            <input {...register('cargo')} className="w-full border rounded px-3 py-2" placeholder="Ej: Desarrollador Backend" />
            {fldError('cargo')}
          </div>

          <div>
            <label className="block text-sm font-medium mb-1">Descripcion del perfil</label>
            <textarea {...register('descripcionPerfil')} rows={3}
              className="w-full border rounded px-3 py-2 resize-none"
              placeholder="Describe el perfil requerido para el cargo..." />
            {fldError('descripcionPerfil')}
          </div>

          <div>
            <label className="block text-sm font-medium mb-1">Modalidad</label>
            <select {...register('modalidad')} className="w-full border rounded px-3 py-2">
              <option value="">- Selecciona modalidad -</option>
              <option value="PRESENCIAL">Presencial</option>
              <option value="REMOTO">Remoto</option>
              <option value="HÍBRIDO">Híbrido</option>
            </select>
            {fldError('modalidad')}
          </div>

          <div>
            <label className="block text-sm font-medium mb-1">Cupos totales</label>
            <input type="number" min={1} {...register('cuposTotales')}
              className="w-full border rounded px-3 py-2" placeholder="Ej: 3" />
            {fldError('cuposTotales')}
          </div>

          <div>
            <label className="block text-sm font-medium mb-1">
              Area <span className="text-gray-400 font-normal">(opcional)</span>
            </label>
            <input {...register('area')} className="w-full border rounded px-3 py-2"
              placeholder="Ej: Tecnologia, Administracion..." />
          </div>

          <div>
            <label className="block text-sm font-medium mb-1">
              Requisitos <span className="text-gray-400 font-normal">(opcional)</span>
            </label>
            <textarea {...register('requisitos')} rows={2}
              className="w-full border rounded px-3 py-2 resize-none"
              placeholder="Conocimientos, habilidades, etc." />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium mb-1">Disponible desde</label>
              <input type="date" {...register('fechaInicioDisponibilidad')}
                className="w-full border rounded px-3 py-2" />
            </div>
            <div>
              <label className="block text-sm font-medium mb-1">Disponible hasta</label>
              <input type="date" {...register('fechaFinDisponibilidad')}
                className="w-full border rounded px-3 py-2" />
            </div>
          </div>

          <div className="flex justify-end space-x-2 pt-2">
            <button type="button" onClick={() => { onClose(); reset(); }}
              className="px-4 py-2 bg-gray-200 text-gray-800 rounded">
              Cancelar
            </button>
            <button type="submit" disabled={isSubmitting}
              className="px-4 py-2 bg-blue-600 text-white rounded disabled:opacity-60">
              {isEdit ? 'Actualizar' : 'Crear'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
