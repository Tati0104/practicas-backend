import { GraduationCap } from 'lucide-react';

export default function AuthLayout({ titulo, subtitulo, children }) {
  return (
    <div className="flex min-h-screen items-center justify-center bg-gradient-to-br from-blue-900 via-blue-800 to-blue-700 px-4 py-8 sm:px-6">
      <div className="w-full max-w-[420px] rounded-2xl bg-white p-6 shadow-xl sm:p-8">
        <div className="mb-6 text-center">
          <div
            className="mx-auto mb-4 flex h-14 w-14 items-center justify-center rounded-full bg-blue-50 text-blue-700"
            aria-hidden="true"
          >
            <GraduationCap className="h-7 w-7" />
          </div>
          <h1 className="text-2xl font-bold text-gray-900">{titulo}</h1>
          {subtitulo && (
            <p className="mt-1 text-sm text-gray-500">{subtitulo}</p>
          )}
        </div>
        {children}
      </div>
    </div>
  );
}
