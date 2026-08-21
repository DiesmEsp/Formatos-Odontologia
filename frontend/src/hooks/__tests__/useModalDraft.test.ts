import { describe, it, expect, beforeEach } from 'vitest';
import { renderHook, act } from '@testing-library/react';
import { useModalDraft } from '../useModalDraft';

interface DraftMock {
  nombres: string;
  cantidad: number;
}

describe('useModalDraft', () => {
  beforeEach(() => {
    sessionStorage.clear();
  });

  it('retorna null cuando no hay draft guardado', () => {
    const { result } = renderHook(() => useModalDraft<DraftMock>('test-uno'));
    expect(result.current.draft).toBeNull();
  });

  it('lee el draft existente de sessionStorage al montar', () => {
    sessionStorage.setItem('formatos.modal.draft.test-dos', JSON.stringify({ nombres: 'Juan', cantidad: 2 }));
    const { result } = renderHook(() => useModalDraft<DraftMock>('test-dos'));
    expect(result.current.draft).toEqual({ nombres: 'Juan', cantidad: 2 });
  });

  it('saveDraft persiste en sessionStorage y actualiza el estado', () => {
    const { result } = renderHook(() => useModalDraft<DraftMock>('test-tres'));
    act(() => result.current.saveDraft({ nombres: 'Maria', cantidad: 5 }));
    expect(result.current.draft).toEqual({ nombres: 'Maria', cantidad: 5 });
    expect(sessionStorage.getItem('formatos.modal.draft.test-tres')).toBe(JSON.stringify({ nombres: 'Maria', cantidad: 5 }));
  });

  it('clearDraft elimina la entrada de sessionStorage y pone draft en null', () => {
    sessionStorage.setItem('formatos.modal.draft.test-cuatro', JSON.stringify({ nombres: 'Luis', cantidad: 1 }));
    const { result } = renderHook(() => useModalDraft<DraftMock>('test-cuatro'));
    expect(result.current.draft).toEqual({ nombres: 'Luis', cantidad: 1 });
    act(() => result.current.clearDraft());
    expect(result.current.draft).toBeNull();
    expect(sessionStorage.getItem('formatos.modal.draft.test-cuatro')).toBeNull();
  });

  it('tolera JSON corrupto retornando null y limpiando la entrada', () => {
    sessionStorage.setItem('formatos.modal.draft.test-cinco', '{json-invalido');
    const { result } = renderHook(() => useModalDraft<DraftMock>('test-cinco'));
    expect(result.current.draft).toBeNull();
    expect(sessionStorage.getItem('formatos.modal.draft.test-cinco')).toBeNull();
  });

  it('claves distintas no comparten draft', () => {
    sessionStorage.setItem('formatos.modal.draft.key-a', JSON.stringify({ nombres: 'A', cantidad: 1 }));
    const { result: ra } = renderHook(() => useModalDraft<DraftMock>('key-a'));
    const { result: rb } = renderHook(() => useModalDraft<DraftMock>('key-b'));
    expect(ra.current.draft).toEqual({ nombres: 'A', cantidad: 1 });
    expect(rb.current.draft).toBeNull();
  });
});
