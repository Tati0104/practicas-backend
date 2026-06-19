import { Moon, Sun } from 'lucide-react';
import useTheme from '@/shared/hooks/useTheme';

/**
 * @param {'navbar'|'auth'} [variant]
 */
export default function ThemeToggle({ variant = 'navbar', className = '' }) {
  const { esOscuro, alternar } = useTheme();

  const clases =
    variant === 'auth'
      ? 'rounded-lg border ui-border bg-white p-2 text-gray-600 transition hover:bg-gray-50 dark:bg-dark-elevated dark:text-slate-300 dark:hover:bg-dark-card'
      : 'rounded-lg p-2 text-gray-500 transition hover:bg-gray-100 hover:text-primary dark:text-slate-400 dark:hover:bg-white/[0.06] dark:hover:text-primary-glow';

  return (
    <button
      type="button"
      onClick={alternar}
      className={[clases, className].filter(Boolean).join(' ')}
      title={esOscuro ? 'Cambiar a modo claro' : 'Cambiar a modo oscuro'}
      aria-label={esOscuro ? 'Activar modo claro' : 'Activar modo oscuro'}
    >
      {esOscuro ? <Sun className="h-5 w-5" aria-hidden="true" /> : <Moon className="h-5 w-5" aria-hidden="true" />}
    </button>
  );
}
