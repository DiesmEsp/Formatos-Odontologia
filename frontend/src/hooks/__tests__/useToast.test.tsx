import { describe, it, expect, vi } from 'vitest';
import { renderHook, act } from '@testing-library/react';
import { ToastProvider, useToast } from '../useToast';
import { createElement } from 'react';

const wrapper = ({ children }: { children: React.ReactNode }) =>
  createElement(ToastProvider, null, children);

describe('useToast', () => {
  it('lanza error fuera del provider', () => {
    expect(() => renderHook(() => useToast())).toThrow(
      'useToast debe usarse dentro de ToastProvider'
    );
  });

  it('addToast agrega un toast', () => {
    const { result } = renderHook(() => useToast(), { wrapper });

    act(() => result.current.addToast('success', 'Operacion exitosa'));

    expect(result.current.toasts).toHaveLength(1);
    expect(result.current.toasts[0].type).toBe('success');
    expect(result.current.toasts[0].message).toBe('Operacion exitosa');
  });

  it('removeToast elimina un toast', () => {
    const { result } = renderHook(() => useToast(), { wrapper });

    act(() => result.current.addToast('error', 'Error'));
    const id = result.current.toasts[0].id;

    act(() => result.current.removeToast(id));

    expect(result.current.toasts).toHaveLength(0);
  });

  it('los IDs son unicos', () => {
    const { result } = renderHook(() => useToast(), { wrapper });

    act(() => {
      result.current.addToast('info', 'Uno');
      result.current.addToast('info', 'Dos');
      result.current.addToast('info', 'Tres');
    });

    const ids = result.current.toasts.map((t) => t.id);
    expect(new Set(ids).size).toBe(3);
  });

  it('auto-remueve toasts despues del timeout', async () => {
    vi.useFakeTimers();
    const { result } = renderHook(() => useToast(), { wrapper });

    act(() => result.current.addToast('warning', 'Advertencia'));
    expect(result.current.toasts).toHaveLength(1);

    act(() => vi.advanceTimersByTime(4000));
    expect(result.current.toasts).toHaveLength(0);

    vi.useRealTimers();
  });
});
