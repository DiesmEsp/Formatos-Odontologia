import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import Catalogos from '../Catalogos';

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
    catalogos: {
      materiales: { listar: vi.fn(), crear: vi.fn(), actualizar: vi.fn(), eliminar: vi.fn() },
      docentes: { listar: vi.fn(), crear: vi.fn(), actualizar: vi.fn(), eliminar: vi.fn() },
      pacientes: { listar: vi.fn(), crear: vi.fn(), actualizar: vi.fn(), eliminar: vi.fn() },
      operadores: { listar: vi.fn(), crear: vi.fn(), actualizar: vi.fn(), eliminar: vi.fn() },
      tratamientosPred: { listar: vi.fn(), crear: vi.fn(), actualizar: vi.fn(), eliminar: vi.fn(), materiales: vi.fn(), guardarMateriales: vi.fn() },
      conversiones: { listar: vi.fn() },
    },
    tratamientos: { buscar: vi.fn(), listar: vi.fn() },
  },
}));

function renderCatalogos() {
  return render(
    <BrowserRouter>
      <Catalogos />
    </BrowserRouter>
  );
}

describe('Catalogos', () => {
  it('renderiza el titulo', () => {
    renderCatalogos();
    expect(screen.getByText('Catalogos')).toBeInTheDocument();
    expect(screen.getByText('Gestion centralizada de catalogos del sistema')).toBeInTheDocument();
  });

  it('renderiza las 6 tabs', () => {
    renderCatalogos();
    expect(screen.getByText('Materiales')).toBeInTheDocument();
    expect(screen.getByText('Docentes')).toBeInTheDocument();
    expect(screen.getByText('Pacientes')).toBeInTheDocument();
    expect(screen.getByText('Operadores')).toBeInTheDocument();
    expect(screen.getByText('Tratamientos Predef.')).toBeInTheDocument();
    expect(screen.getByText('Trat. Realizados')).toBeInTheDocument();
  });

  it('la tab Materiales esta activa por defecto', () => {
    renderCatalogos();
    const materialesTab = screen.getByText('Materiales');
    expect(materialesTab.className).toContain('active');
  });
});
