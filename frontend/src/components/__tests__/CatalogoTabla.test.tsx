import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { CatalogoTabla, type Column } from '../CatalogoTabla';

interface TestRow {
  id: number;
  name: string;
}

const columns: Column<TestRow>[] = [
  { key: 'id', header: 'ID', width: 60, render: (r) => <span className="num">{r.id}</span> },
  { key: 'name', header: 'Nombre', render: (r) => r.name },
];

const data: TestRow[] = [
  { id: 1, name: 'Item 1' },
  { id: 2, name: 'Item 2' },
];

describe('CatalogoTabla', () => {
  it('renderiza las columnas', () => {
    render(<CatalogoTabla columns={columns} data={data} loading={false} />);
    expect(screen.getByText('ID')).toBeInTheDocument();
    expect(screen.getByText('Nombre')).toBeInTheDocument();
  });

  it('renderiza las filas', () => {
    render(<CatalogoTabla columns={columns} data={data} loading={false} />);
    expect(screen.getByText('Item 1')).toBeInTheDocument();
    expect(screen.getByText('Item 2')).toBeInTheDocument();
  });

  it('muestra loading state', () => {
    render(<CatalogoTabla columns={columns} data={[]} loading={true} />);
    expect(screen.getByText('Cargando...')).toBeInTheDocument();
  });

  it('muestra empty state cuando no hay datos', () => {
    render(<CatalogoTabla columns={columns} data={[]} loading={false} />);
    expect(screen.getByText('Sin resultados')).toBeInTheDocument();
  });

  it('muestra el search box cuando searchEnabled es true', () => {
    render(<CatalogoTabla columns={columns} data={data} loading={false} />);
    expect(document.querySelector('.search-box')).toBeTruthy();
  });

  it('oculta el search box cuando searchEnabled es false', () => {
    render(<CatalogoTabla columns={columns} data={data} loading={false} searchEnabled={false} />);
    expect(document.querySelector('.search-box')).toBeFalsy();
  });

  it('muestra el total de registros', () => {
    render(<CatalogoTabla columns={columns} data={data} loading={false} />);
    expect(screen.getByText('2 registros')).toBeInTheDocument();
  });

  it('muestra totalLabel personalizado', () => {
    render(<CatalogoTabla columns={columns} data={data} loading={false} totalLabel="Mostrando 2 de 10" />);
    expect(screen.getByText('Mostrando 2 de 10')).toBeInTheDocument();
  });
});
