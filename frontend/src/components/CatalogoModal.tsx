import { useState } from 'react';
import { X } from 'lucide-react';

type FieldOption = { label: string; value: string };

interface Field {
  key: string;
  label: string;
  type: 'text' | 'number' | 'select' | 'readonly';
  options?: FieldOption[] | ((values: Record<string, any>) => FieldOption[]);
  placeholder?: string;
  integer?: boolean;
  step?: string | number;
  onFieldChange?: (value: string, setField: (key: string, value: any) => void) => void;
}

import type { ReactNode } from 'react';

interface CatalogoModalProps {
  open: boolean;
  title: string;
  fields: Field[];
  initialValues: Record<string, any>;
  onSave: (values: Record<string, any>) => void;
  onCancel: () => void;
  saving?: boolean;
  children?: ReactNode;
  width?: number;
}

export function CatalogoModal({
  open,
  title,
  fields,
  initialValues,
  onSave,
  onCancel,
  saving = false,
  children,
  width,
}: CatalogoModalProps) {
  const [values, setValues] = useState<Record<string, any>>(initialValues);

  if (!open) return null;

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onSave(values);
  };

  const setField = (key: string, value: any) => {
    setValues((prev) => ({ ...prev, [key]: value }));
  };

  const handleCancel = () => {
    setValues(initialValues);
    onCancel();
  };

  const resolveOptions = (f: Field): FieldOption[] => {
    if (typeof f.options === 'function') return f.options(values);
    return f.options ?? [];
  };

  return (
    <div className="dialog-overlay" onClick={handleCancel}>
      <div className="dialog-pane" onClick={(e) => e.stopPropagation()} style={{ maxWidth: width ?? 480 }}>
        <div className="dialog-header">
          <h3 className="dialog-title">{title}</h3>
          <button className="btn btn-ghost btn-sm dialog-close" onClick={handleCancel}>
            <X size={18} />
          </button>
        </div>
        <form onSubmit={handleSubmit}>
          <div className="dialog-body">
            <div style={{ maxHeight: '40vh', overflowY: 'auto', marginBottom: children ? 8 : 0 }}>
              {fields.map((f) => (
                <div key={f.key} className="form-group" style={{ marginBottom: 14 }}>
                  <label className="form-label">{f.label}</label>
                  {f.type === 'select' ? (
                    <select
                      className="combo-box"
                      value={values[f.key] ?? ''}
                      onChange={(e) => {
                        setField(f.key, e.target.value);
                        f.onFieldChange?.(e.target.value, setField);
                      }}
                      style={{ width: '100%' }}
                    >
                      <option value="">Seleccionar...</option>
                      {resolveOptions(f).map((opt) => (
                        <option key={opt.value} value={opt.value}>{opt.label}</option>
                      ))}
                    </select>
                  ) : (
                    <input
                      type={f.type === 'number' ? 'number' : 'text'}
                      className="text-field"
                      value={values[f.key] ?? ''}
                      onChange={(e) => {
                        if (f.type === 'number') {
                          const num = e.target.value === '' ? 0 : Number(e.target.value);
                          setField(f.key, f.integer ? Math.trunc(num) : num);
                        } else {
                          setField(f.key, e.target.value);
                        }
                      }}
                      placeholder={f.placeholder}
                      readOnly={f.type === 'readonly'}
                      step={f.step ?? (f.type === 'number' ? (f.integer ? 1 : '0.01') : undefined)}
                      inputMode={f.integer ? 'numeric' : undefined}
                      style={{ width: '100%' }}
                    />
                  )}
                </div>
              ))}
            </div>
            {children}
          </div>
          <div className="dialog-footer">
            <button type="button" className="btn btn-secondary" onClick={handleCancel}>Cancelar</button>
            <button type="submit" className="btn btn-primary" disabled={saving}>
              {saving ? 'Guardando...' : 'Guardar'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
