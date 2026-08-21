import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import Consumos from '../Consumos';

const mockListar = vi.fn();
const mockCrearLote = vi.fn();
const mockActualizar = vi.fn();
const mockEliminar = vi.fn();

vi.mock('../../hooks/useApi', () => ({
  useApi: vi.fn((fetcher: () => unknown) => {
    const result = fetcher();
    return { loading: false, data: result ?? [], error: null, refetch: vi.fn() };
  }),
}));

vi.mock('../../hooks/useToast', () => ({
  useToast: () => ({
    addToast: vi.fn(),
    removeToast: vi.fn(),
  }),
}));

vi.mock('../../api', () => ({
  api: {
    consumos: {
      listar: (...args: unknown[]) => mockListar(...args),
      crearLote: (...args: unknown[]) => mockCrearLote(...args),
      actualizar: (...args: unknown[]) => mockActualizar(...args),
      eliminar: (...args: unknown[]) => mockEliminar(...args),
    },
    catalogos: {
      materiales: { listar: vi.fn(() => []) },
    },
  },
}));

const CONSUMOS_MOCK = [
  { consumoID: 1, fecha: '2026-08-05', materialID: 10, nombreMaterial: 'Guantes', unidad: 'caja', cantidad: 3 },
  { consumoID: 2, fecha: '2026-08-12', materialID: 11, nombreMaterial: 'Gasas', unidad: 'paquete', cantidad: 2.5 },
];

function renderConsumos() {
  return render(<Consumos />);
}

describe('Consumos', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockListar.mockReturnValue(CONSUMOS_MOCK);
  });

  it('renderiza el titulo y el selector de periodo', () => {
    renderConsumos();
    expect(screen.getByText('Materiales Consumidos')).toBeInTheDocument();
    expect(screen.getByText('Periodo')).toBeInTheDocument();
  });

  it('muestra los registros del mes en la tabla', () => {
    renderConsumos();
    expect(screen.getByText('Guantes')).toBeInTheDocument();
    expect(screen.getByText('Gasas')).toBeInTheDocument();
    expect(screen.getByText('05/08/2026')).toBeInTheDocument();
    expect(screen.getByText('2.5')).toBeInTheDocument();
  });

  it('abre el modal de nuevo consumo al hacer click en el boton', async () => {
    renderConsumos();
    fireEvent.click(screen.getByRole('button', { name: /nuevo consumo/i }));
    await waitFor(() => {
      expect(screen.getByText('Nuevo Consumo de Materiales')).toBeInTheDocument();
      expect(screen.getByRole('button', { name: 'Agregar material' })).toBeInTheDocument();
    });
  });

  it('valida que no se pueda guardar sin materiales validos', async () => {
    renderConsumos();
    fireEvent.click(screen.getByRole('button', { name: /nuevo consumo/i }));
    const guardarBtn = screen.getAllByRole('button', { name: 'Guardar' }).find(
      (b) => (b as HTMLButtonElement).closest('.dialog-pane') !== null,
    );
    expect(guardarBtn).toBeTruthy();
    if (guardarBtn) fireEvent.click(guardarBtn);
    expect(mockCrearLote).not.toHaveBeenCalled();
  });
});
