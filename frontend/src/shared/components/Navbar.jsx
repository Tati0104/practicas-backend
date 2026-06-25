import { Menu } from 'lucide-react';
import useAuth from '../hooks/useAuth';
import NotificacionesBell from './NotificacionesBell';
import AppLogo from './AppLogo';
import ThemeToggle from './ThemeToggle';
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
    <header className="sticky top-0 z-30 flex h-14 items-center justify-between border-b border-gray-200 bg-white/95 px-4 backdrop-blur-sm dark:border-white/[0.06] dark:bg-dark-base/90 sm:px-6 lg:-ml-5 lg:pl-10">
      <div className="flex items-center gap-3">
        <button
          type="button"
          onClick={onAbrirMenu}
          className="rounded-lg p-2 text-gray-600 hover:bg-gray-100 dark:text-slate-400 dark:hover:bg-white/[0.06] lg:hidden"
          aria-label="Abrir menú"
        >
          <Menu className="h-5 w-5" />
        </button>
        <div className="flex items-center gap-2 lg:hidden">
          <AppLogo variant="dark" className="h-8 w-8" />
          <div className="min-w-0 leading-tight">
            <span className="block text-sm font-bold text-primary">PracTI</span>
            <span className="block whitespace-nowrap text-[9px] font-medium uppercase tracking-wider text-gray-500 dark:text-slate-400">
              Prácticas empresariales
            </span>
          </div>
        </div>
        <span className="hidden text-sm text-gray-600 dark:text-slate-300 sm:inline">
          Bienvenido/a, <strong className="font-semibold text-gray-900 dark:text-white">{nombreMostrar}</strong>
        </span>
        <span className="text-sm text-gray-600 dark:text-slate-300 sm:hidden">
          <strong className="font-semibold text-gray-900 dark:text-white">{nombreMostrar}</strong>
        </span>
      </div>

      <div className="flex items-center gap-2 sm:gap-3">
        <ThemeToggle />
        <NotificacionesBell />
        <span className="rounded-full bg-primary/10 px-3 py-1 text-xs font-medium text-primary dark:bg-primary/25 dark:text-primary-glow">
          {rolMostrar}
        </span>
      </div>
    </header>
  );
}
