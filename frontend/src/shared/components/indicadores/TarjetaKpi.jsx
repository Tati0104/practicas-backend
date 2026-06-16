import {
  Award,
  Building2,
  ClipboardList,
  Clock,
  GraduationCap,
  Link2,
  Star,
  Users,
} from 'lucide-react';

const ICONOS = {
  users: Users,
  graduation: GraduationCap,
  building: Building2,
  clipboard: ClipboardList,
  clock: Clock,
  link: Link2,
  star: Star,
  award: Award,
};

export default function TarjetaKpi({
  titulo,
  valor,
  subtitulo,
  icono = 'clipboard',
  destacada = false,
}) {
  const Icon = ICONOS[icono] ?? ClipboardList;

  if (destacada) {
    return (
      <div className="relative overflow-hidden rounded-2xl bg-primary p-5 text-white shadow-lg shadow-primary/20 transition-transform hover:-translate-y-0.5">
        <div className="absolute -right-4 -top-4 h-24 w-24 rounded-full bg-white/10" />
        <div className="relative flex items-start justify-between gap-3">
          <div>
            <p className="text-sm font-medium text-white/80">{titulo}</p>
            <p className="mt-1 text-3xl font-bold tabular-nums">{valor ?? '—'}</p>
            {subtitulo && <p className="mt-1 text-xs text-white/70">{subtitulo}</p>}
          </div>
          <div className="flex h-11 w-11 items-center justify-center rounded-xl bg-white/15">
            <Icon className="h-5 w-5" aria-hidden="true" />
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="rounded-2xl border border-gray-100 bg-white p-5 shadow-sm transition-shadow hover:shadow-md">
      <div className="flex items-start justify-between gap-3">
        <div>
          <p className="text-sm text-gray-500">{titulo}</p>
          <p className="mt-1 text-2xl font-bold tabular-nums text-gray-900">{valor ?? '—'}</p>
          {subtitulo && <p className="mt-0.5 text-xs text-gray-400">{subtitulo}</p>}
        </div>
        <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-primary-light text-primary">
          <Icon className="h-5 w-5" aria-hidden="true" />
        </div>
      </div>
    </div>
  );
}
