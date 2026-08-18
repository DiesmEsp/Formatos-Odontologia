import { useState, useRef } from 'react';
import { X } from 'lucide-react';
import { useApi } from '../../hooks/useApi';
import { useToast } from '../../hooks/useToast';
import { api } from '../../api';
import { SearchableCombo, type SearchableOption } from '../../components/SearchableCombo';
import { MaterialTable, type MaterialRow } from '../../components/MaterialTable';
import { CrearPacienteOnTheFly } from './CrearPacienteOnTheFly';
import { CrearOperadorOnTheFly } from './CrearOperadorOnTheFly';
import { hoyISO, nombreCompleto } from '../../lib/format';
import type { Tratamiento, Unidad } from '../../api/types';

export function CrearTratamientoModal({
  unidad, unidadesList, tratamientoPadre, onClose, onSuccess, addToast,
}: {
  unidad: Unidad | null;
  unidadesList: Unidad[];
  tratamientoPadre?: Tratamiento | null;
  onClose: () => void;
  onSuccess: () => void;
  addToast: ReturnType<typeof useToast>['addToast'];
}) {
  const [fecha, setFecha] = useState(hoyISO());
  const [pacienteId, setPacienteId] = useState<number | null>(tratamientoPadre?.pacienteID ?? null);
  const [operadorId, setOperadorId] = useState<number | null>(tratamientoPadre?.operadorID ?? null);
  const [tratPredId, setTratPredId] = useState<number | null>(null);
  const [unidadId, setUnidadId] = useState<number | null>(unidad?.unidadID ?? tratamientoPadre?.unidadID ?? null);
  const [montoStr, setMontoStr] = useState<string>('');
  const [tipo, setTipo] = useState<string>(tratamientoPadre ? 'CONTINUO' : 'NORMAL');
  const [saving, setSaving] = useState(false);
  const [qPac, setQPac] = useState('');
  const [qOpe, setQOpe] = useState('');
  const [qTrat, setQTrat] = useState('');
  const [showNewPaciente, setShowNewPaciente] = useState(false);
  const [showNewOperador, setShowNewOperador] = useState(false);
  const [materialRows, setMaterialRows] = useState<MaterialRow[]>([]);
  const materialRowsRef = useRef<MaterialRow[]>([]);

  const pacientes = useApi(() => api.catalogos.pacientes.listar(qPac || undefined), [qPac]);
  const operadores = useApi(() => api.catalogos.operadores.listar(qOpe || undefined), [qOpe]);
  const tratsPred = useApi(() => api.catalogos.tratamientosPred.listar(qTrat || undefined), [qTrat]);
  const materiales = useApi(() => api.catalogos.materiales.listar());

  const pOptions: SearchableOption[] = (pacientes.data ?? []).map((p) => ({ id: p.pacienteID, label: nombreCompleto(p.nombres, p.apellidos) }));
  const oOptions: SearchableOption[] = (operadores.data ?? []).map((o) => ({ id: o.operadorID, label: nombreCompleto(o.nombres, o.apellidos), badge: o.grado }));
  const tOptions: SearchableOption[] = (tratsPred.data ?? []).map((t) => ({ id: t.tratPredID, label: t.nombreTratamiento, extra: t.montoSugerido != null ? `S/ ${t.montoSugerido.toFixed(2)}` : undefined }));

  const handlePacienteChange = (id: number | null) => {
    setPacienteId(id);
  };

  const handleTratChange = async (id: number | null) => {
    setTratPredId(id);
    if (id) {
      const tp = tratsPred.data?.find((t) => t.tratPredID === id);
      if (tp?.montoSugerido != null && tipo !== 'CONTINUO') setMontoStr(String(tp.montoSugerido));
      try {
        const mats = await api.catalogos.tratamientosPred.materiales(id);
        const rows: MaterialRow[] = mats.map((m, i) => ({
          key: `new-${Date.now()}-${i}`,
          materialId: m.materialID,
          nombreMaterial: m.nombreMaterial,
          cantidad: m.cantidad,
        }));
        setMaterialRows(rows);
        materialRowsRef.current = rows;
      } catch {
        setMaterialRows([]);
        materialRowsRef.current = [];
      }
    } else {
      setMaterialRows([]);
      materialRowsRef.current = [];
    }
  };

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

  const handleTipoChange = (nuevoTipo: string) => {
    setTipo(nuevoTipo);
    if (nuevoTipo === 'CONTINUO') setMontoStr('');
  };

  const filterNumeric = (val: string) => {
    let cleaned = val.replace(/[^0-9.]/g, '');
    const parts = cleaned.split('.');
    if (parts.length > 2) cleaned = parts[0] + '.' + parts.slice(1).join('');
    if (parts[1] && parts[1].length > 2) cleaned = parts[0] + '.' + parts[1].slice(0, 2);
    return cleaned;
  };

  const handleNumericKeyDown = (e: React.KeyboardEvent) => {
    const allowed = ['Backspace','Delete','Tab','Escape','ArrowLeft','ArrowRight','ArrowUp','ArrowDown','Home','End'];
    if (allowed.includes(e.key)) return;
    if (e.ctrlKey || e.metaKey) return;
    if (e.key === '.' && !montoStr.includes('.')) return;
    if (e.key >= '0' && e.key <= '9') return;
    e.preventDefault();
  };

  const handleMontoKeyDown = (e: React.KeyboardEvent) => {
    handleNumericKeyDown(e);
    if (e.key === 'Enter') {
      e.preventDefault();
      if (montoStr === '' || montoStr.trim() === '') setMontoStr('0');
      (e.target as HTMLInputElement).blur();
    }
  };

  const handleGuardar = async () => {
    if (!operadorId || !pacienteId) { addToast('error', 'Seleccione paciente y operador'); return; }
    const montoVal = montoStr === '' ? null : Number(montoStr);
    const materialesMap: Record<string, number> = {};
    for (const row of materialRowsRef.current) {
      if (row.materialId != null && row.cantidad > 0) {
        materialesMap[String(row.materialId)] = row.cantidad;
      }
    }
    setSaving(true);
    try {
      await api.tratamientos.crear({
        operadorID: operadorId, pacienteID: pacienteId, unidadID: unidadId, fecha,
        tratPredID: tratPredId, monto: tipo === 'CONTINUO' ? null : montoVal, tipo,
        tratamientoPadreID: tratamientoPadre?.tratamientoID ?? null,
        materiales: Object.keys(materialesMap).length > 0 ? materialesMap : undefined,
      });
      addToast('success', 'Tratamiento creado correctamente');
      onSuccess();
    } catch (err) { addToast('error', err instanceof Error ? err.message : 'Error al crear tratamiento'); }
    finally { setSaving(false); }
  };

  return (
    <>
      <div className="dialog-overlay" onClick={onClose}>
        <div className="dialog-pane mw-560" onClick={(e) => e.stopPropagation()}>
          <div className="dialog-header">
            <h3 className="dialog-title">{tratamientoPadre ? 'Crear Tratamiento Continuo' : 'Crear Tratamiento'}</h3>
            <button className="btn btn-ghost btn-sm" onClick={onClose}><X size={18} /></button>
          </div>
          <div className="dialog-body">
            {tratamientoPadre && (
              <div className="alert-banner alert-info mb-16">
                <span>Continuación del tratamiento #{tratamientoPadre.tratamientoID} ({tratamientoPadre.nombreTratamiento})</span>
              </div>
            )}
            {unidad ? (
              <div className="form-group"><label className="form-label">Unidad</label><input className="text-field w-full" value={`Unidad ${unidad.unidadNro}`} readOnly /></div>
            ) : (
              <div className="form-group">
                <label className="form-label">Unidad (opcional)</label>
                <select className="combo-box w-full" value={unidadId ?? ''} onChange={(e) => setUnidadId(e.target.value === '' ? null : Number(e.target.value))}>
                  <option value="">Sin unidad</option>
                  {unidadesList.map((u) => <option key={u.unidadID} value={u.unidadID}>Unidad {u.unidadNro}</option>)}
                </select>
              </div>
            )}
            <div className="form-group"><label className="form-label">Fecha</label><input type="date" className="text-field w-full" value={fecha} onChange={(e) => setFecha(e.target.value)} /></div>

            <div className="form-group">
              <label className="form-label">Paciente</label>
              <SearchableCombo options={pOptions} value={pacienteId} onChange={handlePacienteChange} onSearch={setQPac} placeholder="Buscar paciente..." />
              <button className="btn btn-ghost btn-sm btn-inline-add" onClick={() => setShowNewPaciente(true)}>+ Nuevo paciente</button>
            </div>

            <div className="form-group">
              <label className="form-label">Operador</label>
              <SearchableCombo options={oOptions} value={operadorId} onChange={setOperadorId} onSearch={setQOpe} placeholder="Buscar operador..." />
              <button className="btn btn-ghost btn-sm btn-inline-add" onClick={() => setShowNewOperador(true)}>+ Nuevo operador</button>
            </div>

            <div className="form-group"><label className="form-label">Tipo de tratamiento</label><SearchableCombo options={tOptions} value={tratPredId} onChange={handleTratChange} onSearch={setQTrat} placeholder="Buscar tratamiento..." /></div>

            <div className="form-group">
              <label className="form-label">Monto total</label>
              <input type="text" inputMode="decimal" className="text-field w-full" value={montoStr} onChange={(e) => setMontoStr(filterNumeric(e.target.value))}
                onKeyDown={handleMontoKeyDown}
                disabled={tipo === 'CONTINUO'} placeholder="0.00" />
            </div>

            <div className="form-group">
              <label className="form-label">Tipo</label>
              <div className="flex gap-8">
                {(['NORMAL', 'CONTINUO'] as const).map((t) => (
                  <button key={t} type="button"
                    className={`btn ${tipo === t ? 'btn-primary' : 'btn-secondary'}`}
                    onClick={() => handleTipoChange(t)}
                    disabled={!!tratamientoPadre}>
                    {t === 'NORMAL' ? 'Común' : 'Continuo'}
                  </button>
                ))}
              </div>
              {tratamientoPadre && <span className="text-muted text-sm">Tipo fijado en Continuo para la continuación del tratamiento.</span>}
            </div>

            <div className="form-group">
              <label className="form-label">Materiales</label>
              <MaterialTable rows={materialRows} materials={materiales.data ?? []} onAdd={handleAddRow} onRemove={handleRemoveRow} onMaterialChange={handleMaterialChange} onCantidadChange={handleCantidadChange} />
            </div>
          </div>
          <div className="dialog-footer">
            <button className="btn btn-secondary" onClick={onClose}>Cancelar</button>
            <button className="btn btn-primary" onClick={handleGuardar} disabled={saving}>{saving ? 'Guardando...' : 'Crear Tratamiento'}</button>
          </div>
        </div>
      </div>

      {showNewPaciente && <CrearPacienteOnTheFly onClose={() => setShowNewPaciente(false)} onCreated={(id) => { handlePacienteChange(id); setShowNewPaciente(false); pacientes.refetch(); }} addToast={addToast} />}
      {showNewOperador && <CrearOperadorOnTheFly onClose={() => setShowNewOperador(false)} onCreated={(id) => { setOperadorId(id); setShowNewOperador(false); operadores.refetch(); }} addToast={addToast} />}
    </>
  );
}
