import { MESES } from '../lib/constants';

interface MonthYearRangePickerProps {
  mesInicio: number;
  anioInicio: number;
  mesFin: number;
  anioFin: number;
  onMesInicioChange: (mes: number) => void;
  onAnioInicioChange: (anio: number) => void;
  onMesFinChange: (mes: number) => void;
  onAnioFinChange: (anio: number) => void;
  onGenerate: () => void;
  generating?: boolean;
}

export function MonthYearRangePicker({
  mesInicio,
  anioInicio,
  mesFin,
  anioFin,
  onMesInicioChange,
  onAnioInicioChange,
  onMesFinChange,
  onAnioFinChange,
  onGenerate,
  generating = false,
}: MonthYearRangePickerProps) {
  const currentYear = new Date().getFullYear();
  const years: number[] = [];
  for (let y = currentYear; y >= currentYear - 10; y--) {
    years.push(y);
  }

  return (
    <div className="month-year-picker">
      <div className="picker-controls">
        <span className="text-muted text-sm" style={{ marginRight: 4 }}>Desde</span>
        <select className="combo-box picker-select" value={mesInicio} onChange={(e) => onMesInicioChange(Number(e.target.value))}>
          {MESES.map((nombre, idx) => (
            <option key={idx + 1} value={idx + 1}>{nombre}</option>
          ))}
        </select>
        <select className="combo-box picker-select" value={anioInicio} onChange={(e) => onAnioInicioChange(Number(e.target.value))}>
          {years.map((y) => (
            <option key={y} value={y}>{y}</option>
          ))}
        </select>
        <span className="text-muted text-sm" style={{ marginLeft: 8, marginRight: 4 }}>Hasta</span>
        <select className="combo-box picker-select" value={mesFin} onChange={(e) => onMesFinChange(Number(e.target.value))}>
          {MESES.map((nombre, idx) => (
            <option key={idx + 1} value={idx + 1}>{nombre}</option>
          ))}
        </select>
        <select className="combo-box picker-select" value={anioFin} onChange={(e) => onAnioFinChange(Number(e.target.value))}>
          {years.map((y) => (
            <option key={y} value={y}>{y}</option>
          ))}
        </select>
        <button className="btn btn-primary" onClick={onGenerate} disabled={generating}>
          {generating ? 'Generando...' : 'Generar consolidado'}
        </button>
      </div>
    </div>
  );
}
