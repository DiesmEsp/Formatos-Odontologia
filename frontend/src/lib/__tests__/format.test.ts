import { describe, it, expect } from 'vitest';
import { formatMonto, formatFecha, formatMes, formatDateTime, formatBytes, hoyISO, mesActual, anioActual, nombreCompleto, horaActual, formatearHora, calcularDuracion } from '../format';

describe('formatMonto', () => {
  it('formatea montos positivos', () => {
    expect(formatMonto(100)).toBe('S/ 100.00');
    expect(formatMonto(50.5)).toBe('S/ 50.50');
  });

  it('formatea cero', () => {
    expect(formatMonto(0)).toBe('S/ 0.00');
  });

  it('formatea montos negativos', () => {
    expect(formatMonto(-25)).toBe('S/ -25.00');
  });
});

describe('formatFecha', () => {
  it('formatea una fecha ISO', () => {
    const result = formatFecha('2026-08-07');
    expect(result).toMatch(/\d{2}\/\d{2}\/\d{4}/);
  });

  it('retorna vacio para string vacio', () => {
    expect(formatFecha('')).toBe('');
  });
});

describe('formatMes', () => {
  it('retorna el nombre del mes', () => {
    expect(formatMes(1)).toBe('Enero');
    expect(formatMes(7)).toBe('Julio');
    expect(formatMes(12)).toBe('Diciembre');
  });

  it('retorna vacio para mes invalido', () => {
    expect(formatMes(0)).toBe('');
    expect(formatMes(13)).toBe('');
  });
});

describe('formatDateTime', () => {
  it('formatea un timestamp', () => {
    expect(formatDateTime('2026-08-07T10:30:00')).toMatch(/07\/08\/2026/);
  });

  it('retorna vacio para string vacio', () => {
    expect(formatDateTime('')).toBe('');
  });
});

describe('formatBytes', () => {
  it('retorna bytes para valores menores a 1KB', () => {
    expect(formatBytes(512)).toBe('512 B');
  });

  it('retorna KB', () => {
    expect(formatBytes(2048)).toMatch(/KB/);
  });

  it('retorna MB', () => {
    expect(formatBytes(3 * 1024 * 1024)).toMatch(/MB/);
  });
});

describe('hoyISO', () => {
  it('retorna una fecha en formato YYYY-MM-DD', () => {
    expect(hoyISO()).toMatch(/^\d{4}-\d{2}-\d{2}$/);
  });
});

describe('mesActual', () => {
  it('retorna un numero entre 1 y 12', () => {
    const mes = mesActual();
    expect(mes).toBeGreaterThanOrEqual(1);
    expect(mes).toBeLessThanOrEqual(12);
  });
});

describe('anioActual', () => {
  it('retorna el año actual', () => {
    const anio = anioActual();
    expect(anio).toBe(new Date().getFullYear());
  });
});

describe('nombreCompleto', () => {
  it('concatena nombres y apellidos', () => {
    expect(nombreCompleto('Maria', 'Gonzalez')).toBe('Maria Gonzalez');
  });
});

describe('horaActual', () => {
  it('retorna formato HH:mm:ss', () => {
    expect(horaActual()).toMatch(/^\d{2}:\d{2}:\d{2}$/);
  });
});

describe('formatearHora', () => {
  it('retorna HH:mm desde HH:mm:ss', () => {
    expect(formatearHora('08:30:00')).toBe('08:30');
    expect(formatearHora('13:45:22')).toBe('13:45');
  });

  it('retorna HH:mm desde HH:mm', () => {
    expect(formatearHora('08:30')).toBe('08:30');
  });

  it('retorna vacio para null', () => {
    expect(formatearHora(null)).toBe('');
  });
});

describe('calcularDuracion', () => {
  it('calcula horas y minutos', () => {
    expect(calcularDuracion('08:00', '09:30')).toBe('1h 30m');
  });

  it('calcula solo minutos', () => {
    expect(calcularDuracion('08:00', '08:45')).toBe('45m');
  });

  it('calcula solo horas', () => {
    expect(calcularDuracion('08:00', '10:00')).toBe('2h');
  });

  it('retorna vacio si inicio es null', () => {
    expect(calcularDuracion(null, '10:00')).toBe('');
  });

  it('retorna vacio si fin es null', () => {
    expect(calcularDuracion('08:00', null)).toBe('');
  });

  it('retorna vacio si fin <= inicio', () => {
    expect(calcularDuracion('10:00', '08:00')).toBe('');
    expect(calcularDuracion('10:00', '10:00')).toBe('');
  });
});
