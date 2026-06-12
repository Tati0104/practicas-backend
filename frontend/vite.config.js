import { defineConfig } from 'vitest/config'
import react from '@vitejs/plugin-react'
import path from 'path'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],

  // ──────────────────────────────────────────────────────────────────────
  // Configuración de Vitest
  // globals:true → no hay que importar describe/it/expect en cada archivo
  // environment:'jsdom' → simula el DOM del navegador
  // setupFiles → se ejecuta antes de cada suite de tests
  // ──────────────────────────────────────────────────────────────────────
  test: {
    globals: true,
    environment: 'jsdom',
    setupFiles: './src/setupTests.ts',
    coverage: {
      provider: 'v8',
      reporter: ['text', 'html'],
    },
  },
})

