import { useRef, useCallback, useEffect } from 'react';
import { Plus, Trash2 } from 'lucide-react';
import { SearchableCombo, type SearchableOption } from './SearchableCombo';
import type { Materiales } from '../api/types';

export interface MaterialRow {
  key: string;
  materialId: number | null;
  nombreMaterial: string;
  cantidad: number;
}

interface MaterialTableProps {
  rows: MaterialRow[];
  materials: Materiales[];
  onAdd: () => void;
  onRemove: (key: string) => void;
  onMaterialChange: (key: string, materialId: number) => void;
  onCantidadChange: (key: string, cantidad: number) => void;
  onSearchMaterial?: (query: string) => void;
  loading?: boolean;
  readOnly?: boolean;
}

function toDisplay(cantidad: number): string {
  return cantidad === 0 ? '' : String(cantidad);
}

export function MaterialTable({
  rows,
  materials,
  onAdd,
  onRemove,
  onMaterialChange,
  onCantidadChange,
  onSearchMaterial,
  loading = false,
  readOnly = false,
}: MaterialTableProps) {
  const displayValues = useRef<Record<string, string>>({});
  const cantidadRefs = useRef<Record<string, HTMLInputElement | null>>({});
  const prevMaterialIds = useRef<Record<string, number | null> | null>(null);

  useEffect(() => {
    if (prevMaterialIds.current !== null) {
      for (const row of rows) {
        const prev = prevMaterialIds.current[row.key];
        if ((prev === null || prev === undefined) && typeof row.materialId === 'number') {
          const input = cantidadRefs.current[row.key];
          input?.focus();
          input?.select();
        }
      }
    }
    prevMaterialIds.current = Object.fromEntries(rows.map((r) => [r.key, r.materialId]));
  }, [rows]);

  const getDisplay = useCallback((key: string, cantidad: number): string => {
    if (key in displayValues.current) return displayValues.current[key];
    return toDisplay(cantidad);
  }, []);

  const setDisplay = (key: string, val: string) => {
    displayValues.current = { ...displayValues.current, [key]: val };
  };

  const syncToParent = (key: string, val: string) => {
    const cleaned = val.replace(/[^0-9.]/g, '');
    const parsed = cleaned === '' || cleaned === '.' ? 0 : Number(cleaned);
    delete displayValues.current[key];
    onCantidadChange(key, parsed);
  };

  const handleNumericKeyDown = (e: React.KeyboardEvent, currentVal: string) => {
    const allowed = ['Backspace','Delete','Tab','Escape','ArrowLeft','ArrowRight','ArrowUp','ArrowDown','Home','End'];
    if (allowed.includes(e.key)) return;
    if (e.ctrlKey || e.metaKey) return;
    if (e.key === '.' && !currentVal.includes('.')) return;
    if (e.key >= '0' && e.key <= '9') return;
    e.preventDefault();
  };

  const handleInputChange = (key: string, val: string) => {
    const cleaned = val.replace(/[^0-9.]/g, '');
    setDisplay(key, cleaned);
    const parsed = cleaned === '' || cleaned === '.' ? 0 : Number(cleaned);
    onCantidadChange(key, parsed);
  };

  const handleInputBlur = (key: string, val: string) => {
    syncToParent(key, val);
  };

  const handleInputKeyDown = (e: React.KeyboardEvent, key: string, val: string) => {
    handleNumericKeyDown(e, val);
    if (e.key === 'Enter') {
      e.preventDefault();
      syncToParent(key, val);
      (e.target as HTMLInputElement).blur();
    }
  };

  const materialOptions: SearchableOption[] = materials.map((m) => ({
    id: m.materialID,
    label: m.nombre,
    extra: m.unidad,
  }));

  if (rows.length === 0 && readOnly) {
    return <div className="empty-state"><span className="empty-text">Sin materiales registrados</span></div>;
  }

  return (
    <div className="material-table">
      <div className="material-table-header">
        <span className="material-table-header-label">Material</span>
        <span className="material-table-header-label">Cantidad</span>
        {!readOnly && <span className="material-table-header-label material-col-action" />}
      </div>
      {rows.map((row) => (
        <div key={row.key} className="material-row">
          {readOnly ? (
            <span className="material-row-name">{row.nombreMaterial}</span>
          ) : (
            <SearchableCombo
              options={materialOptions}
              value={row.materialId}
              onChange={(id) => id !== null && onMaterialChange(row.key, id)}
              placeholder="Buscar material..."
              onSearch={onSearchMaterial}
              loading={loading}
              className="material-row-combo"
            />
          )}
          <input
            type="text"
            inputMode="decimal"
            className="text-field material-row-input"
            ref={(el) => { cantidadRefs.current[row.key] = el; }}
            value={getDisplay(row.key, row.cantidad)}
            onChange={(e) => handleInputChange(row.key, e.target.value)}
            onBlur={(e) => handleInputBlur(row.key, e.target.value)}
            onKeyDown={(e) => handleInputKeyDown(e, row.key, e.currentTarget.value)}
            min={0}
            step={0.01}
            readOnly={readOnly}
          />
          {!readOnly && (
            <button type="button" className="btn btn-ghost btn-sm material-row-remove" onClick={() => onRemove(row.key)}>
              <Trash2 size={14} />
            </button>
          )}
        </div>
      ))}
      {!readOnly && (
        <button type="button" className="material-table-add" onClick={onAdd}>
          <Plus size={14} />
          <span>Agregar material</span>
        </button>
      )}
    </div>
  );
}
