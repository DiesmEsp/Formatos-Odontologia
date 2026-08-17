const STORAGE_KEY = 'formatos.clinica-activa';

export interface ClinicaSesion {
  clinicaID: number;
  nombre: string;
  grupo?: string | null;
}

let clinicaActual: ClinicaSesion | null | undefined;

export function getClinicaSesion(): ClinicaSesion | null {
  if (clinicaActual === undefined) {
    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      clinicaActual = raw ? (JSON.parse(raw) as ClinicaSesion) : null;
    } catch {
      clinicaActual = null;
    }
  }
  return clinicaActual;
}

export function setClinicaSesion(clinica: ClinicaSesion | null): void {
  clinicaActual = clinica;
  if (clinica === null) {
    localStorage.removeItem(STORAGE_KEY);
  } else {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(clinica));
  }
}

export function nombreClinicaSesion(): string | null {
  return getClinicaSesion()?.nombre ?? null;
}