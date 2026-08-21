import { useState, useEffect } from 'react';
import { X } from 'lucide-react';
import { useToast } from '../../hooks/useToast';
import { useModalDraft } from '../../hooks/useModalDraft';
import { api } from '../../api';

interface PacienteDraft {
  nombres: string;
  apellidos: string;
}

export function CrearPacienteOnTheFly({ onClose, onCreated, addToast }: { onClose: () => void; onCreated: (id: number) => void; addToast: ReturnType<typeof useToast>['addToast'] }) {
  const { draft, saveDraft, clearDraft } = useModalDraft<PacienteDraft>('crear-paciente');
  const [nombres, setNombres] = useState(draft?.nombres ?? '');
  const [apellidos, setApellidos] = useState(draft?.apellidos ?? '');
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    saveDraft({ nombres, apellidos });
  }, [nombres, apellidos, saveDraft]);

  const handleSave = async () => {
    if (!nombres.trim() || !apellidos.trim()) { addToast('error', 'Complete nombre y apellido'); return; }
    setSaving(true);
    try {
      const result = await api.catalogos.pacientes.crear({ nombres: nombres.trim(), apellidos: apellidos.trim() });
      addToast('success', 'Paciente creado');
      clearDraft();
      onCreated(result.id);
    } catch (err) { addToast('error', err instanceof Error ? err.message : 'Error al crear'); }
    finally { setSaving(false); }
  };

  return (
    <div className="dialog-overlay overlay-top" onClick={onClose}>
      <div className="dialog-pane mw-420" onClick={(e) => e.stopPropagation()}>
        <div className="dialog-header"><h3 className="dialog-title">Nuevo Paciente</h3><button className="btn btn-ghost btn-sm" onClick={() => { clearDraft(); onClose(); }}><X size={18} /></button></div>
        <div className="dialog-body">
          <div className="form-group"><label className="form-label">Nombres</label><input className="text-field w-full" value={nombres} onChange={(e) => setNombres(e.target.value)} /></div>
          <div className="form-group"><label className="form-label">Apellidos</label><input className="text-field w-full" value={apellidos} onChange={(e) => setApellidos(e.target.value)} /></div>
        </div>
        <div className="dialog-footer">
          <button className="btn btn-secondary" onClick={onClose}>Cancelar</button>
          <button className="btn btn-primary" onClick={handleSave} disabled={saving}>{saving ? 'Guardando...' : 'Guardar y volver'}</button>
        </div>
      </div>
    </div>
  );
}
