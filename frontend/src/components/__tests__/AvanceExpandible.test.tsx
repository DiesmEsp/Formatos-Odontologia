import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { AvanceExpandible } from '../AvanceExpandible';
import type { AvanceDetalle } from '../../api/types';

vi.mock('../../api', () => ({
  api: {
    tratamientos: {
      avanceDetalle: vi.fn(),
    },
  },
}));

import { api } from '../../api';

const avanceBase = {
  avanceID: 7,
  tratamientoID: 1,
  numero: 2,
  fecha: '2026-08-04',
  unidadID: null,
  estado: 'ACTIVO',
  timestamp: '2026-08-04 10:00:00',
};

const detalle: AvanceDetalle = {
  avance: avanceBase,
  materiales: [
    { materialID: 3, nombreMaterial: 'Resina', unidad: 'unidad', cantidad: 2 },
  ],
  pagos: [
    { pagoID: 9, tratamientoID: 1, avanceID: 7, fecha: '2026-08-04', monto: 40, timestamp: '2026-08-04 10:00:00' },
  ],
};

function renderAvance(estado = 'ACTIVO') {
  return render(
    <ul>
      <AvanceExpandible avance={{ ...avanceBase, estado }} />
    </ul>
  );
}

describe('AvanceExpandible', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(api.tratamientos.avanceDetalle).mockResolvedValue(detalle);
  });

  it('muestra el resumen colapsado sin llamar al API', () => {
    renderAvance();
    expect(screen.getByText('Avance #2 - 2026-08-04')).toBeInTheDocument();
    expect(screen.getByText('ACTIVO')).toBeInTheDocument();
    expect(api.tratamientos.avanceDetalle).not.toHaveBeenCalled();
    expect(screen.queryByText('Resina')).not.toBeInTheDocument();
  });

  it('al expandir carga y muestra materiales y pagos', async () => {
    renderAvance();
    fireEvent.click(screen.getByTitle('Ver detalle'));

    expect(api.tratamientos.avanceDetalle).toHaveBeenCalledWith(7);
    expect(await screen.findByText('Resina')).toBeInTheDocument();
    expect(screen.getByText('2 unidad')).toBeInTheDocument();
    expect(screen.getByText('S/ 40.00')).toBeInTheDocument();
  });

  it('muestra vacios cuando el avance anulado no tiene pagos', async () => {
    vi.mocked(api.tratamientos.avanceDetalle).mockResolvedValue({
      ...detalle,
      pagos: [],
    });
    renderAvance('ANULADO');
    expect(screen.getByText('ANULADO')).toBeInTheDocument();

    fireEvent.click(screen.getByTitle('Ver detalle'));
    expect(await screen.findByText('Sin pagos vinculados')).toBeInTheDocument();
    expect(screen.getByText('Resina')).toBeInTheDocument();
  });
});
