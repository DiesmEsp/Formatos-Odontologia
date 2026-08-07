import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { ConfirmDialog } from '../ConfirmDialog';

describe('ConfirmDialog', () => {
  it('no renderiza cuando open es false', () => {
    const { container } = render(
      <ConfirmDialog open={false} title="Titulo" message="Mensaje" onConfirm={vi.fn()} onCancel={vi.fn()} />
    );
    expect(container.querySelector('.dialog-overlay')).toBeFalsy();
  });

  it('renderiza cuando open es true', () => {
    render(
      <ConfirmDialog open={true} title="Confirmar accion" message="Esta seguro?" onConfirm={vi.fn()} onCancel={vi.fn()} />
    );
    expect(screen.getByText('Confirmar accion')).toBeInTheDocument();
    expect(screen.getByText('Esta seguro?')).toBeInTheDocument();
  });

  it('llama a onConfirm al hacer clic en confirmar', () => {
    const onConfirm = vi.fn();
    render(
      <ConfirmDialog open={true} title="T" message="M" onConfirm={onConfirm} onCancel={vi.fn()} />
    );
    fireEvent.click(screen.getByText('Confirmar'));
    expect(onConfirm).toHaveBeenCalledTimes(1);
  });

  it('llama a onCancel al hacer clic en cancelar', () => {
    const onCancel = vi.fn();
    render(
      <ConfirmDialog open={true} title="T" message="M" onConfirm={vi.fn()} onCancel={onCancel} />
    );
    fireEvent.click(screen.getByText('Cancelar'));
    expect(onCancel).toHaveBeenCalledTimes(1);
  });

  it('muestra textarea cuando requireMotivo es true', () => {
    render(
      <ConfirmDialog open={true} title="Anular" message="Motivo?" requireMotivo onConfirm={vi.fn()} onCancel={vi.fn()} />
    );
    expect(document.querySelector('textarea')).toBeTruthy();
  });

  it('el boton de confirmar esta deshabilitado si requireMotivo y el textarea esta vacio', () => {
    render(
      <ConfirmDialog open={true} title="Anular" message="Motivo?" requireMotivo onConfirm={vi.fn()} onCancel={vi.fn()} />
    );
    const btn = screen.getByText('Confirmar');
    expect(btn).toBeDisabled();
  });

  it('el boton se habilita al escribir motivo', () => {
    render(
      <ConfirmDialog open={true} title="Anular" message="Motivo?" requireMotivo onConfirm={vi.fn()} onCancel={vi.fn()} />
    );
    const textarea = document.querySelector('textarea')!;
    fireEvent.change(textarea, { target: { value: 'Motivo de prueba' } });
    expect(screen.getByText('Confirmar')).not.toBeDisabled();
  });

  it('paso del motivo a onConfirm', () => {
    const onConfirm = vi.fn();
    render(
      <ConfirmDialog open={true} title="Anular" message="Motivo?" requireMotivo onConfirm={onConfirm} onCancel={vi.fn()} />
    );
    fireEvent.change(document.querySelector('textarea')!, { target: { value: 'Error en datos' } });
    fireEvent.click(screen.getByText('Confirmar'));
    expect(onConfirm).toHaveBeenCalledWith('Error en datos');
  });

  it('usa labels personalizados', () => {
    render(
      <ConfirmDialog open={true} title="T" message="M" confirmLabel="Si, eliminar" cancelLabel="No" onConfirm={vi.fn()} onCancel={vi.fn()} />
    );
    expect(screen.getByText('Si, eliminar')).toBeInTheDocument();
    expect(screen.getByText('No')).toBeInTheDocument();
  });
});
