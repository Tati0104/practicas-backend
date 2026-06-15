import { Menu } from 'lucide-react';
import useAuth from '../hooks/useAuth';

const nombreRol = {
  ADMIN: 'Administrador',
  DIRECCION: 'Dirección',
  COORD_ACADEMICA: 'Coordinación Académica',
  COORD_PRACTICA: 'Coordinador de Práctica',
  SECRETARIA: 'Secretaría',
  DOCENTE_ASESOR: 'Docente Asesor',
  EMPRESA: 'Empresa',
  TUTOR_EMPRESARIAL: 'Tutor Empresarial',
  ESTUDIANTE: 'Estudiante',
};

export default function Navbar({ onAbrirMenu }) {
  const { usuario, nombre, rol } = useAuth();
  const nombreMostrar = nombre ?? usuario?.nombre ?? 'Usuario';
  const rolMostrar = nombreRol[rol ?? usuario?.rol] ?? rol ?? usuario?.rol;

  return (
    <header className="sticky top-0 z-30 flex h-14 items-center justify-between border-b border-gray-200 bg-white px-4 sm:px-6 lg:-ml-5 lg:pl-10">
      <div className="flex items-center gap-3">
        <button
          type="button"
          onClick={onAbrirMenu}
          className="rounded-lg p-2 text-gray-600 hover:bg-gray-100 lg:hidden"
          aria-label="Abrir menú"
        >
          <Menu className="h-5 w-5" />
        </button>
        <span className="hidden text-sm text-gray-600 sm:inline">
          Bienvenido/a, <strong className="font-semibold text-gray-900">{nombreMostrar}</strong>
        </span>
        <span className="text-sm text-gray-600 sm:hidden">
          <strong className="font-semibold text-gray-900">{nombreMostrar}</strong>
        </span>
      </div>

      <span className="rounded-full bg-primary/10 px-3 py-1 text-xs font-medium text-primary">
        {rolMostrar}
      </span>
    </header>
  );
}
