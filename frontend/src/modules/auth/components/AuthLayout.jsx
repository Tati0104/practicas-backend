import AppLogo from '@/shared/components/AppLogo';

export default function AuthLayout({ titulo, subtitulo, children }) {
  return (
    <div className="flex min-h-screen items-center justify-center bg-white px-4 py-8 sm:px-6">
      <div className="w-full max-w-[420px] rounded-2xl bg-white p-6 shadow-[0_8px_40px_rgba(25,66,107,0.12)] ring-1 ring-gray-100 sm:p-8">
        <div className="mb-6 text-center">
          <AppLogo variant="dark" className="mx-auto mb-4 h-16 w-16" />
          <h1 className="text-2xl font-bold text-gray-900">{titulo}</h1>
          {subtitulo && (
            <p className="mt-1 text-sm text-gray-500">{subtitulo}</p>
          )}
          <p className="mt-2 text-xs font-medium uppercase tracking-[0.15em] text-primary/80">
            Prácticas empresariales
          </p>
        </div>
        {children}
      </div>
    </div>
  );
}
