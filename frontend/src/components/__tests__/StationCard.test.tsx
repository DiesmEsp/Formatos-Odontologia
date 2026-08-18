import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { StationCard } from '../StationCard';

const tratamientoMock = {
  tratamientoID: 1, operadorID: 1, pacienteID: 1,
  unidadID: 1, fecha: '2026-08-01', nombreTratamiento: 'Exodoncia',
  monto: 30, tipo: 'NORMAL', estadoPago: 'PENDIENTE',
  montoPagado: 0, estado: 'ABIERTO', cerradoEn: null,
  montoAnterior: null,
};

describe('StationCard', () => {
  it('muestra "Libre" cuando no hay tratamiento', () => {
    render(<StationCard unidadNro={1} tratamiento={null} onClick={vi.fn()} />);
    expect(screen.getByText('Libre')).toBeInTheDocument();
  });

  it('renderiza btn-station en estado libre', () => {
    render(<StationCard unidadNro={1} tratamiento={null} onClick={vi.fn()} />);
    expect(screen.getByText('Nuevo tratamiento')).toBeInTheDocument();
    expect(document.querySelector('.btn-station')).toBeTruthy();
  });

  it('muestra "En curso" cuando hay tratamiento', () => {
    render(<StationCard unidadNro={2} tratamiento={tratamientoMock} onClick={vi.fn()} />);
    expect(screen.getByText('En curso')).toBeInTheDocument();
  });

  it('muestra el numero de tratamiento', () => {
    render(<StationCard unidadNro={2} tratamiento={tratamientoMock} onClick={vi.fn()} />);
    expect(screen.getByText('#1')).toBeInTheDocument();
  });

  it('muestra el nombre del tratamiento', () => {
    render(<StationCard unidadNro={2} tratamiento={tratamientoMock} onClick={vi.fn()} />);
    expect(screen.getByText('Exodoncia')).toBeInTheDocument();
  });

  it('muestra el monto formateado', () => {
    render(<StationCard unidadNro={2} tratamiento={tratamientoMock} onClick={vi.fn()} />);
    expect(screen.getByText('S/ 30.00')).toBeInTheDocument();
  });

  it('llama a onClick al hacer clic en la tarjeta', () => {
    const onClick = vi.fn();
    render(<StationCard unidadNro={1} tratamiento={null} onClick={onClick} />);
    fireEvent.click(document.querySelector('.station-card')!);
    expect(onClick).toHaveBeenCalledWith(1);
  });

  it('aplica clase libre cuando no hay tratamiento', () => {
    const { container } = render(
      <StationCard unidadNro={1} tratamiento={null} onClick={vi.fn()} />
    );
    expect(container.querySelector('.station-card.libre')).toBeTruthy();
  });

  it('aplica clase ocupado cuando hay tratamiento', () => {
    const { container } = render(
      <StationCard unidadNro={1} tratamiento={tratamientoMock} onClick={vi.fn()} />
    );
    expect(container.querySelector('.station-card.ocupado')).toBeTruthy();
  });

  it('muestra nombres completos cuando se proporcionan', () => {
    render(
      <StationCard unidadNro={2} tratamiento={tratamientoMock} operadorNombre="Ana Perez" pacienteNombre="Juan Lopez" onClick={vi.fn()} />
    );
    expect(screen.getByText('Ana Perez')).toBeInTheDocument();
    expect(screen.getByText('Juan Lopez')).toBeInTheDocument();
  });

  it('muestra id como respaldo cuando no hay nombre', () => {
    render(<StationCard unidadNro={2} tratamiento={tratamientoMock} onClick={vi.fn()} />);
    expect(screen.getByText('Operador #1')).toBeInTheDocument();
    expect(screen.getByText('Paciente #1')).toBeInTheDocument();
  });
});
