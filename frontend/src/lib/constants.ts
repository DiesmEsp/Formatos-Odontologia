export const TRATAMIENTO_TIPOS = ['NORMAL', 'CONTINUO'] as const;
export const TRATAMIENTO_ESTADOS = ['ABIERTO', 'CERRADO', 'ANULADO'] as const;
export const ESTADO_PAGOS = ['PENDIENTE', 'PAGADO', 'PARCIAL'] as const;
export const GRADOS_OPERADOR = ['PRE', 'POS'] as const;
export const TIPOS_OPERADOR_PRE = ['3', '4', '5'] as const;
export const TIPOS_OPERADOR_POS = ['R1', 'R2', 'R3'] as const;
export const GRADO_LABELS: Record<string, string> = { PRE: 'Pregrado', POS: 'Posgrado' };

export const MESES = [
  'Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio',
  'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre',
] as const;

export const API_BASE = 'http://localhost:7070';

export const STATUS_MAP: Record<string, { label: string; variant: 'success' | 'warning' | 'danger' | 'info' | 'neutral' }> = {
  ABIERTO: { label: 'Abierto', variant: 'info' },
  CERRADO: { label: 'Cerrado', variant: 'success' },
  ANULADO: { label: 'Anulado', variant: 'danger' },
  PENDIENTE: { label: 'Pendiente', variant: 'warning' },
  PAGADO: { label: 'Pagado', variant: 'success' },
  PARCIAL: { label: 'Parcial', variant: 'info' },
  ACTIVO: { label: 'Activo', variant: 'success' },
  INACTIVO: { label: 'Inactivo', variant: 'neutral' },
};

export const COLORS_CHART = ['#0f7b72', '#b5791f', '#2a6b7a', '#c2403a', '#5c7178', '#8a5e11', '#1e8f5c', '#9db8bc'];
