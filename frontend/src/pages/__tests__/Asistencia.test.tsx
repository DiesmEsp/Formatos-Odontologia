import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import Asistencia from '../Asistencia';

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
    asistencia: {
      porFecha: vi.fn(),
      materialesDefault: { listar: vi.fn(), guardar: vi.fn() },
    },
    catalogos: {
      docentes: { listar: vi.fn() },
      materiales: { listar: vi.fn() },
    },
  },
}));

function renderAsistencia() {
  return render(
    <BrowserRouter>
      <Asistencia />
    </BrowserRouter>
  );
}

describe('Asistencia', () => {
  it('renderiza el titulo y subtitulo', () => {
    renderAsistencia();
    expect(screen.getByText('Asistencia Docente')).toBeInTheDocument();
    expect(screen.getByText('Registro de entrada, salida, ausencias y materiales')).toBeInTheDocument();
  });

  it('renderiza la tabla de docentes y la busqueda manual', () => {
    renderAsistencia();
    expect(screen.getByText('Docentes registrados')).toBeInTheDocument();
    expect(screen.getByText('Búsqueda manual')).toBeInTheDocument();
  });
});
