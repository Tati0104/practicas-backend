import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import usuarioService from '../services/usuarioService';

const MOCK_USUARIOS = [
  { id: 1, nombre: 'Admin AVH',        correo: 'admin@avh.edu.co',     rol: 'ADMIN',           scope: 'GLOBAL',   activo: true  },
  { id: 2, nombre: 'Coord Práctica',   correo: 'coord@avh.edu.co',     rol: 'COORD_PRACTICA',  scope: 'PROGRAMA', activo: true  },
  { id: 3, nombre: 'Coord Académica',  correo: 'academica@avh.edu.co', rol: 'COORD_ACADEMICA', scope: 'FACULTAD', activo: true  },
  { id: 4, nombre: 'Docente Asesor',   correo: 'docente@avh.edu.co',   rol: 'DOCENTE_ASESOR',  scope: 'ASIGNADO', activo: false },
];

export default function useUsuarios() {
  const queryClient         = useQueryClient();
  const [filtros, setFiltros] = useState({});

  const { data, isLoading } = useQuery({
    queryKey:    ['usuarios', filtros],
    queryFn:     () => usuarioService.listar(filtros).then(r => r.data.data?.content || []),
    initialData: MOCK_USUARIOS
  });

  const crear = useMutation({
    mutationFn: (dto) => usuarioService.crear(dto),
    onSuccess:  () => queryClient.invalidateQueries(['usuarios'])
  });

  const editar = useMutation({
    mutationFn: ({ id, dto }) => usuarioService.editar(id, dto),
    onSuccess:  () => queryClient.invalidateQueries(['usuarios'])
  });

  const activar = useMutation({
    mutationFn: (id) => usuarioService.activar(id),
    onSuccess:  () => queryClient.invalidateQueries(['usuarios'])
  });

  const inactivar = useMutation({
    mutationFn: (id) => usuarioService.inactivar(id),
    onSuccess:  () => queryClient.invalidateQueries(['usuarios'])
  });

  return { usuarios: data || [], isLoading, filtros, setFiltros, crear, editar, activar, inactivar };
}