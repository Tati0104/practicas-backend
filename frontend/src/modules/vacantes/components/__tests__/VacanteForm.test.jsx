// src/modules/vacantes/components/__tests__/VacanteForm.test.jsx

import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import VacanteForm from '../VacanteForm';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';

// Mock del hook que devuelve las mutaciones
jest.mock('../hooks/useVacantesMutaciones', () => ({
  useVacantesMutaciones: () => ({
    crear: { mutate: jest.fn() },
    editar: { mutate: jest.fn() },
  }),
}));

const queryClient = new QueryClient();

function renderForm(props = {}) {
  return render(
    <QueryClientProvider client={queryClient}>
      <VacanteForm isOpen={true} onClose={jest.fn()} {...props} />
    </QueryClientProvider>
  );
}

test('muestra errores de validación cuando se envía vacío', async () => {
  renderForm();
  fireEvent.click(screen.getByRole('button', { name: /Crear/i }));
  // Los campos obligatorios deben mostrar mensaje de error
  expect(await screen.findAllByText(/es requerida/i)).toHaveLength(2); // empresa y cargo
});

test('envía datos correctos al crear', async () => {
  renderForm();

  fireEvent.change(screen.getByLabelText(/Empresa/i), { target: { value: 'Acme Corp' } });
  fireEvent.change(screen.getByLabelText(/Cargo/i), { target: { value: 'Frontend Engineer' } });
  fireEvent.change(screen.getByLabelText(/Modalidad/i), { target: { value: 'REMOTO' } });
  fireEvent.change(screen.getByLabelText(/Cupos Totales/i), { target: { value: '5' } });
  fireEvent.change(screen.getByLabelText(/Cupos Disponibles/i), { target: { value: '5' } });
  fireEvent.change(screen.getByLabelText(/Estado/i), { target: { value: 'ACTIVA' } });

  fireEvent.click(screen.getByRole('button', { name: /Crear/i }));

  await waitFor(() => {
    const { crear } = require('../hooks/useVacantesMutaciones').useVacantesMutaciones();
    expect(crear.mutate).toHaveBeenCalledWith({
      empresa: 'Acme Corp',
      cargo: 'Frontend Engineer',
      modalidad: 'REMOTO',
      cuposTotal: 5,
      cuposDisponibles: 5,
      estado: 'ACTIVA',
    });
  });
});
