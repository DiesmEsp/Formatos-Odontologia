import { describe, it, expect } from 'vitest';
import { renderHook, act } from '@testing-library/react';
import { usePagination } from '../usePagination';

describe('usePagination', () => {
  it('inicia en pagina 1', () => {
    const { result } = renderHook(() =>
      usePagination({ totalItems: 100, pageSize: 10 })
    );
    expect(result.current.currentPage).toBe(1);
  });

  it('calcula totalPages correctamente', () => {
    const { result } = renderHook(() =>
      usePagination({ totalItems: 45, pageSize: 10 })
    );
    expect(result.current.totalPages).toBe(5);
  });

  it('start y end calculan el rango correcto', () => {
    const { result } = renderHook(() =>
      usePagination({ totalItems: 100, pageSize: 10 })
    );
    expect(result.current.start).toBe(0);
    expect(result.current.end).toBe(10);
  });

  it('next avanza de pagina', () => {
    const { result } = renderHook(() =>
      usePagination({ totalItems: 100, pageSize: 10 })
    );

    act(() => result.current.next());
    expect(result.current.currentPage).toBe(2);
  });

  it('prev retrocede de pagina', () => {
    const { result } = renderHook(() =>
      usePagination({ totalItems: 100, pageSize: 10 })
    );

    act(() => result.current.goTo(3));
    act(() => result.current.prev());
    expect(result.current.currentPage).toBe(2);
  });

  it('no puede ir antes de la pagina 1', () => {
    const { result } = renderHook(() =>
      usePagination({ totalItems: 100, pageSize: 10 })
    );

    act(() => result.current.prev());
    expect(result.current.currentPage).toBe(1);
  });

  it('no puede ir despues de la ultima pagina', () => {
    const { result } = renderHook(() =>
      usePagination({ totalItems: 20, pageSize: 10 })
    );

    act(() => result.current.goTo(3));
    expect(result.current.currentPage).toBe(2);
  });

  it('resiste 0 items', () => {
    const { result } = renderHook(() =>
      usePagination({ totalItems: 0, pageSize: 10 })
    );
    expect(result.current.totalPages).toBe(1);
    expect(result.current.currentPage).toBe(1);
  });
});
