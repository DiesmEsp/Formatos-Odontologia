import { useState, useEffect, useCallback, useRef } from 'react';

interface UseApiState<T> {
  data: T | null;
  loading: boolean;
  error: string | null;
}

export function useApi<T>(
  fetcher: () => Promise<T>,
  deps: unknown[] = []
) {
  const [state, setState] = useState<UseApiState<T>>({
    data: null,
    loading: true,
    error: null,
  });
  const mounted = useRef(true);
  const fetcherRef = useRef(fetcher);
  fetcherRef.current = fetcher;

  const execute = useCallback(async () => {
    setState((prev) => ({ ...prev, loading: true, error: null }));
    try {
      const data = await fetcherRef.current();
      if (mounted.current) {
        setState({ data, loading: false, error: null });
      }
      return data;
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : 'Error desconocido';
      if (mounted.current) {
        setState({ data: null, loading: false, error: message });
      }
      throw err;
    }
  }, deps);

  useEffect(() => {
    mounted.current = true;
    execute().catch(() => {});
    return () => { mounted.current = false; };
  }, [execute]);

  return { ...state, refetch: execute };
}
