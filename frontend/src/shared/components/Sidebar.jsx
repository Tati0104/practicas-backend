import { useState } from 'react';
import { NavLink, useLocation, useNavigate } from 'react-router-dom';
import {
  Activity,
  BarChart3,
  BookOpen,
  Briefcase,
  Building2,
  CheckSquare,
  ChevronDown,
  ClipboardList,
  FileText,
  GraduationCap,
  LayoutDashboard,
  KeyRound,
  Link2,
  LogOut,
  Mail,
  Star,
  Users,
  X,
} from 'lucide-react';
import useAuth from '../hooks/useAuth';
import useNavegacion from '../hooks/useNavegacion';
import { esRutaActiva } from '../config/navegacion';

const ICONOS = {
  LayoutDashboard,
  Users,
  Building2,
  BookOpen,
  GraduationCap,
  Briefcase,
  ClipboardList,
  Link2,
  FileText,
  Activity,
  Star,
  CheckSquare,
  BarChart3,
  Mail,
};

function IconoMenu({ nombre, className = 'h-4 w-4 shrink-0' }) {
  const Icon = ICONOS[nombre] ?? LayoutDashboard;
  return <Icon className={className} aria-hidden="true" />;
}

function clasesEnlace(activo, anidado = false) {
  const base =
    'flex min-w-0 items-center gap-2.5 py-2.5 text-[11px] font-semibold uppercase tracking-wider transition-colors duration-150';

  if (activo) {
    return [base, 'nav-item-active', anidado ? 'nav-item-active--nested' : ''].filter(Boolean).join(' ');
  }

  return [
    base,
    'mx-3 rounded-lg px-3 text-white/75 hover:bg-white/10 hover:text-white',
  ].join(' ');
}

function EnlaceMenu({ item, onNavigate, anidado = false }) {
  const { pathname } = useLocation();
  const activo = esRutaActiva(item, pathname);

  return (
    <NavLink
      to={item.ruta}
      onClick={onNavigate}
      className={clasesEnlace(activo, anidado)}
    >
      <IconoMenu nombre={item.icono} />
      <span className="min-w-0 truncate">{item.nombre}</span>
    </NavLink>
  );
}

function GrupoMenu({ grupo, onNavigate }) {
  const { pathname } = useLocation();
  const tieneActivo = grupo.items.some((item) => esRutaActiva(item, pathname));
  const [abierto, setAbierto] = useState(tieneActivo || grupo.items.length > 1);

  if (grupo.items.length === 1) {
    return <EnlaceMenu item={grupo.items[0]} onNavigate={onNavigate} />;
  }

  return (
    <div className="mb-0.5 min-w-0">
      <button
        type="button"
        onClick={() => setAbierto((v) => !v)}
        className={[
          'mx-3 flex w-[calc(100%-1.5rem)] min-w-0 items-center justify-between rounded-lg px-3 py-2.5 text-left text-[11px] font-semibold uppercase tracking-wider transition-colors',
          tieneActivo
            ? 'text-white'
            : 'text-white/75 hover:bg-white/10 hover:text-white',
        ].join(' ')}
        aria-expanded={abierto}
      >
        <span className="truncate">{grupo.nombre}</span>
        <ChevronDown
          className={['h-4 w-4 shrink-0 transition-transform', abierto ? 'rotate-180' : ''].join(' ')}
          aria-hidden="true"
        />
      </button>
      {abierto && (
        <div className="mt-0.5 min-w-0 space-y-0.5">
          {grupo.items.map((item) => (
            <EnlaceMenu key={item.id} item={item} onNavigate={onNavigate} anidado />
          ))}
        </div>
      )}
    </div>
  );
}

export default function Sidebar({ abierto, onCerrar }) {
  const { logout } = useAuth();
  const { grupos } = useNavegacion();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const alNavegar = () => {
    if (onCerrar) onCerrar();
  };

  return (
    <>
      {abierto && (
        <button
          type="button"
          className="fixed inset-0 z-40 bg-black/40 lg:hidden"
          onClick={onCerrar}
          aria-label="Cerrar menú"
        />
      )}

      <aside
        className={[
          'fixed inset-y-0 left-0 z-50 flex w-[17.5rem] flex-col bg-primary transition-transform duration-200',
          'rounded-tl-[1.75rem] lg:rounded-bl-[1.75rem]',
          abierto ? 'translate-x-0' : '-translate-x-full lg:translate-x-0',
        ].join(' ')}
        aria-label="Menú principal"
      >
        <div className="flex shrink-0 items-center justify-between px-6 pb-4 pt-7">
          <div className="min-w-0">
            <span className="block text-2xl font-bold tracking-tight text-white">PracTI</span>
            <span className="mt-0.5 block text-[10px] font-medium uppercase tracking-[0.2em] text-white/60">
              Prácticas profesionales
            </span>
          </div>
          <button
            type="button"
            onClick={onCerrar}
            className="shrink-0 rounded-lg p-1 text-white/70 hover:bg-white/10 hover:text-white lg:hidden"
            aria-label="Cerrar menú lateral"
          >
            <X className="h-5 w-5" />
          </button>
        </div>

        <nav className="sidebar-nav px-0 py-3" aria-label="Navegación">
          <div className="min-w-0 space-y-0.5">
            {grupos.map((grupo) => (
              <GrupoMenu key={grupo.id} grupo={grupo} onNavigate={alNavegar} />
            ))}
          </div>
        </nav>

        <div className="shrink-0 space-y-0.5 p-3 pb-5">
          <NavLink
            to="/cambiar-password"
            onClick={alNavegar}
            className={({ isActive }) =>
              [
                'mx-3 flex w-[calc(100%-1.5rem)] min-w-0 items-center gap-2.5 rounded-lg px-3 py-2.5 text-[11px] font-semibold uppercase tracking-wider transition-colors',
                isActive
                  ? 'nav-item-active'
                  : 'text-white/75 hover:bg-white/10 hover:text-white',
              ].join(' ')
            }
          >
            <KeyRound className="h-4 w-4 shrink-0" aria-hidden="true" />
            <span className="truncate">Cambiar contraseña</span>
          </NavLink>
          <button
            type="button"
            onClick={handleLogout}
            className="mx-3 flex w-[calc(100%-1.5rem)] min-w-0 items-center gap-2.5 rounded-lg px-3 py-2.5 text-[11px] font-semibold uppercase tracking-wider text-white/75 transition-colors hover:bg-white/10 hover:text-white"
          >
            <LogOut className="h-4 w-4 shrink-0" aria-hidden="true" />
            <span className="truncate">Cerrar sesión</span>
          </button>
        </div>
      </aside>
    </>
  );
}
