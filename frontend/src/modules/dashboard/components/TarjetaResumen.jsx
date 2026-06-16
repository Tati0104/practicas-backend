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
import { Card } from '@/shared/components/ui';
import { AnilloProgreso } from '@/shared/components/indicadores';

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

const COLORES = {
  primary: {
    border: 'border-t-primary dark:border-t-primary-glow',
    icon: 'bg-primary/10 text-primary dark:bg-primary/20 dark:text-primary-glow',
    text: 'text-primary dark:text-primary-glow',
  },
  emerald: {
    border: 'border-t-emerald-600 dark:border-t-emerald-400',
    icon: 'bg-emerald-50 text-emerald-600 dark:bg-emerald-500/15 dark:text-emerald-400',
    text: 'text-emerald-600 dark:text-emerald-400',
  },
  amber: {
    border: 'border-t-amber-500 dark:border-t-amber-400',
    icon: 'bg-amber-50 text-amber-600 dark:bg-amber-500/15 dark:text-amber-400',
    text: 'text-amber-600 dark:text-amber-400',
  },
  violet: {
    border: 'border-t-violet-600 dark:border-t-violet-400',
    icon: 'bg-violet-50 text-violet-600 dark:bg-violet-500/15 dark:text-violet-400',
    text: 'text-violet-600 dark:text-violet-400',
  },
  red: {
    border: 'border-t-red-600 dark:border-t-red-400',
    icon: 'bg-red-50 text-red-600 dark:bg-red-500/15 dark:text-red-400',
    text: 'text-red-600 dark:text-red-400',
  },
};

export default function TarjetaResumen({
  titulo,
  valor,
  total,
  icono = 'clipboard',
  color = 'primary',
}) {
  const Icon = ICONOS[icono] ?? ClipboardList;
  const palette = COLORES[color] ?? COLORES.primary;
  const numero = Number(valor) || 0;
  const referencia = total ?? numero;

  return (
    <Card
      padding="p-5"
      className={`border-t-4 ${palette.border} transition-shadow hover:shadow-md`}
    >
      <div className="flex items-center gap-4">
        <AnilloProgreso valor={numero} total={referencia} color={color} />
        <div className="min-w-0 flex-1">
          <p className="truncate text-sm ui-text-muted">{titulo}</p>
          <p className={`mt-0.5 text-2xl font-bold tabular-nums ${palette.text}`}>
            {valor ?? '—'}
          </p>
          {total > 0 && total !== numero && (
            <p className="mt-0.5 text-xs ui-text-muted">de {total} total</p>
          )}
        </div>
        <div className={`hidden h-11 w-11 shrink-0 items-center justify-center rounded-xl sm:flex ${palette.icon}`}>
          <Icon className="h-5 w-5" aria-hidden="true" />
        </div>
      </div>
    </Card>
  );
}
