import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { CatalogoModal } from '../CatalogoModal';

const fields = [
  { key: 'nombre', label: 'Nombre', type: 'text' as const },
  { key: 'unidad', label: 'Unidad', type: 'text' as const },
];

describe('CatalogoModal', () => {
  it('no renderiza cuando open es false', () => {
    const { container } = render(
      <CatalogoModal open={false} title="Nuevo" fields={fields} initialValues={{}} onSave={vi.fn()} onCancel={vi.fn()} />
    );
    expect(container.querySelector('.dialog-overlay')).toBeFalsy();
  });

  it('renderiza cuando open es true', () => {
    render(
      <CatalogoModal open={true} title="Nuevo Material" fields={fields} initialValues={{}} onSave={vi.fn()} onCancel={vi.fn()} />
    );
    expect(screen.getByText('Nuevo Material')).toBeInTheDocument();
  });

  it('renderiza los campos', () => {
    render(
      <CatalogoModal open={true} title="Nuevo" fields={fields} initialValues={{ nombre: '', unidad: '' }} onSave={vi.fn()} onCancel={vi.fn()} />
    );
    expect(screen.getByText('Nombre')).toBeInTheDocument();
    expect(screen.getByText('Unidad')).toBeInTheDocument();
  });

  it('llama a onSave con los valores', () => {
    const onSave = vi.fn();
    render(
      <CatalogoModal open={true} title="Nuevo" fields={fields} initialValues={{ nombre: 'Test', unidad: 'Caja' }} onSave={onSave} onCancel={vi.fn()} />
    );
    fireEvent.click(screen.getByText('Guardar'));
    expect(onSave).toHaveBeenCalledWith({ nombre: 'Test', unidad: 'Caja' });
  });

  it('llama a onCancel al hacer clic en Cancelar', () => {
    const onCancel = vi.fn();
    render(
      <CatalogoModal open={true} title="Nuevo" fields={fields} initialValues={{}} onSave={vi.fn()} onCancel={onCancel} />
    );
    fireEvent.click(screen.getByText('Cancelar'));
    expect(onCancel).toHaveBeenCalled();
  });

  it('muestra children si se pasan', () => {
    render(
      <CatalogoModal open={true} title="Nuevo" fields={fields} initialValues={{}} onSave={vi.fn()} onCancel={vi.fn()}>
        <div data-testid="child">Extra contenido</div>
      </CatalogoModal>
    );
    expect(screen.getByTestId('child')).toBeInTheDocument();
  });

  it('muestra estado saving en el boton', () => {
    render(
      <CatalogoModal open={true} title="Nuevo" fields={fields} initialValues={{}} onSave={vi.fn()} onCancel={vi.fn()} saving />
    );
    expect(screen.getByText('Guardando...')).toBeInTheDocument();
    expect(screen.getByText('Guardando...')).toBeDisabled();
  });

  it('llama a onCancel al hacer clic en el backdrop', () => {
    const onCancel = vi.fn();
    render(
      <CatalogoModal open={true} title="Nuevo" fields={fields} initialValues={{}} onSave={vi.fn()} onCancel={onCancel} />
    );
    fireEvent.click(document.querySelector('.dialog-overlay')!);
    expect(onCancel).toHaveBeenCalled();
  });
});
