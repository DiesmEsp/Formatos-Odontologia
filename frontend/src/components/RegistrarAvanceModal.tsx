import { useState, useRef } from 'react';
import { X } from 'lucide-react';
import { useApi } from '../hooks/useApi';
import { useToast } from '../hooks/useToast';
import { api } from '../api';
import { MaterialTable, type MaterialRow } from './MaterialTable';
import { formatMonto, hoyISO } from '../lib/format';
import type { Tratamiento } from '../api/types';

export function RegistrarAvanceModal({
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
  const [fecha, setFecha] = useState(hoyISO());
  const [pagoStr, setPagoStr] = useState('');
  const [saving, setSaving] = useState(false);
  const [materialRows, setMaterialRows] = useState<MaterialRow[]>([]);
  const materialRowsRef = useRef<MaterialRow[]>([]);
  const materiales = useApi(() => api.catalogos.materiales.listar());

  const handleAddRow = () => {
    setMaterialRows((prev) => {
      const next = [...prev, { key: `new-${Date.now()}`, materialId: null, nombreMaterial: '', cantidad: 0 }];
      materialRowsRef.current = next;
      return next;
    });
  };

  const handleRemoveRow = (key: string) => {
    setMaterialRows((prev) => {
      const next = prev.filter((r) => r.key !== key);
      materialRowsRef.current = next;
      return next;
    });
  };

  const handleMaterialChange = (key: string, materialId: number) => {
    setMaterialRows((prev) => {
      const next = prev.map((r) => r.key === key ? { ...r, materialId } : r);
      materialRowsRef.current = next;
      return next;
    });
  };

  const handleCantidadChange = (key: string, cantidad: number) => {
    setMaterialRows((prev) => {
      const next = prev.map((r) => r.key === key ? { ...r, cantidad } : r);
      materialRowsRef.current = next;
      return next;
    });
  };

  const filterNumeric = (val: string) => {
    let cleaned = val.replace(/[^0-9.]/g, '');
    const parts = cleaned.split('.');
    if (parts.length > 2) cleaned = parts[0] + '.' + parts.slice(1).join('');
    if (parts[1] && parts[1].length > 2) cleaned = parts[0] + '.' + parts[1].slice(0, 2);
    return cleaned;
  };

  const handleGuardar = async () => {
    const materialesMap: Record<string, number> = {};
    for (const row of materialRowsRef.current) {
      if (row.materialId != null && row.cantidad > 0) {
        materialesMap[String(row.materialId)] = row.cantidad;
      }
    }
    const pago = pagoStr === '' ? null : Number(pagoStr);
    setSaving(true);
    try {
      await api.tratamientos.agregarAvance(tratamiento.tratamientoID, {
        fecha,
        pago,
        materiales: Object.keys(materialesMap).length > 0 ? materialesMap : undefined,
      });
      addToast('success', 'Avance registrado');
      onSuccess();
    } catch (err) {
      addToast('error', err instanceof Error ? err.message : 'Error al registrar avance');
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="dialog-overlay" onClick={onClose}>
      <div className="dialog-pane mw-560" role="dialog" aria-modal="true" aria-labelledby="registrar-avance-title" onClick={(e) => e.stopPropagation()}>
        <div className="dialog-header">
          <h3 className="dialog-title" id="registrar-avance-title">Registrar Avance</h3>
          <button className="btn btn-ghost btn-sm" onClick={onClose}><X size={18} /></button>
        </div>
        <div className="dialog-body">
          <div className="sv-grid mb-16">
            <div className="sv-row"><span className="sv-label">Tratamiento</span><span>#{tratamiento.tratamientoID} - {tratamiento.nombreTratamiento}</span></div>
            <div className="sv-row"><span className="sv-label">Saldo pendiente</span><span className="num">{formatMonto(tratamiento.monto - tratamiento.montoPagado)}</span></div>
          </div>
          <div className="form-group"><label className="form-label">Fecha</label><input type="date" className="text-field w-full" value={fecha} onChange={(e) => setFecha(e.target.value)} /></div>
          <div className="form-group">
            <label className="form-label">Pago al tratamiento (opcional)</label>
            <input type="text" inputMode="decimal" className="text-field w-full" value={pagoStr} onChange={(e) => setPagoStr(filterNumeric(e.target.value))} placeholder="0.00" />
          </div>
          <div className="form-group">
            <label className="form-label">Materiales</label>
            <MaterialTable rows={materialRows} materials={materiales.data ?? []} onAdd={handleAddRow} onRemove={handleRemoveRow} onMaterialChange={handleMaterialChange} onCantidadChange={handleCantidadChange} />
          </div>
        </div>
        <div className="dialog-footer">
          <button className="btn btn-secondary" onClick={onClose}>Cancelar</button>
          <button className="btn btn-primary" onClick={handleGuardar} disabled={saving}>{saving ? 'Guardando...' : 'Registrar Avance'}</button>
        </div>
      </div>
    </div>
  );
}
