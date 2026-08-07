import { MESES } from '../lib/constants';

interface MonthYearPickerProps {
  mes: number;
  anio: number;
  onMesChange: (mes: number) => void;
  onAnioChange: (anio: number) => void;
  onGenerate: () => void;
  generating?: boolean;
  label?: string;
  showAnual?: boolean;
  onGenerateAnual?: () => void;
}

export function MonthYearPicker({
  mes,
  anio,
  onMesChange,
  onAnioChange,
  onGenerate,
  generating = false,
  label = 'Generar reporte',
  showAnual = false,
  onGenerateAnual,
}: MonthYearPickerProps) {
  const currentYear = new Date().getFullYear();
  const years: number[] = [];
  for (let y = currentYear; y >= currentYear - 10; y--) {
    years.push(y);
  }

  return (
    <div className="month-year-picker">
      <div className="picker-controls">
        <select className="combo-box picker-select" value={mes} onChange={(e) => onMesChange(Number(e.target.value))}>
          {MESES.map((nombre, idx) => (
            <option key={idx + 1} value={idx + 1}>{nombre}</option>
          ))}
        </select>
        <select className="combo-box picker-select" value={anio} onChange={(e) => onAnioChange(Number(e.target.value))}>
          {years.map((y) => (
            <option key={y} value={y}>{y}</option>
          ))}
        </select>
        <button className="btn btn-primary" onClick={onGenerate} disabled={generating}>
          {generating ? 'Generando...' : label}
        </button>
        {showAnual && onGenerateAnual && (
          <button className="btn btn-secondary" onClick={onGenerateAnual} disabled={generating}>
            Anual {anio}
          </button>
        )}
      </div>
    </div>
  );
}
