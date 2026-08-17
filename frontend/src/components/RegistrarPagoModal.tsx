import { useState } from 'react';
import { X } from 'lucide-react';
import { useToast } from '../hooks/useToast';
import { api } from '../api';
import { formatMonto, hoyISO } from '../lib/format';
import type { Tratamiento } from '../api/types';

export function RegistrarPagoModal({
  tratamiento,
  onClose,
  onSuccess,
  addToast,
}: {
  tratamiento: Tratamiento;
  onClose: () => void;
  onSuccess: () => void;
  addToast: ReturnType<typeof useToast>['addToast'];
}) {
  const [abono, setAbono] = useState(0);
  const [fecha, setFecha] = useState(hoyISO());
  const [saving, setSaving] = useState(false);
  const saldo = tratamiento.monto - tratamiento.montoPagado;

  const handleGuardar = async () => {
    if (abono <= 0) return;
    setSaving(true);
    try {
      await api.tratamientos.registrarPago(tratamiento.tratamientoID, abono, fecha);
      addToast('success', 'Pago registrado');
      onSuccess();
    } catch (err) {
      addToast('error', err instanceof Error ? err.message : 'Error al registrar pago');
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="dialog-overlay" onClick={onClose}>
      <div className="dialog-pane mw-420" role="dialog" aria-modal="true" aria-labelledby="registrar-pago-title" onClick={(e) => e.stopPropagation()}>
        <div className="dialog-header">
          <h3 className="dialog-title" id="registrar-pago-title">Registrar Pago</h3>
          <button className="btn btn-ghost btn-sm" onClick={onClose}><X size={18} /></button>
        </div>
        <div className="dialog-body">
          <div className="sv-grid mb-16">
            <div className="sv-row"><span className="sv-label">Monto total</span><span className="num">{formatMonto(tratamiento.monto)}</span></div>
            <div className="sv-row"><span className="sv-label">Pagado</span><span className="num">{formatMonto(tratamiento.montoPagado)}</span></div>
            <div className="sv-row"><span className="sv-label">Saldo</span><span className={`num ${saldo > 0 ? 'text-danger' : 'text-success'}`}>{formatMonto(saldo)}</span></div>
          </div>
          <div className="form-group"><label className="form-label">Fecha</label><input type="date" className="text-field w-full" value={fecha} onChange={(e) => setFecha(e.target.value)} /></div>
          <div className="form-group"><label className="form-label">Monto a abonar</label><input type="number" className="text-field w-full" value={abono} onChange={(e) => setAbono(Number(e.target.value))} min={0.01} step="0.01" /></div>
        </div>
        <div className="dialog-footer">
          <button className="btn btn-secondary" onClick={onClose}>Cancelar</button>
          <button className="btn btn-primary" onClick={handleGuardar} disabled={saving || abono <= 0}>{saving ? 'Guardando...' : 'Registrar'}</button>
        </div>
      </div>
    </div>
  );
}
