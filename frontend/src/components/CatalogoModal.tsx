import { useState } from 'react';
import { X } from 'lucide-react';

interface Field {
  key: string;
  label: string;
  type: 'text' | 'number' | 'select' | 'readonly';
  options?: { label: string; value: string }[];
  placeholder?: string;
}

interface CatalogoModalProps {
  open: boolean;
  title: string;
  fields: Field[];
  initialValues: Record<string, any>;
  onSave: (values: Record<string, any>) => void;
  onCancel: () => void;
  saving?: boolean;
}

export function CatalogoModal({
  open,
  title,
  fields,
  initialValues,
  onSave,
  onCancel,
  saving = false,
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

  return (
    <div className="dialog-overlay" onClick={handleCancel}>
      <div className="dialog-pane" onClick={(e) => e.stopPropagation()} style={{ maxWidth: 480 }}>
        <div className="dialog-header">
          <h3 className="dialog-title">{title}</h3>
          <button className="btn btn-ghost btn-sm dialog-close" onClick={handleCancel}>
            <X size={18} />
          </button>
        </div>
        <form onSubmit={handleSubmit}>
          <div className="dialog-body">
            {fields.map((f) => (
              <div key={f.key} className="form-group" style={{ marginBottom: 14 }}>
                <label className="form-label">{f.label}</label>
                {f.type === 'select' ? (
                  <select
                    className="combo-box"
                    value={values[f.key] ?? ''}
                    onChange={(e) => setField(f.key, e.target.value)}
                    style={{ width: '100%' }}
                  >
                    <option value="">Seleccionar...</option>
                    {f.options?.map((opt) => (
                      <option key={opt.value} value={opt.value}>{opt.label}</option>
                    ))}
                  </select>
                ) : (
                  <input
                    type={f.type === 'number' ? 'number' : 'text'}
                    className="text-field"
                    value={values[f.key] ?? ''}
                    onChange={(e) => setField(f.key, f.type === 'number' ? Number(e.target.value) : e.target.value)}
                    placeholder={f.placeholder}
                    readOnly={f.type === 'readonly'}
                    step={f.type === 'number' ? '0.01' : undefined}
                    style={{ width: '100%' }}
                  />
                )}
              </div>
            ))}
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
