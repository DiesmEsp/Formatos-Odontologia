import { useState } from 'react';
import { X } from 'lucide-react';
import { useToast } from '../../hooks/useToast';
import { api } from '../../api';

const TIPOS_PRE = ['3', '4', '5'];
const TIPOS_POS = ['R1', 'R2', 'R3'];

export function CrearOperadorOnTheFly({ onClose, onCreated, addToast }: { onClose: () => void; onCreated: (id: number) => void; addToast: ReturnType<typeof useToast>['addToast'] }) {
  const [nombres, setNombres] = useState('');
  const [apellidos, setApellidos] = useState('');
  const [dni, setDni] = useState('');
  const [grado, setGrado] = useState('PRE');
  const [tipoOp, setTipoOp] = useState('3');
  const [periodo, setPeriodo] = useState(new Date().getFullYear());
  const [saving, setSaving] = useState(false);

  const tipos = grado === 'PRE' ? TIPOS_PRE : TIPOS_POS;

  const handleGradoChange = (g: string) => { setGrado(g); setTipoOp(g === 'PRE' ? '3' : 'R1'); };

  const handleSave = async () => {
    if (!nombres.trim() || !apellidos.trim()) { addToast('error', 'Complete nombre y apellido'); return; }
    setSaving(true);
    try {
      const result = await api.catalogos.operadores.crear({ nombres: nombres.trim(), apellidos: apellidos.trim(), dni: dni.trim() || undefined, grado, tipo: tipoOp, periodo });
      addToast('success', 'Operador creado');
      onCreated(result.id);
    } catch (err) { addToast('error', err instanceof Error ? err.message : 'Error al crear'); }
    finally { setSaving(false); }
  };

  return (
    <div className="dialog-overlay overlay-top" onClick={onClose}>
      <div className="dialog-pane mw-460" onClick={(e) => e.stopPropagation()}>
        <div className="dialog-header"><h3 className="dialog-title">Nuevo Operador</h3><button className="btn btn-ghost btn-sm" onClick={onClose}><X size={18} /></button></div>
        <div className="dialog-body">
          <div className="form-row">
            <div className="form-group"><label className="form-label">Nombres</label><input className="text-field w-full" value={nombres} onChange={(e) => setNombres(e.target.value)} /></div>
            <div className="form-group"><label className="form-label">Apellidos</label><input className="text-field w-full" value={apellidos} onChange={(e) => setApellidos(e.target.value)} /></div>
          </div>
          <div className="form-group"><label className="form-label">DNI (opcional)</label><input className="text-field w-full" value={dni} onChange={(e) => setDni(e.target.value)} placeholder="Opcional" /></div>
          <div className="form-row">
            <div className="form-group">
              <label className="form-label">Grado</label>
              <select className="combo-box w-full" value={grado} onChange={(e) => handleGradoChange(e.target.value)}>
                <option value="PRE">Pregrado (PRE)</option>
                <option value="POS">Posgrado (POS)</option>
              </select>
            </div>
            <div className="form-group">
              <label className="form-label">Tipo</label>
              <select className="combo-box w-full" value={tipoOp} onChange={(e) => setTipoOp(e.target.value)}>
                {tipos.map((t) => <option key={t} value={t}>{t}</option>)}
              </select>
            </div>
          </div>
          <div className="form-group"><label className="form-label">Periodo (ano)</label><input type="number" className="text-field w-full" value={periodo} onChange={(e) => setPeriodo(Math.trunc(Number(e.target.value)))} step={1} inputMode="numeric" min={2000} /></div>
        </div>
        <div className="dialog-footer">
          <button className="btn btn-secondary" onClick={onClose}>Cancelar</button>
          <button className="btn btn-primary" onClick={handleSave} disabled={saving}>{saving ? 'Guardando...' : 'Guardar y volver'}</button>
        </div>
      </div>
    </div>
  );
}
