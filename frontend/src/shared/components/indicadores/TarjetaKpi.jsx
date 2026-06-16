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
      <div className="relative overflow-hidden rounded-2xl bg-gradient-to-br from-primary to-primary-accent p-5 text-white shadow-lg shadow-primary/30 transition-transform hover:-translate-y-0.5 dark:from-primary dark:to-primary-glow dark:shadow-primary/20">
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
    <div className="ui-panel p-5 transition-shadow hover:shadow-md dark:hover:shadow-card">
      <div className="flex items-start justify-between gap-3">
        <div>
          <p className="text-sm ui-text-muted">{titulo}</p>
          <p className="mt-1 text-2xl font-bold tabular-nums ui-text-title">{valor ?? '—'}</p>
          {subtitulo && <p className="mt-0.5 text-xs ui-text-muted">{subtitulo}</p>}
        </div>
        <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-primary-light text-primary dark:bg-primary/20 dark:text-primary-glow">
          <Icon className="h-5 w-5" aria-hidden="true" />
        </div>
      </div>
    </div>
  );
}
