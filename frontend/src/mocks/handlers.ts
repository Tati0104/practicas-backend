// src/mocks/handlers.ts

import { rest } from 'msw';

// Mock endpoint used by useVacanteDetalle (GET /vacantes/:id)
export const handlers = [
  rest.get('*/vacantes/:id', (req, res, ctx) => {
    const { id } = req.params;
    return res(
      ctx.status(200),
      ctx.json({
        id,
        empresa: 'Acme Corp',
        cargo: 'Desarrollador Frontend',
        modalidad: 'REMOTO',
        cuposTotal: 5,
        cuposDisponibles: 3,
        estado: 'ACTIVA',
      })
    );
  }),
];
