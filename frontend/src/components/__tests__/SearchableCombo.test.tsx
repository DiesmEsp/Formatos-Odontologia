import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { SearchableCombo } from '../SearchableCombo';
import type { SearchableOption } from '../SearchableCombo';

const options: SearchableOption[] = [
  { id: 1, label: 'Maria Gonzalez' },
  { id: 2, label: 'Carlos Mendoza', badge: 'PRE' },
  { id: 3, label: 'Ana Huaman', extra: 'S/ 50.00' },
];

describe('SearchableCombo', () => {
  it('muestra el placeholder cuando no hay seleccion', () => {
    render(
      <SearchableCombo options={options} value={null} onChange={vi.fn()} placeholder="Buscar paciente..." />
    );
    expect(screen.getByText('Buscar paciente...')).toBeInTheDocument();
  });

  it('muestra el label seleccionado', () => {
    render(
      <SearchableCombo options={options} value={1} onChange={vi.fn()} />
    );
    expect(screen.getByText('Maria Gonzalez')).toBeInTheDocument();
  });

  it('abre el dropdown al hacer clic', () => {
    render(
      <SearchableCombo options={options} value={null} onChange={vi.fn()} />
    );
    fireEvent.click(document.querySelector('.search-box')!);
    expect(document.querySelector('.combo-dropdown')).toBeTruthy();
  });

  it('muestra las opciones en el dropdown', () => {
    render(
      <SearchableCombo options={options} value={null} onChange={vi.fn()} />
    );
    fireEvent.click(document.querySelector('.search-box')!);
    expect(screen.getByText('Maria Gonzalez')).toBeInTheDocument();
    expect(screen.getByText('Carlos Mendoza')).toBeInTheDocument();
    expect(screen.getByText('Ana Huaman')).toBeInTheDocument();
  });

  it('muestra IDs formateados', () => {
    render(
      <SearchableCombo options={options} value={null} onChange={vi.fn()} />
    );
    fireEvent.click(document.querySelector('.search-box')!);
    expect(screen.getByText('001')).toBeInTheDocument();
    expect(screen.getByText('002')).toBeInTheDocument();
  });

  it('muestra badges cuando la opcion los tiene', () => {
    render(
      <SearchableCombo options={options} value={null} onChange={vi.fn()} />
    );
    fireEvent.click(document.querySelector('.search-box')!);
    expect(screen.getByText('PRE')).toBeInTheDocument();
  });

  it('muestra extra cuando la opcion lo tiene', () => {
    render(
      <SearchableCombo options={options} value={null} onChange={vi.fn()} />
    );
    fireEvent.click(document.querySelector('.search-box')!);
    expect(screen.getByText('S/ 50.00')).toBeInTheDocument();
  });

  it('llama a onChange al seleccionar una opcion', () => {
    const onChange = vi.fn();
    render(
      <SearchableCombo options={options} value={null} onChange={onChange} />
    );
    fireEvent.click(document.querySelector('.search-box')!);
    fireEvent.click(screen.getByText('Maria Gonzalez'));
    expect(onChange).toHaveBeenCalledWith(1);
  });

  it('muestra "Sin resultados" cuando no hay opciones', () => {
    render(
      <SearchableCombo options={[]} value={null} onChange={vi.fn()} />
    );
    fireEvent.click(document.querySelector('.search-box')!);
    expect(screen.getByText('Sin resultados')).toBeInTheDocument();
  });

  it('muestra boton de crear cuando allowCreate es true y hay query', () => {
    const onCreateNew = vi.fn();
    render(
      <SearchableCombo options={[]} value={null} onChange={vi.fn()} allowCreate onCreateNew={onCreateNew} />
    );
    fireEvent.click(document.querySelector('.search-box')!);
    const input = document.querySelector('.search-box-input')!;
    fireEvent.change(input, { target: { value: 'Nuevo' } });
    expect(screen.getByText('Crear "Nuevo"')).toBeInTheDocument();
  });
});
