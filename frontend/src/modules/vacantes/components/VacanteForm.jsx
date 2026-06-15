import { useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { zodResolver } from '@hookform/resolvers/zod';
import { toast } from 'react-hot-toast';
import { useQuery } from '@tanstack/react-query';
import { useVacantesMutaciones } from '../hooks/useVacantesMutaciones';
import empresaService from '../../empresa/services/empresaService';
import http from '../../../shared/services/http';

const vacanteSchema = z.object({
  empresaId:               z.coerce.number().int().positive('Empresa es requerida'),
  programaId:              z.coerce.number().int().positive('Programa es requerido'),
  cargo:                   z.string().min(1, 'Cargo es requerido'),
  descripcionPerfil:       z.string().min(1, 'Descripción del perfil es requerida'),
  modalidad:               z.enum(['PRESENCIAL', 'REMOTO', 'HÍBRIDO'], { errorMap: () => ({ message: 'Modalidad es requerida' }) }),
  cuposTotales:            z.coerce.number().int().min(1, 'Debe ser al menos 1'),
  area:                    z.string().optional(),
  requisitos:              z.string().optional(),
  fechaInicioDisponibilidad: z.string().optional(),
  fechaFinDisponibilidad:    z.string().optional(),
});

export default function VacanteForm({ isOpen, onClose, vacante }) {
  const isEdit = !!vacante?.id;

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

  const { register, handleSubmit, reset, formState: { errors, isSubmitting } } = useForm({
    resolver: zodResolver(vacanteSchema),
    defaultValues: vacante ?? {},
  });

  const { crear, editar } = useVacantesMutaciones({
    onSuccess: () => {
      toast.success(isEdit ? 'Vacante actualizada' : 'Vacante creada');
      onClose();
    },
    onError: err => toast.error(err?.response?.data?.message ?? err?.message ?? 'Error al guardar'),
  });

  useEffect(() => {
    if (isOpen) reset(vacante ?? {});
  }, [isOpen, vacante, reset]);

  const onSubmit = data => {
    if (isEdit) {
      editar.mutate({ id: vacante.id, dto: data });
    } else {
      crear.mutate(data);
    }
  };

  if (!isOpen) return null;

  const fldError = (key) => errors[key] && (
    <p className="text-red-600 text-sm mt-1">{errors[key].message}</p>
  );

  return (
    <div className="fixed inset-0 flex items-center justify-center bg-black/40 z-50">
      <div className="bg-white rounded-lg w-full max-w-lg p-6 shadow-lg overflow-y-auto max-h-[90vh]">
        <h2 className="text-xl font-semibold mb-4">
          {isEdit ? 'Editar vacante' : 'Crear vacante'}
        </h2>

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">

          {/* Empresa */}
          <div>
            <label className="block text-sm font-medium mb-1">Empresa</label>
            <select {...register('empresaId')} className="w-full border rounded px-3 py-2">
              <option value="">— Selecciona empresa —</option>
              {empresas.map(e => (
                <option key={e.id} value={e.id}>{e.razonSocial ?? e.nombre ?? `Empresa ${e.id}`}</option>
              ))}
            </select>
            {fldError('empresaId')}
          </div>

          {/* Programa */}
          <div>
            <label className="block text-sm font-medium mb-1">Programa</label>
            <select {...register('programaId')} className="w-full border rounded px-3 py-2">
              <option value="">— Selecciona programa —</option>
              {programas.map(p => (
                <option key={p.id} value={p.id}>{p.nombre}</option>
              ))}
            </select>
            {fldError('programaId')}
          </div>

          {/* Cargo */}
          <div>
            <label className="block text-sm font-medium mb-1">Cargo</label>
            <input {...register('cargo')} className="w-full border rounded px-3 py-2" placeholder="Ej: Desarrollador Backend" />
            {fldError('cargo')}
          </div>

          {/* Descripción del perfil */}
          <div>
            <label className="block text-sm font-medium mb-1">Descripción del perfil</label>
            <textarea {...register('descripcionPerfil')} rows={3}
              className="w-full border rounded px-3 py-2 resize-none"
              placeholder="Describe el perfil requerido para el cargo..." />
            {fldError('descripcionPerfil')}
          </div>

          {/* Modalidad */}
          <div>
            <label className="block text-sm font-medium mb-1">Modalidad</label>
            <select {...register('modalidad')} className="w-full border rounded px-3 py-2">
              <option value="">— Selecciona modalidad —</option>
              <option value="PRESENCIAL">Presencial</option>
              <option value="REMOTO">Remoto</option>
              <option value="HÍBRIDO">Híbrido</option>
            </select>
            {fldError('modalidad')}
          </div>

          {/* Cupos totales */}
          <div>
            <label className="block text-sm font-medium mb-1">Cupos totales</label>
            <input type="number" min={1} {...register('cuposTotales')}
              className="w-full border rounded px-3 py-2" placeholder="Ej: 3" />
            {fldError('cuposTotales')}
          </div>

          {/* Área (opcional) */}
          <div>
            <label className="block text-sm font-medium mb-1">
              Área <span className="text-gray-400 font-normal">(opcional)</span>
            </label>
            <input {...register('area')} className="w-full border rounded px-3 py-2"
              placeholder="Ej: Tecnología, Administración..." />
          </div>

          {/* Requisitos (opcional) */}
          <div>
            <label className="block text-sm font-medium mb-1">
              Requisitos <span className="text-gray-400 font-normal">(opcional)</span>
            </label>
            <textarea {...register('requisitos')} rows={2}
              className="w-full border rounded px-3 py-2 resize-none"
              placeholder="Conocimientos, habilidades, etc." />
          </div>

          {/* Fechas (opcionales) */}
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