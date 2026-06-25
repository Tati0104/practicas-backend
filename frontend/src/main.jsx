import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { Toaster } from 'react-hot-toast';
import AppRouter from './router/AppRouter';
import { initThemeFromStorage, aplicarModoEnDocumento } from './store/themeStore';
import useThemeStore from './store/themeStore';
import useAuthStore from './store/authStore';
import './index.css';

initThemeFromStorage();

useThemeStore.persist.onFinishHydration(() => {
  const correo = useAuthStore.getState().correo;
  const modo = useThemeStore.getState().obtenerModo(correo);
  aplicarModoEnDocumento(modo);
});

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 1,
      staleTime: 30000
    }
  }
});

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <QueryClientProvider client={queryClient}>
      <Toaster
        position="top-right"
        toastOptions={{
          duration: 3500,
          className: 'dark:!bg-dark-card dark:!text-slate-100 dark:!border dark:!border-white/[0.08]',
        }}
      />
      <AppRouter />
    </QueryClientProvider>
  </StrictMode>
);