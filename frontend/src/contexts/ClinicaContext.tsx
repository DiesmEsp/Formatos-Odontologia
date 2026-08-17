import { createContext, useCallback, useContext, useMemo, useState } from 'react';
import type { ReactNode } from 'react';
import type { ClinicaSesion } from '../lib/clinicaStore';
import { getClinicaSesion, setClinicaSesion } from '../lib/clinicaStore';

interface ClinicaContextValue {
  clinica: ClinicaSesion | null;
  seleccionarClinica: (clinica: ClinicaSesion) => void;
  limpiarClinica: () => void;
}

const ClinicaContext = createContext<ClinicaContextValue | undefined>(undefined);

export function ClinicaProvider({ children }: { children: ReactNode }) {
  const [clinica, setClinica] = useState<ClinicaSesion | null>(() => getClinicaSesion());

  const seleccionarClinica = useCallback((nueva: ClinicaSesion) => {
    setClinicaSesion(nueva);
    setClinica(nueva);
  }, []);

  const limpiarClinica = useCallback(() => {
    setClinicaSesion(null);
    setClinica(null);
  }, []);

  const value = useMemo(
    () => ({ clinica, seleccionarClinica, limpiarClinica }),
    [clinica, seleccionarClinica, limpiarClinica]
  );

  return <ClinicaContext.Provider value={value}>{children}</ClinicaContext.Provider>;
}

export function useClinica(): ClinicaContextValue {
  const ctx = useContext(ClinicaContext);
  if (!ctx) {
    throw new Error('useClinica debe usarse dentro de ClinicaProvider');
  }
  return ctx;
}