import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import empresaService from '../services/empresaService';

const MOCK_EMPRESAS = [
  { id: 1, nit: '900123456-1', razonSocial: 'Tech Solutions SAS',   sector: 'TECNOLOGIA',  municipio: 'Armenia',   activo: true  },
  { id: 2, nit: '800234567-2', razonSocial: 'Constructora ABC',     sector: 'CONSTRUCCION', municipio: 'Calarcá',   activo: true  },
  { id: 3, nit: '700345678-3', razonSocial: 'Agro Valle Ltda',      sector: 'AGRICULTURA', municipio: 'Montenegro', activo: false },
];

const MOCK_VACANTES = [
  { id: 1, empresa: 'Tech Solutions SAS', cargo: 'Desarrollador Backend',  modalidad: 'PRESENCIAL', cuposTotal: 2, cuposDisponibles: 1, estado: 'ACTIVA' },
  { id: 2, empresa: 'Constructora ABC',   cargo: 'Ing. Residente de Obra', modalidad: 'PRESENCIAL', cuposTotal: 1, cuposDisponibles: 1, estado: 'PENDIENTE_APROBACION' },
  { id: 3, empresa: 'Tech Solutions SAS', cargo: 'Analista de Datos',      modalidad: 'REMOTO',     cuposTotal: 3, cuposDisponibles: 0, estado: 'CUPOS_COMPLETOS' },
];

export function useEmpresas() {
  const queryClient           = useQueryClient();
  const [filtros, setFiltros] = useState({});

  const { data, isLoading } = useQuery({
    queryKey:    ['empresas', filtros],
    queryFn:     () => empresaService.listar(filtros).then(r => r.data.data?.content || []),
    initialData: MOCK_EMPRESAS
  });

  const registrar = useMutation({
    mutationFn: (dto) => empresaService.registrar(dto),
    onSuccess:  () => queryClient.invalidateQueries(['empresas'])
  });

  const activar = useMutation({
    mutationFn: (id) => empresaService.activar(id),
    onSuccess:  () => queryClient.invalidateQueries(['empresas'])
  });

  const inactivar = useMutation({
    mutationFn: (id) => empresaService.inactivar(id),
    onSuccess:  () => queryClient.invalidateQueries(['empresas'])
  });

  return { empresas: data || [], isLoading, filtros, setFiltros,
           registrar, activar, inactivar };
}

export function useVacantes() {
  const queryClient           = useQueryClient();
  const [filtros, setFiltros] = useState({});

  const { data, isLoading } = useQuery({
    queryKey:    ['vacantes', filtros],
    queryFn:     () => empresaService.listarVacantes(filtros).then(r => r.data.data?.content || []),
    initialData: MOCK_VACANTES
  });

  const aprobar = useMutation({
    mutationFn: (id) => empresaService.aprobarVacante(id),
    onSuccess:  () => queryClient.invalidateQueries(['vacantes'])
  });

  const rechazar = useMutation({
    mutationFn: ({ id, motivo }) => empresaService.rechazarVacante(id, motivo),
    onSuccess:  () => queryClient.invalidateQueries(['vacantes'])
  });

  return { vacantes: data || [], isLoading, filtros, setFiltros, aprobar, rechazar };
}