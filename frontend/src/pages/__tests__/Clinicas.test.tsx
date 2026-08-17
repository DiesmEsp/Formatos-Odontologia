import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import Clinicas from '../Clinicas';

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
    clinicas: { listar: vi.fn(), crear: vi.fn(), actualizar: vi.fn(), eliminar: vi.fn() },
  },
}));

function renderClinicas() {
  return render(
    <BrowserRouter>
      <Clinicas />
    </BrowserRouter>
  );
}

describe('Clinicas', () => {
  it('renderiza el titulo', () => {
    renderClinicas();
    expect(screen.getByText('Gestion de Clinicas')).toBeInTheDocument();
  });

  it('renderiza el boton Nueva Clinica', () => {
    renderClinicas();
    expect(screen.getByText('Nueva Clinica')).toBeInTheDocument();
  });

  it('muestra estado vacio cuando no hay clinicas', () => {
    renderClinicas();
    expect(screen.getByText('No hay clinicas registradas')).toBeInTheDocument();
    expect(screen.getByText('Cree la primera clinica para comenzar.')).toBeInTheDocument();
  });
});