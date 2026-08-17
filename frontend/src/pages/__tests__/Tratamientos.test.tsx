import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import Tratamientos from '../Tratamientos';

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
    unidades: { listar: vi.fn() },
    tratamientos: { activos: vi.fn() },
    catalogos: {
      operadores: { listar: vi.fn() },
      pacientes: { listar: vi.fn() },
    },
  },
}));

function renderTratamientos() {
  return render(
    <BrowserRouter>
      <Tratamientos />
    </BrowserRouter>
  );
}

describe('Tratamientos', () => {
  it('renderiza el titulo y subtitulo', () => {
    renderTratamientos();
    expect(screen.getByText('Tratamientos en Curso')).toBeInTheDocument();
    expect(screen.getByText('Seleccione una unidad libre para iniciar un nuevo tratamiento')).toBeInTheDocument();
  });

  it('muestra estado vacio cuando no hay unidades', () => {
    renderTratamientos();
    expect(screen.getByText('No hay unidades registradas')).toBeInTheDocument();
    expect(screen.getByText('Cree unidades en la sección de Gestión para comenzar.')).toBeInTheDocument();
  });
});
