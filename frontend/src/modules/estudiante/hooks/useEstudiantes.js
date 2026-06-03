import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import estudianteService from '../services/estudianteService';

const MOCK_ESTUDIANTES = [
  { id: 1, nombre: 'Ana García',    identificacion: '1001234567', correo: 'ana@avh.edu.co',   programa: 'Ing. Sistemas',  semestre: 8, estadoAptitud: 'APTO',        activo: true },
  { id: 2, nombre: 'Luis Martínez', identificacion: '1002345678', correo: 'luis@avh.edu.co',  programa: 'Ing. Civil',     semestre: 7, estadoAptitud: 'SIN_EVALUAR', activo: true },
  { id: 3, nombre: 'María López',   identificacion: '1003456789', correo: 'maria@avh.edu.co', programa: 'Ing. Sistemas',  semestre: 9, estadoAptitud: 'NO_APTO',     activo: true },
  { id: 4, nombre: 'Carlos Ruiz',   identificacion: '1004567890', correo: 'carlos@avh.edu.co',programa: 'Administración', semestre: 8, estadoAptitud: 'APTO',        activo: false },
];

export default function useEstudiantes() {
  const queryClient           = useQueryClient();
  const [filtros, setFiltros] = useState({});

  const { data, isLoading } = useQuery({
    queryKey:    ['estudiantes', filtros],
    queryFn:     () => estudianteService.listar(filtros).then(r => r.data.data?.content || []),
    initialData: MOCK_ESTUDIANTES
  });

  const registrar = useMutation({
    mutationFn: (dto) => estudianteService.registrar(dto),
    onSuccess:  () => queryClient.invalidateQueries(['estudiantes'])
  });

  const marcarApto = useMutation({
    mutationFn: (id) => estudianteService.marcarApto(id),
    onSuccess:  () => queryClient.invalidateQueries(['estudiantes'])
  });

  const marcarNoApto = useMutation({
    mutationFn: ({ id, motivo }) => estudianteService.marcarNoApto(id, motivo),
    onSuccess:  () => queryClient.invalidateQueries(['estudiantes'])
  });

  const importarExcel = useMutation({
    mutationFn: (archivo) => estudianteService.importarExcel(archivo),
    onSuccess:  () => queryClient.invalidateQueries(['estudiantes'])
  });

  return { estudiantes: data || [], isLoading, filtros, setFiltros,
           registrar, marcarApto, marcarNoApto, importarExcel };
}