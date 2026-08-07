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
            value={row.cantidad === 0 ? '' : row.cantidad}
            onChange={(e) => {
              const val = e.target.value.replace(/[^0-9.]/g, '');
              const num = val === '' || val === '.' ? 0 : Number(val);
              onCantidadChange(row.key, num);
            }}
            min={0}
            step={0.01}
            readOnly={readOnly}
          />
          {!readOnly && (
            <button className="btn btn-ghost btn-sm material-row-remove" onClick={() => onRemove(row.key)}>
              <Trash2 size={14} />
            </button>
          )}
        </div>
      ))}
      {!readOnly && (
        <button className="material-table-add" onClick={onAdd}>
          <Plus size={14} />
          <span>Agregar material</span>
        </button>
      )}
    </div>
  );
}
