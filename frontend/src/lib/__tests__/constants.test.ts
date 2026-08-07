import { describe, it, expect } from 'vitest';
import { MESES, COLORS_CHART, STATUS_MAP, TRATAMIENTO_TIPOS, TRATAMIENTO_ESTADOS, ESTADO_PAGOS, GRADOS_OPERADOR } from '../constants';

describe('MESES', () => {
  it('tiene 12 meses', () => {
    expect(MESES).toHaveLength(12);
  });

  it('comienza con Enero', () => {
    expect(MESES[0]).toBe('Enero');
  });

  it('termina con Diciembre', () => {
    expect(MESES[11]).toBe('Diciembre');
  });
});

describe('COLORS_CHART', () => {
  it('tiene 8 colores', () => {
    expect(COLORS_CHART).toHaveLength(8);
  });

  it('todos los colores son strings hex validos', () => {
    COLORS_CHART.forEach((color) => {
      expect(color).toMatch(/^#[0-9a-fA-F]{6}$/);
    });
  });
});

describe('STATUS_MAP', () => {
  it('tiene entradas para estados de tratamiento', () => {
    expect(STATUS_MAP['ABIERTO']).toBeDefined();
    expect(STATUS_MAP['CERRADO']).toBeDefined();
    expect(STATUS_MAP['ANULADO']).toBeDefined();
  });

  it('tiene entradas para estados de pago', () => {
    expect(STATUS_MAP['PENDIENTE']).toBeDefined();
    expect(STATUS_MAP['PAGADO']).toBeDefined();
    expect(STATUS_MAP['PARCIAL']).toBeDefined();
  });

  it('cada entrada tiene label y variant', () => {
    Object.values(STATUS_MAP).forEach((entry) => {
      expect(entry).toHaveProperty('label');
      expect(entry).toHaveProperty('variant');
    });
  });
});

describe('arrays de tipos', () => {
  it('TRATAMIENTO_TIPOS contiene NORMAL y CONTINUO', () => {
    expect(TRATAMIENTO_TIPOS).toContain('NORMAL');
    expect(TRATAMIENTO_TIPOS).toContain('CONTINUO');
  });

  it('TRATAMIENTO_ESTADOS tiene 3 valores', () => {
    expect(TRATAMIENTO_ESTADOS).toHaveLength(3);
  });

  it('ESTADO_PAGOS tiene 3 valores', () => {
    expect(ESTADO_PAGOS).toHaveLength(3);
  });

  it('GRADOS_OPERADOR contiene PRE y POS', () => {
    expect(GRADOS_OPERADOR).toContain('PRE');
    expect(GRADOS_OPERADOR).toContain('POS');
  });
});
