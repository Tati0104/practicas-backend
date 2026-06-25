// src/modules/vacantes/pages/__tests__/VacanteDetallePage.test.jsx
import { render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import VacanteDetallePage from '../VacanteDetallePage';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';

const queryClient = new QueryClient();

function renderWithRouter(id = '1') {
  render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter initialEntries={[`/vacantes/${id}`]}>
        <Routes>
          <Route path="/vacantes/:id" element={<VacanteDetallePage />} />
        </Routes>
      </MemoryRouter>
    </QueryClientProvider>
  );
}

test('muestra spinner mientras carga', async () => {
  renderWithRouter();
  expect(screen.getByText(/cargando detalle de la vacante/i)).toBeInTheDocument();
});

test('muestra datos de la vacante una vez cargados', async () => {
  renderWithRouter();
  await waitFor(() => {
    expect(screen.getByText(/Acme Corp/i)).toBeInTheDocument();
    expect(screen.getByText(/Desarrollador Frontend/i)).toBeInTheDocument();
    expect(screen.getByText(/REMOTO/i)).toBeInTheDocument();
  });
  // Verifica que el componente HistorialEstados esté presente
  expect(
    screen.getByRole('heading', { name: /historial de estados/i })
  ).toBeInTheDocument();
});
