import { useCallback, useState } from 'react';

const STORAGE_PREFIX = 'formatos.modal.draft.';

function safeGetItem(key: string): string | null {
  try {
    return sessionStorage.getItem(key);
  } catch {
    return null;
  }
}

function safeSetItem(key: string, value: string): boolean {
  try {
    sessionStorage.setItem(key, value);
    return true;
  } catch {
    return false;
  }
}

function safeRemoveItem(key: string): void {
  try {
    sessionStorage.removeItem(key);
  } catch {
    void 0;
  }
}

export function useModalDraft<T>(key: string) {
  const storageKey = STORAGE_PREFIX + key;

  const [draft, setDraft] = useState<T | null>(() => {
    const raw = safeGetItem(storageKey);
    if (!raw) return null;
    try {
      return JSON.parse(raw) as T;
    } catch {
      safeRemoveItem(storageKey);
      return null;
    }
  });

  const saveDraft = useCallback((value: T) => {
    setDraft(value);
    safeSetItem(storageKey, JSON.stringify(value));
  }, [storageKey]);

  const clearDraft = useCallback(() => {
    setDraft(null);
    safeRemoveItem(storageKey);
  }, [storageKey]);

  return { draft, saveDraft, clearDraft };
}
