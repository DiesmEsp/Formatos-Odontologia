import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { EditarTratamientoModal } from '../EditarTratamientoModal';
import type { Tratamiento } from '../../../api/types';

vi.mock('../../../hooks/useApi', () => ({
  useApi: vi.fn(() => ({
    loading: false,
    data: [],
    error: null,
    refetch: vi.fn(),
  })),
}));

const mockEditarEnCurso = vi.fn();
const mockEditarRetroactivo = vi.fn();

vi.mock('../../../api', () => ({
  api: {
    catalogos: {
      operadores: { listar: vi.fn() },
      pacientes: { listar: vi.fn() },
    },
    tratamientos: {
      editarEnCurso: (...args: unknown[]) => mockEditarEnCurso(...args),
      editarRetroactivo: (...args: unknown[]) => mockEditarRetroactivo(...args),
    },
  },
}));

const baseTratamiento: Tratamiento = {
  tratamientoID: 7,
  operadorID: 1,
  pacienteID: 2,
  unidadID: null,
  fecha: '2026-08-03',
  nombreTratamiento: 'Limpieza dental',
  monto: 100,
  tipo: 'NORMAL',
  estadoPago: 'PENDIENTE',
  montoPagado: 0,
  estado: 'ABIERTO',
  cerradoEn: null,
  montoAnterior: null,
  tratamientoPadreID: null,
};

function renderModal(tratamiento: Tratamiento = baseTratamiento) {
  const onClose = vi.fn();
  const onSuccess = vi.fn();
  const addToast = vi.fn();
  const utils = render(
    <EditarTratamientoModal
      tratamiento={tratamiento}
      onClose={onClose}
      onSuccess={onSuccess}
      addToast={addToast}
    />
  );
  return { ...utils, onClose, onSuccess, addToast };
}

describe('EditarTratamientoModal', () => {
  it('renderiza el nombre del tratamiento en el input', () => {
    renderModal();
    const input = screen.getByDisplayValue('Limpieza dental');
    expect(input).toBeInTheDocument();
  });

  it('renderiza el monto en el input', () => {
    renderModal();
    const monto = screen.getByDisplayValue('100');
    expect(monto).toBeInTheDocument();
  });

  it('renderiza la fecha en el input', () => {
    renderModal();
    const fecha = screen.getByDisplayValue('2026-08-03');
    expect(fecha).toBeInTheDocument();
  });

  it('llama a onClose al cancelar', () => {
    const { onClose } = renderModal();
    fireEvent.click(screen.getByText('Cancelar'));
    expect(onClose).toHaveBeenCalled();
  });

  it('rechaza nombre vacío', async () => {
    const { onSuccess } = renderModal();
    const nombreInput = screen.getByDisplayValue('Limpieza dental');
    fireEvent.change(nombreInput, { target: { value: '   ' } });
    fireEvent.click(screen.getByText('Guardar cambios'));
    await waitFor(() => {
      expect(screen.getByText(/no puede estar vac/i)).toBeInTheDocument();
    });
    expect(onSuccess).not.toHaveBeenCalled();
  });

  it('llama a editarEnCurso para tratamiento ABIERTO', async () => {
    mockEditarEnCurso.mockResolvedValue({ ok: true });
    const { onSuccess } = renderModal();
    fireEvent.click(screen.getByText('Guardar cambios'));
    await waitFor(() => {
      expect(mockEditarEnCurso).toHaveBeenCalledWith(7, expect.objectContaining({
        nombreTratamiento: 'Limpieza dental',
        fecha: '2026-08-03',
        monto: 100,
      }));
    });
    expect(onSuccess).toHaveBeenCalled();
  });

  it('llama a editarRetroactivo para tratamiento CERRADO', async () => {
    mockEditarRetroactivo.mockResolvedValue({ ok: true });
    const cerrado: Tratamiento = { ...baseTratamiento, estado: 'CERRADO', montoPagado: 100, estadoPago: 'PAGADO' };
    renderModal(cerrado);
    fireEvent.click(screen.getByText('Guardar cambios'));
    await waitFor(() => {
      expect(mockEditarRetroactivo).toHaveBeenCalledWith(7, expect.objectContaining({
        nombreTratamiento: 'Limpieza dental',
        fecha: '2026-08-03',
      }));
    });
  });

  it('deshabilita el monto cuando el tipo es CONTINUO', () => {
    const continuo: Tratamiento = { ...baseTratamiento, tipo: 'CONTINUO', monto: 0 };
    renderModal(continuo);
    const monto = screen.getByPlaceholderText('0.00') as HTMLInputElement;
    expect(monto.disabled).toBe(true);
  });

  it('muestra el error del backend', async () => {
    mockEditarEnCurso.mockRejectedValue(new Error('Monto menor que lo pagado'));
    renderModal();
    fireEvent.click(screen.getByText('Guardar cambios'));
    await waitFor(() => {
      expect(screen.getByText('Monto menor que lo pagado')).toBeInTheDocument();
    });
  });
});
