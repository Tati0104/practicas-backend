import AppLogo from '@/shared/components/AppLogo';
import ThemeToggle from '@/shared/components/ThemeToggle';

export default function AuthLayout({ titulo, subtitulo, children }) {
  return (
    <div className="relative flex min-h-screen items-center justify-center ui-page px-4 py-8 sm:px-6">
      <div className="absolute right-4 top-4 sm:right-6 sm:top-6">
        <ThemeToggle variant="auth" />
      </div>

      <div className="ui-panel w-full max-w-[420px] p-6 shadow-[0_8px_40px_rgba(25,66,107,0.12)] dark:shadow-card sm:p-8">
        <div className="mb-6 text-center">
          <AppLogo variant="dark" className="mx-auto mb-4 h-16 w-16 dark:brightness-110" />
          <h1 className="text-2xl font-bold ui-text-title">{titulo}</h1>
          {subtitulo && (
            <p className="mt-1 text-sm ui-text-muted">{subtitulo}</p>
          )}
          <p className="mt-2 text-xs font-medium uppercase tracking-[0.15em] text-primary/80 dark:text-primary-glow/80">
            Prácticas empresariales
          </p>
        </div>
        {children}
      </div>
    </div>
  );
}
