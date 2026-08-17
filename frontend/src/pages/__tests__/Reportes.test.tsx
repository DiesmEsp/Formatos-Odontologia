import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import Reportes from '../Reportes';

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
    reportes: { listarRecientes: vi.fn() },
  },
}));

function renderReportes() {
  return render(
    <BrowserRouter>
      <Reportes />
    </BrowserRouter>
  );
}

describe('Reportes', () => {
  it('renderiza el titulo', () => {
    renderReportes();
    expect(screen.getByText('Reportes')).toBeInTheDocument();
    expect(screen.getByText('Exportacion de reportes mensuales y anuales a Excel')).toBeInTheDocument();
  });

  it('renderiza las tarjetas de reportes mensuales', () => {
    renderReportes();
    expect(screen.getByText('Materiales Generales')).toBeInTheDocument();
    expect(screen.getByText('Ingresos Financieros')).toBeInTheDocument();
    expect(screen.getByText('Consumo Docente')).toBeInTheDocument();
    expect(screen.getByText('Consumo Especialista')).toBeInTheDocument();
    expect(screen.getByText('Consumo por Tratamiento')).toBeInTheDocument();
    expect(screen.getByText('Asistencia Docente')).toBeInTheDocument();
  });

  it('muestra estado vacio de reportes recientes', () => {
    renderReportes();
    expect(screen.getByText('No hay reportes generados aun.')).toBeInTheDocument();
  });
});
