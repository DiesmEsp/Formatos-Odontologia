import { useState } from 'react';
import { X } from 'lucide-react';

interface ConfirmDialogProps {
  open: boolean;
  title: string;
  message: string;
  confirmLabel?: string;
  cancelLabel?: string;
  variant?: 'danger' | 'primary';
  requireMotivo?: boolean;
  onConfirm: (motivo?: string) => void;
  onCancel: () => void;
}

export function ConfirmDialog({
  open,
  title,
  message,
  confirmLabel = 'Confirmar',
  cancelLabel = 'Cancelar',
  variant = 'primary',
  requireMotivo = false,
  onConfirm,
  onCancel,
}: ConfirmDialogProps) {
  const [motivo, setMotivo] = useState('');

  if (!open) return null;

  const handleConfirm = () => {
    if (requireMotivo && !motivo.trim()) return;
    onConfirm(requireMotivo ? motivo : undefined);
    setMotivo('');
  };

  const handleCancel = () => {
    setMotivo('');
    onCancel();
  };

  return (
    <div className="dialog-overlay" onClick={handleCancel}>
      <div className="dialog-pane" onClick={(e) => e.stopPropagation()}>
        <div className="dialog-header">
          <h3 className="dialog-title">{title}</h3>
          <button className="btn btn-ghost btn-sm dialog-close" onClick={handleCancel}>
            <X size={18} />
          </button>
        </div>
        <div className="dialog-body">
          <p className="dialog-message">{message}</p>
          {requireMotivo && (
            <div className="form-group">
              <label className="form-label">Motivo</label>
              <textarea
                className="text-field dialog-textarea"
                value={motivo}
                onChange={(e) => setMotivo(e.target.value)}
                placeholder="Describa el motivo..."
                rows={3}
              />
            </div>
          )}
        </div>
        <div className="dialog-footer">
          <button className="btn btn-secondary" onClick={handleCancel}>
            {cancelLabel}
          </button>
          <button
            className={`btn btn-${variant}`}
            onClick={handleConfirm}
            disabled={requireMotivo && !motivo.trim()}
          >
            {confirmLabel}
          </button>
        </div>
      </div>
    </div>
  );
}
