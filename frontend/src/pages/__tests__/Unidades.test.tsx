import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import Unidades from '../Unidades';

vi.mock('../../hooks/useApi', () => ({
  useApi: vi.fn(() => ({
    loading: false,
    data: [],
    error: null,
    refetch: vi.fn(),
  })),
}));

vi.mock('../../hooks/useToast', () => ({
  useToast: () => ({
    addToast: vi.fn(),
    removeToast: vi.fn(),
  }),
}));

vi.mock('../../api', () => ({
  api: {
    unidades: { listar: vi.fn(), crear: vi.fn(), eliminar: vi.fn() },
    tratamientos: { activos: vi.fn() },
  },
}));

function renderUnidades() {
  return render(
    <BrowserRouter>
      <Unidades />
    </BrowserRouter>
  );
}

describe('Unidades', () => {
  it('renderiza el titulo', () => {
    renderUnidades();
    expect(screen.getByText('Gestion de Unidades')).toBeInTheDocument();
  });

  it('renderiza el boton Nueva Unidad', () => {
    renderUnidades();
    expect(screen.getByText('Nueva Unidad')).toBeInTheDocument();
  });

  it('muestra estado vacio cuando no hay unidades', () => {
    renderUnidades();
    expect(screen.getByText('No hay unidades registradas')).toBeInTheDocument();
    expect(screen.getByText('Cree la primera unidad para comenzar.')).toBeInTheDocument();
  });
});
