import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { SelectClinicScreen } from '../SelectClinicScreen';
import { ClinicaProvider } from '../../contexts/ClinicaContext';
import { ToastProvider } from '../../hooks/useToast';

vi.mock('../../api', () => ({
  api: {
    clinicas: {
      listar: vi.fn(),
      crear: vi.fn(),
    },
  },
}));

const apiMock = (await import('../../api')).api;

describe('SelectClinicScreen', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();
  });

  function renderizar() {
    return render(
      <ToastProvider>
        <ClinicaProvider>
          <SelectClinicScreen />
        </ClinicaProvider>
      </ToastProvider>
    );
  }

  it('lista las clinicas disponibles', async () => {
    (apiMock.clinicas.listar as ReturnType<typeof vi.fn>).mockResolvedValue([
      { clinicaID: 1, nombre: 'Clinica 5 ODP', grupo: 'Pos Grado', estado: 1 },
      { clinicaID: 2, nombre: 'Clinica 6 Endo', grupo: 'Pos Grado', estado: 1 },
    ]);

    renderizar();

    await waitFor(() => {
      expect(screen.getByText('Clinica 5 ODP')).toBeInTheDocument();
      expect(screen.getByText('Clinica 6 Endo')).toBeInTheDocument();
    });
  });

  it('al seleccionar una clinica guarda la sesion', async () => {
    (apiMock.clinicas.listar as ReturnType<typeof vi.fn>).mockResolvedValue([
      { clinicaID: 2, nombre: 'Clinica 6 Endo', grupo: 'Pos Grado', estado: 1 },
    ]);

    renderizar();

    const boton = await screen.findByText('Usar');
    fireEvent.click(boton);

    await waitFor(() => {
      expect(localStorage.getItem('formatos.clinica-activa')).toContain('Clinica 6 Endo');
    });
  });

  it('permite crear la primera clinica cuando no existe ninguna', async () => {
    (apiMock.clinicas.listar as ReturnType<typeof vi.fn>).mockResolvedValue([]);
    (apiMock.clinicas.crear as ReturnType<typeof vi.fn>).mockResolvedValue({ id: 1 });

    renderizar();

    await waitFor(() => {
      expect(screen.getByText(/Aun no existe ninguna clinica/)).toBeInTheDocument();
    });

    fireEvent.change(screen.getByPlaceholderText('Nombre de la clinica'), { target: { value: 'Clinica Nueva' } });
    fireEvent.click(screen.getByText('Crear'));

    await waitFor(() => {
      expect(apiMock.clinicas.crear).toHaveBeenCalledWith({ nombre: 'Clinica Nueva', grupo: null });
    });
    expect(localStorage.getItem('formatos.clinica-activa')).toContain('Clinica Nueva');
  });
});