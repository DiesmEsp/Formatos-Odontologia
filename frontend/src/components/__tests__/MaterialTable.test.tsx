import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { MaterialTable, type MaterialRow } from '../MaterialTable';
import type { Materiales } from '../../api/types';

const materialesMock: Materiales[] = [
  { materialID: 1, nombre: 'Guantes', unidad: 'Caja', estado: 1 },
  { materialID: 2, nombre: 'Gasas', unidad: 'Paquete', estado: 1 },
];

const rowsMock: MaterialRow[] = [
  { key: '1', materialId: 1, nombreMaterial: 'Guantes', cantidad: 5 },
  { key: '2', materialId: 2, nombreMaterial: 'Gasas', cantidad: 3 },
];

describe('MaterialTable', () => {
  it('renderiza las filas de materiales', () => {
    render(
      <MaterialTable
        rows={rowsMock} materials={materialesMock}
        onAdd={vi.fn()} onRemove={vi.fn()} onMaterialChange={vi.fn()} onCantidadChange={vi.fn()}
      />
    );
    const inputs = document.querySelectorAll('.material-row-input');
    expect(inputs).toHaveLength(2);
  });

  it('llama a onAdd al hacer clic en agregar', () => {
    const onAdd = vi.fn();
    render(
      <MaterialTable
        rows={rowsMock} materials={materialesMock}
        onAdd={onAdd} onRemove={vi.fn()} onMaterialChange={vi.fn()} onCantidadChange={vi.fn()}
      />
    );
    fireEvent.click(screen.getByText('Agregar material'));
    expect(onAdd).toHaveBeenCalled();
  });

  it('llama a onRemove al hacer clic en el boton de eliminar', () => {
    const onRemove = vi.fn();
    render(
      <MaterialTable
        rows={rowsMock} materials={materialesMock}
        onAdd={vi.fn()} onRemove={onRemove} onMaterialChange={vi.fn()} onCantidadChange={vi.fn()}
      />
    );
    const removeBtns = document.querySelectorAll('.material-row-remove');
    fireEvent.click(removeBtns[0]);
    expect(onRemove).toHaveBeenCalled();
  });

  it('en modo readOnly no muestra botones de accion', () => {
    const { container } = render(
      <MaterialTable
        rows={rowsMock} materials={materialesMock}
        onAdd={vi.fn()} onRemove={vi.fn()} onMaterialChange={vi.fn()} onCantidadChange={vi.fn()}
        readOnly
      />
    );
    expect(container.querySelector('.material-table-add')).toBeFalsy();
    expect(container.querySelector('.material-row-remove')).toBeFalsy();
  });

  it('en modo readOnly muestra nombres en vez de combos', () => {
    render(
      <MaterialTable
        rows={rowsMock} materials={materialesMock}
        onAdd={vi.fn()} onRemove={vi.fn()} onMaterialChange={vi.fn()} onCantidadChange={vi.fn()}
        readOnly
      />
    );
    expect(screen.getByText('Guantes')).toBeInTheDocument();
    expect(screen.getByText('Gasas')).toBeInTheDocument();
  });

  it('muestra empty state cuando no hay filas y es readOnly', () => {
    render(
      <MaterialTable
        rows={[]} materials={materialesMock}
        onAdd={vi.fn()} onRemove={vi.fn()} onMaterialChange={vi.fn()} onCantidadChange={vi.fn()}
        readOnly
      />
    );
    expect(screen.getByText('Sin materiales registrados')).toBeInTheDocument();
  });

  it('enfoca el input de cantidad al seleccionar material', () => {
    const rows: MaterialRow[] = [{ key: '1', materialId: null, nombreMaterial: '', cantidad: 0 }];
    const { rerender } = render(
      <MaterialTable
        rows={rows} materials={materialesMock}
        onAdd={vi.fn()} onRemove={vi.fn()} onMaterialChange={vi.fn()} onCantidadChange={vi.fn()}
      />
    );
    rerender(
      <MaterialTable
        rows={[{ key: '1', materialId: 1, nombreMaterial: 'Guantes', cantidad: 0 }]} materials={materialesMock}
        onAdd={vi.fn()} onRemove={vi.fn()} onMaterialChange={vi.fn()} onCantidadChange={vi.fn()}
      />
    );
    const inputs = document.querySelectorAll('.material-row-input');
    expect(inputs[0]).toBe(document.activeElement);
  });

  it('no enfoca cantidad al montar con materiales ya asignados', () => {
    render(
      <MaterialTable
        rows={rowsMock} materials={materialesMock}
        onAdd={vi.fn()} onRemove={vi.fn()} onMaterialChange={vi.fn()} onCantidadChange={vi.fn()}
      />
    );
    const inputs = document.querySelectorAll('.material-row-input');
    expect(inputs[0]).not.toBe(document.activeElement);
  });
});
