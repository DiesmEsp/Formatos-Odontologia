import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { CrearTratamientoModal } from '../CrearTratamientoModal';

vi.mock('../../../hooks/useApi', () => ({
  useApi: vi.fn(() => ({
    loading: false,
    data: [],
    error: null,
    refetch: vi.fn(),
  })),
}));

const mockCrear = vi.fn();

vi.mock('../../../api', () => ({
  api: {
    catalogos: {
      operadores: { listar: vi.fn() },
      pacientes: { listar: vi.fn() },
      tratamientosPred: { listar: vi.fn(), materiales: vi.fn() },
      materiales: { listar: vi.fn() },
    },
    tratamientos: {
      crear: (...args: unknown[]) => mockCrear(...args),
      todos: vi.fn(),
    },
  },
}));

function renderModal() {
  const onClose = vi.fn();
  const onSuccess = vi.fn();
  const addToast = vi.fn();
  const utils = render(
    <CrearTratamientoModal
      unidad={null}
      unidadesList={[]}
      onClose={onClose}
      onSuccess={onSuccess}
      addToast={addToast}
    />
  );
  return { ...utils, onClose, onSuccess, addToast };
}

function renderModalConUnidades(unidadesOcupadas?: number[]) {
  const onClose = vi.fn();
  const onSuccess = vi.fn();
  const addToast = vi.fn();
  const utils = render(
    <CrearTratamientoModal
      unidad={null}
      unidadesList={[{ unidadID: 1, unidadNro: 1 }, { unidadID: 2, unidadNro: 2 }]}
      unidadesOcupadas={unidadesOcupadas}
      onClose={onClose}
      onSuccess={onSuccess}
      addToast={addToast}
    />
  );
  return { ...utils, onClose, onSuccess, addToast };
}

describe('CrearTratamientoModal', () => {
  it('renderiza el titulo por defecto', () => {
    renderModal();
    expect(screen.getByRole('heading', { name: 'Crear Tratamiento' })).toBeInTheDocument();
  });

  it('muestra el placeholder actualizado cuando hay nombre libre', () => {
    renderModal();
    const combo = document.querySelector('.search-box');
    expect(combo).toBeTruthy();
  });

  it('muestra mensaje de error si paciente/operador faltantes al guardar', async () => {
    mockCrear.mockResolvedValue({ id: 1 });
    const { addToast } = renderModal();
    fireEvent.click(screen.getByRole('button', { name: 'Crear Tratamiento' }));
    await waitFor(() => {
      expect(addToast).toHaveBeenCalledWith('error', 'Seleccione paciente y operador');
    });
    expect(mockCrear).not.toHaveBeenCalled();
  });

  it('filtra las unidades ocupadas del selector', () => {
    renderModalConUnidades([1]);
    const opciones = screen.getAllByRole('option').map((o) => o.textContent);
    expect(opciones).toContain('Sin unidad');
    expect(opciones).toContain('Unidad 2');
    expect(opciones).not.toContain('Unidad 1');
  });

  it('muestra todas las unidades cuando ninguna esta ocupada', () => {
    renderModalConUnidades();
    const opciones = screen.getAllByRole('option').map((o) => o.textContent);
    expect(opciones).toContain('Unidad 1');
    expect(opciones).toContain('Unidad 2');
  });
});
