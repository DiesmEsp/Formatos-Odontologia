import { describe, it, expect } from 'vitest';
import { renderHook, waitFor, act } from '@testing-library/react';
import { useApi } from '../useApi';

describe('useApi', () => {
  it('inicia con loading true y data null', () => {
    const { result } = renderHook(() =>
      useApi(() => Promise.resolve(42))
    );
    expect(result.current.loading).toBe(true);
    expect(result.current.data).toBeNull();
    expect(result.current.error).toBeNull();
  });

  it('retorna data al resolverse la promesa', async () => {
    const { result } = renderHook(() =>
      useApi(() => Promise.resolve({ id: 1, name: 'test' }))
    );

    await waitFor(() => {
      expect(result.current.loading).toBe(false);
    });

    expect(result.current.data).toEqual({ id: 1, name: 'test' });
    expect(result.current.error).toBeNull();
  });

  it('retorna error al rechazarse la promesa', async () => {
    const { result } = renderHook(() =>
      useApi(() => Promise.reject(new Error('Fallo la conexion')))
    );

    await waitFor(() => {
      expect(result.current.loading).toBe(false);
    });

    expect(result.current.data).toBeNull();
    expect(result.current.error).toBe('Fallo la conexion');
  });

  it('refetch actualiza los datos', async () => {
    let counter = 0;
    const { result } = renderHook(() =>
      useApi(() => Promise.resolve(++counter))
    );

    await waitFor(() => {
      expect(result.current.loading).toBe(false);
    });

    expect(result.current.data).toBe(1);

    await act(async () => {
      await result.current.refetch();
    });

    expect(result.current.data).toBe(2);
  });

  it('no actualiza estado si el componente se desmonto', async () => {
    let resolvePromise!: (value: unknown) => void;
    const promise = new Promise((resolve) => { resolvePromise = resolve; });

    const { result, unmount } = renderHook(() =>
      useApi(() => promise as Promise<string>)
    );

    expect(result.current.loading).toBe(true);
    unmount();

    resolvePromise('data tardia');
    await new Promise((r) => setTimeout(r, 10));

    expect(result.current.loading).toBe(true);
    expect(result.current.data).toBeNull();
  });
});
