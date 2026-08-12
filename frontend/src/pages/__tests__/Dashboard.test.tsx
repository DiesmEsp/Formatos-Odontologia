import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import Dashboard from '../Dashboard';

vi.mock('../../hooks/useApi', () => ({
  useApi: vi.fn(() => ({
    loading: false,
    data: null,
    error: null,
    refetch: vi.fn(),
  })),
}));

vi.mock('../../api', () => ({
  api: {
    dashboard: {
      kpis: vi.fn(),
      ingresosMensuales: vi.fn(),
      tratamientosEstado: vi.fn(),
      topMateriales: vi.fn(),
      asistenciaHoy: vi.fn(),
    },
  },
}));

function renderDashboard() {
  return render(
    <BrowserRouter>
      <Dashboard />
    </BrowserRouter>
  );
}

describe('Dashboard', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renderiza el saludo de bienvenida', () => {
    renderDashboard();
    expect(screen.getByText(/Buenos días|Buenas tardes|Buenas noches/)).toBeInTheDocument();
  });

  it('renderiza los 4 KPI cards con labels correctos', () => {
    renderDashboard();
    expect(screen.getByText('Ingresos del Mes')).toBeInTheDocument();
    expect(screen.getByText('Ingresos Semana')).toBeInTheDocument();
    expect(screen.getByText('Tratamientos en Curso')).toBeInTheDocument();
    expect(screen.getByText('Docentes Hoy')).toBeInTheDocument();
  });

  it('renderiza los accesos rapidos', () => {
    renderDashboard();
    expect(screen.getByText('Acceso rapido')).toBeInTheDocument();
    expect(screen.getByText('Asistencia Docente')).toBeInTheDocument();
    expect(screen.getByText('Catalogos')).toBeInTheDocument();
    expect(screen.getByText('Reportes')).toBeInTheDocument();
  });

  it('renderiza las secciones de graficos', () => {
    renderDashboard();
    expect(screen.getByText('Ingresos mensuales')).toBeInTheDocument();
    expect(screen.getByText('Tratamientos por estado')).toBeInTheDocument();
    expect(screen.getByText('Materiales mas utilizados')).toBeInTheDocument();
  });
});
