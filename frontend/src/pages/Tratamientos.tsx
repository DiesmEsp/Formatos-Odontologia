import { useState, useEffect, useRef, useCallback } from 'react';
import { useApi } from '../hooks/useApi';
import { useToast } from '../hooks/useToast';
import { api } from '../api';
import { StationCard } from '../components/StationCard';
import { ConfirmDialog } from '../components/ConfirmDialog';
import { Badge } from '../components/Badge';
import { SearchableCombo, type SearchableOption } from '../components/SearchableCombo';
import { MaterialTable, type MaterialRow } from '../components/MaterialTable';
import { X, DollarSign, RotateCcw, AlertTriangle, ArrowLeftRight, Plus } from 'lucide-react';
import { formatMonto, hoyISO, nombreCompleto } from '../lib/format';
import type { Tratamiento, Unidad } from '../api/types';

const TIPOS_PRE = ['3', '4', '5'];
const TIPOS_POS = ['R1', 'R2', 'R3'];

export default function Tratamientos() {
  const unidades = useApi(() => api.unidades.listar());
  const tratamientos = useApi(() => api.tratamientos.activos());
  const operadores = useApi(() => api.catalogos.operadores.listar());
  const pacientes = useApi(() => api.catalogos.pacientes.listar());
  const { addToast } = useToast();

  const [crearUnidad, setCrearUnidad] = useState<Unidad | null>(null);
  const [crearManual, setCrearManual] = useState(false);
  const [detalleTratamiento, setDetalleTratamiento] = useState<Tratamiento | null>(null);

  const tratamientosActivos = tratamientos.data ?? [];
  const unidadesList = unidades.data ?? [];

  const operadorNombreMap = new Map((operadores.data ?? []).map((o) => [o.operadorID, nombreCompleto(o.nombres, o.apellidos)]));
  const pacienteNombreMap = new Map((pacientes.data ?? []).map((p) => [p.pacienteID, nombreCompleto(p.nombres, p.apellidos)]));

  const getTratamientoEnUnidad = (unidadId: number) => {
    return tratamientosActivos.find((t) => t.unidadID === unidadId && t.estado !== 'CERRADO' && t.estado !== 'ANULADO') ?? null;
  };

  return (
    <div>
      <div className="view-header">
        <div>
          <h1 className="view-title">Tratamientos en Curso</h1>
          <p className="subtitle">Seleccione una unidad libre para iniciar un nuevo tratamiento</p>
        </div>
        <button className="btn btn-primary" onClick={() => setCrearManual(true)}>
          <Plus size={16} /> Nuevo tratamiento (manual)
        </button>
      </div>

      <div className="station-grid">
        {unidadesList.map((u) => {
          const t = getTratamientoEnUnidad(u.unidadID);
          return (
            <StationCard
              key={u.unidadID}
              unidadNro={u.unidadNro}
              tratamiento={t}
              operadorNombre={t ? operadorNombreMap.get(t.operadorID) : undefined}
              pacienteNombre={t ? pacienteNombreMap.get(t.pacienteID) : undefined}
              onClick={() => {
                if (t) setDetalleTratamiento(t);
                else setCrearUnidad(u);
              }}
            />
          );
        })}
        {unidadesList.length === 0 && (
          <div className="empty-state" style={{ gridColumn: '1 / -1' }}>
            <span className="empty-title">No hay unidades registradas</span>
            <span className="empty-text">Cree unidades en la sección de Gestión para comenzar.</span>
          </div>
        )}
      </div>

      {crearUnidad && (
        <CrearTratamientoModal
          unidad={crearUnidad}
          unidadesList={unidadesList}
          onClose={() => setCrearUnidad(null)}
          onSuccess={() => { setCrearUnidad(null); tratamientos.refetch(); operadores.refetch(); pacientes.refetch(); }}
          addToast={addToast}
        />
      )}

      {crearManual && (
        <CrearTratamientoModal
          unidad={null}
          unidadesList={unidadesList}
          onClose={() => setCrearManual(false)}
          onSuccess={() => { setCrearManual(false); tratamientos.refetch(); operadores.refetch(); pacientes.refetch(); }}
          addToast={addToast}
        />
      )}

      {detalleTratamiento && (
        <DetalleTratamientoSubventana
          tratamiento={detalleTratamiento}
          operadorNombre={operadorNombreMap.get(detalleTratamiento.operadorID)}
          pacienteNombre={pacienteNombreMap.get(detalleTratamiento.pacienteID)}
          onClose={() => { setDetalleTratamiento(null); tratamientos.refetch(); }}
          addToast={addToast}
        />
      )}
    </div>
  );
}

function CrearTratamientoModal({
  unidad, unidadesList, onClose, onSuccess, addToast,
}: { unidad: Unidad | null; unidadesList: Unidad[]; onClose: () => void; onSuccess: () => void; addToast: ReturnType<typeof useToast>['addToast'] }) {
  const [fecha, setFecha] = useState(hoyISO());
  const [pacienteId, setPacienteId] = useState<number | null>(null);
  const [operadorId, setOperadorId] = useState<number | null>(null);
  const [tratPredId, setTratPredId] = useState<number | null>(null);
  const [unidadId, setUnidadId] = useState<number | null>(unidad?.unidadID ?? null);
  const [montoStr, setMontoStr] = useState<string>('');
  const [tipo, setTipo] = useState<string>('NORMAL');
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
            <h3 className="dialog-title">Crear Tratamiento</h3>
            <button className="btn btn-ghost btn-sm" onClick={onClose}><X size={18} /></button>
          </div>
          <div className="dialog-body">
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
              <SearchableCombo options={pOptions} value={pacienteId} onChange={setPacienteId} onSearch={setQPac} placeholder="Buscar paciente..." />
              <button className="btn btn-ghost btn-sm" onClick={() => setShowNewPaciente(true)} style={{ alignSelf: 'flex-start', marginTop: 2, fontSize: 'var(--font-sm)' }}>+ Nuevo paciente</button>
            </div>

            <div className="form-group">
              <label className="form-label">Operador</label>
              <SearchableCombo options={oOptions} value={operadorId} onChange={setOperadorId} onSearch={setQOpe} placeholder="Buscar operador..." />
              <button className="btn btn-ghost btn-sm" onClick={() => setShowNewOperador(true)} style={{ alignSelf: 'flex-start', marginTop: 2, fontSize: 'var(--font-sm)' }}>+ Nuevo operador</button>
            </div>

            <div className="form-group"><label className="form-label">Tipo de tratamiento</label><SearchableCombo options={tOptions} value={tratPredId} onChange={handleTratChange} onSearch={setQTrat} placeholder="Buscar tratamiento..." /></div>

            <div className="form-group">
              <label className="form-label">Monto total</label>
              <input type="text" inputMode="decimal" className="text-field" value={montoStr} onChange={(e) => setMontoStr(filterNumeric(e.target.value))}
                onKeyDown={handleMontoKeyDown}
                disabled={tipo === 'CONTINUO'} placeholder="0.00"
                style={{ width: '100%', ...(tipo === 'CONTINUO' ? { backgroundColor: '#e8ecec', color: 'var(--color-text-muted)', cursor: 'not-allowed' } : {}) }} />
            </div>

            <div className="form-group">
              <label className="form-label">Tipo</label>
              <select className="combo-box w-full" value={tipo} onChange={(e) => handleTipoChange(e.target.value)}>
                <option value="NORMAL">Normal</option>
                <option value="CONTINUO">Continuo</option>
              </select>
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

      {showNewPaciente && <CrearPacienteOnTheFly onClose={() => setShowNewPaciente(false)} onCreated={(id) => { setPacienteId(id); setShowNewPaciente(false); pacientes.refetch(); }} addToast={addToast} />}
      {showNewOperador && <CrearOperadorOnTheFly onClose={() => setShowNewOperador(false)} onCreated={(id) => { setOperadorId(id); setShowNewOperador(false); operadores.refetch(); }} addToast={addToast} />}
    </>
  );
}

function CrearPacienteOnTheFly({ onClose, onCreated, addToast }: { onClose: () => void; onCreated: (id: number) => void; addToast: ReturnType<typeof useToast>['addToast'] }) {
  const [nombres, setNombres] = useState('');
  const [apellidos, setApellidos] = useState('');
  const [saving, setSaving] = useState(false);

  const handleSave = async () => {
    if (!nombres.trim() || !apellidos.trim()) { addToast('error', 'Complete nombre y apellido'); return; }
    setSaving(true);
    try {
      const result = await api.catalogos.pacientes.crear({ nombres: nombres.trim(), apellidos: apellidos.trim() });
      addToast('success', 'Paciente creado');
      onCreated(result.id);
    } catch (err) { addToast('error', err instanceof Error ? err.message : 'Error al crear'); }
    finally { setSaving(false); }
  };

  return (
    <div className="dialog-overlay" onClick={onClose} style={{ zIndex: 210 }}>
      <div className="dialog-pane mw-420" onClick={(e) => e.stopPropagation()}>
        <div className="dialog-header"><h3 className="dialog-title">Nuevo Paciente</h3><button className="btn btn-ghost btn-sm" onClick={onClose}><X size={18} /></button></div>
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

function CrearOperadorOnTheFly({ onClose, onCreated, addToast }: { onClose: () => void; onCreated: (id: number) => void; addToast: ReturnType<typeof useToast>['addToast'] }) {
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
    <div className="dialog-overlay" onClick={onClose} style={{ zIndex: 210 }}>
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

function DetalleTratamientoSubventana({
  tratamiento: initialTrat, operadorNombre, pacienteNombre, onClose, addToast,
}: { tratamiento: Tratamiento; operadorNombre?: string; pacienteNombre?: string; onClose: () => void; addToast: ReturnType<typeof useToast>['addToast'] }) {
  const [tratamiento, setTratamiento] = useState<Tratamiento>(initialTrat);
  const [materialRows, setMaterialRows] = useState<MaterialRow[]>([]);
  const [showPago, setShowPago] = useState(false);
  const [showAnular, setShowAnular] = useState(false);
  const [abono, setAbono] = useState(0);
  const [saving, setSaving] = useState(false);
  const [savingMat, setSavingMat] = useState(false);
  const [dirty, setDirty] = useState(false);
  const materiales = useApi(() => api.catalogos.materiales.listar());
  const mounted = useRef(true);
  const originalRowsRef = useRef<MaterialRow[]>([]);
  const materialRowsRef = useRef<MaterialRow[]>([]);

  const cargarDatos = useCallback(async () => {
    try {
      const t = await api.tratamientos.buscarPorId(initialTrat.tratamientoID);
      if (!mounted.current) return;
      setTratamiento(t);
      const mats = await api.tratamientos.materialesConNombre(t.tratamientoID);
      if (!mounted.current) return;
      const rows: MaterialRow[] = mats.map((m) => ({
        key: `mat-${m.materialesListID}`,
        materialId: m.materialID,
        nombreMaterial: m.nombreMaterial,
        cantidad: m.cantidad,
      }));
      setMaterialRows(rows);
      materialRowsRef.current = rows;
      originalRowsRef.current = rows.map((r) => ({ ...r }));
      setDirty(false);
    } catch {
      if (mounted.current) addToast('error', 'Error al cargar los datos del tratamiento');
    }
  }, [initialTrat.tratamientoID, addToast]);

  useEffect(() => { mounted.current = true; cargarDatos(); return () => { mounted.current = false; }; }, [cargarDatos]);

  const handleAddRow = () => {
    setMaterialRows((prev) => {
      const next = [...prev, { key: `new-${Date.now()}`, materialId: null, nombreMaterial: '', cantidad: 0 }];
      materialRowsRef.current = next;
      return next;
    });
    setDirty(true);
  };

  const handleRemoveRow = (key: string) => {
    setMaterialRows((prev) => {
      const next = prev.filter((r) => r.key !== key);
      materialRowsRef.current = next;
      return next;
    });
    setDirty(true);
  };

  const handleMaterialChange = (key: string, materialId: number) => {
    setMaterialRows((prev) => {
      const next = prev.map((r) => r.key === key ? { ...r, materialId } : r);
      materialRowsRef.current = next;
      return next;
    });
    setDirty(true);
  };

  const handleCantidadChange = (key: string, cantidad: number) => {
    setMaterialRows((prev) => {
      const next = prev.map((r) => r.key === key ? { ...r, cantidad } : r);
      materialRowsRef.current = next;
      return next;
    });
    setDirty(true);
  };

  const handleSaveMaterials = async () => {
    const rows = materialRowsRef.current;
    const original = originalRowsRef.current;
    if (rows.length === 0 && original.length === 0) return;

    setSavingMat(true);
    try {
      const removedKeys = original.filter((or) => !rows.some((r) => r.key === or.key));

      for (const row of removedKeys) {
        const id = Number(row.key.replace('mat-', ''));
        await api.tratamientos.quitarMaterial(id);
      }

      for (const row of rows) {
        if (row.materialId == null) continue;

        if (row.key.startsWith('new-')) {
          if (row.cantidad > 0 || row.materialId != null) {
            await api.tratamientos.agregarMaterial(tratamiento.tratamientoID, {
              materialId: row.materialId,
              cantidad: row.cantidad > 0 ? row.cantidad : 1,
            });
          }
        } else {
          const orig = original.find((or) => or.key === row.key);
          const id = Number(row.key.replace('mat-', ''));

          if (orig && orig.materialId !== row.materialId) {
            await api.tratamientos.quitarMaterial(id);
            await api.tratamientos.agregarMaterial(tratamiento.tratamientoID, {
              materialId: row.materialId,
              cantidad: row.cantidad > 0 ? row.cantidad : 1,
            });
          } else if (orig && orig.cantidad !== row.cantidad) {
            await api.tratamientos.actualizarCantidad(id, row.cantidad);
          }
        }
      }

      addToast('success', 'Materiales guardados correctamente');
      await cargarDatos();
      setDirty(false);
    } catch (err) {
      addToast('error', err instanceof Error ? err.message : 'Error al guardar materiales');
    } finally {
      setSavingMat(false);
    }
  };

  const handleClose = async () => {
    if (dirty) {
      setSavingMat(true);
      try {
        const rows = materialRowsRef.current;
        const original = originalRowsRef.current;
        const removedKeys = original.filter((or) => !rows.some((r) => r.key === or.key));

        for (const row of removedKeys) {
          const id = Number(row.key.replace('mat-', ''));
          await api.tratamientos.quitarMaterial(id);
        }

        for (const row of rows) {
          if (row.materialId == null) continue;

          if (row.key.startsWith('new-')) {
            if (row.cantidad > 0 || row.materialId != null) {
              await api.tratamientos.agregarMaterial(tratamiento.tratamientoID, {
                materialId: row.materialId,
                cantidad: row.cantidad > 0 ? row.cantidad : 1,
              });
            }
          } else {
            const orig = original.find((or) => or.key === row.key);
            const id = Number(row.key.replace('mat-', ''));

            if (orig && orig.materialId !== row.materialId) {
              await api.tratamientos.quitarMaterial(id);
              await api.tratamientos.agregarMaterial(tratamiento.tratamientoID, {
                materialId: row.materialId,
                cantidad: row.cantidad > 0 ? row.cantidad : 1,
              });
            } else if (orig && orig.cantidad !== row.cantidad) {
              await api.tratamientos.actualizarCantidad(id, row.cantidad);
            }
          }
        }

        addToast('success', 'Materiales guardados automáticamente');
      } catch (err) {
        addToast('error', err instanceof Error ? err.message : 'Error al guardar materiales');
      } finally {
        setSavingMat(false);
      }
    }
    onClose();
  };

  const handleCerrar = async () => { setSaving(true); try { await api.tratamientos.cerrar(tratamiento.tratamientoID); addToast('success', 'Tratamiento cerrado'); onClose(); } catch (err) { addToast('error', err instanceof Error ? err.message : 'Error al cerrar'); } finally { setSaving(false); } };
  const handleAnular = async (motivo?: string) => { if (!motivo) return; try { await api.tratamientos.anular(tratamiento.tratamientoID, motivo); addToast('success', 'Tratamiento anulado'); onClose(); } catch (err) { addToast('error', err instanceof Error ? err.message : 'Error al anular'); } setShowAnular(false); };
  const handleReabrir = async () => { try { await api.tratamientos.reabrir(tratamiento.tratamientoID); const t = await api.tratamientos.buscarPorId(tratamiento.tratamientoID); setTratamiento(t); addToast('success', 'Tratamiento reabierto'); } catch (err) { addToast('error', err instanceof Error ? err.message : 'Error al reabrir'); } };
  const handleRegistrarPago = async () => { if (abono <= 0) return; try { await api.tratamientos.registrarPago(tratamiento.tratamientoID, abono); const t = await api.tratamientos.buscarPorId(tratamiento.tratamientoID); setTratamiento(t); addToast('success', 'Pago registrado'); setShowPago(false); setAbono(0); } catch (err) { addToast('error', err instanceof Error ? err.message : 'Error al registrar pago'); } };
  const handleCambiarTipo = async () => { const nuevo = tratamiento.tipo === 'NORMAL' ? 'CONTINUO' : 'NORMAL'; try { await api.tratamientos.cambiarTipo(tratamiento.tratamientoID, nuevo); const t = await api.tratamientos.buscarPorId(tratamiento.tratamientoID); setTratamiento(t); addToast('success', `Tipo cambiado a ${nuevo}`); } catch (err) { addToast('error', err instanceof Error ? err.message : 'Error al cambiar tipo'); } };

  const getEstadoVariant = (e: string) => e === 'CERRADO' ? 'success' : e === 'ANULADO' ? 'danger' : 'info';
  const saldo = tratamiento.monto - tratamiento.montoPagado;
  const totalMat = materialRows.filter((r) => r.materialId != null).length;

  return (
    <>
      <div className="subventana-overlay">
        <div className="subventana">
          <div className="subventana-header">
            <div><h3 className="dialog-title">Detalle de Tratamiento #{tratamiento.tratamientoID}</h3></div>
            <div className="flex flex-center gap-8">
              <Badge variant={getEstadoVariant(tratamiento.estado)}>{tratamiento.estado}</Badge>
              <button className="btn btn-ghost btn-sm" onClick={handleClose}><X size={18} /></button>
            </div>
          </div>
          <div className="subventana-body">
            <div className="sv-grid">
              <div className="sv-row"><span className="sv-label">Operador</span><span>{operadorNombre ?? `#${tratamiento.operadorID}`}</span></div>
              <div className="sv-row"><span className="sv-label">Paciente</span><span>{pacienteNombre ?? `#${tratamiento.pacienteID}`}</span></div>
              <div className="sv-row"><span className="sv-label">Fecha</span><span>{tratamiento.fecha}</span></div>
              <div className="sv-row"><span className="sv-label">Monto total</span><span className="num">{formatMonto(tratamiento.monto)}</span></div>
              <div className="sv-row"><span className="sv-label">Monto pagado</span><span className="num">{formatMonto(tratamiento.montoPagado)}</span></div>
              <div className="sv-row"><span className="sv-label">Tipo</span><Badge variant="info">{tratamiento.tipo}</Badge></div>
              <div className="sv-row"><span className="sv-label">Pago</span><Badge variant={tratamiento.estadoPago === 'PAGADO' ? 'success' : tratamiento.estadoPago === 'PARCIAL' ? 'warning' : 'neutral'}>{tratamiento.estadoPago}</Badge></div>
            </div>
            {saldo > 0 && tratamiento.estado === 'ABIERTO' && (
              <div className="alert-banner alert-warning mt-16"><AlertTriangle size={16} /><span>Saldo pendiente: {formatMonto(saldo)}</span></div>
            )}
            <h4 style={{ marginTop: 20, marginBottom: 12 }}>Materiales del tratamiento</h4>
            <MaterialTable rows={materialRows} materials={materiales.data ?? []} onAdd={handleAddRow} onRemove={handleRemoveRow} onMaterialChange={handleMaterialChange} onCantidadChange={handleCantidadChange} />
          </div>
          <div className="subventana-footer">
            <span className="text-muted text-sm">
              {totalMat} material(es){dirty && <span style={{ color: 'var(--color-warning-text)', marginLeft: 8 }}>(cambios sin guardar)</span>}
            </span>
            <div className="flex gap-8 flex-wrap">
              {dirty && (
                <button className="btn btn-primary" onClick={handleSaveMaterials} disabled={savingMat}>
                  {savingMat ? 'Guardando...' : 'Guardar materiales'}
                </button>
              )}
              {tratamiento.estado === 'ABIERTO' && (
                <>
                  <button className="btn btn-success" onClick={handleCerrar} disabled={saving}>Cerrar tratamiento</button>
                  <button className="btn btn-primary" onClick={() => setShowPago(true)}><DollarSign size={14} /> Registrar pago</button>
                  <button className="btn btn-secondary" onClick={handleCambiarTipo}><ArrowLeftRight size={14} /> {tratamiento.tipo === 'NORMAL' ? 'CONTINUO' : 'NORMAL'}</button>
                  <button className="btn btn-danger" onClick={() => setShowAnular(true)}><AlertTriangle size={14} /> Anular</button>
                </>
              )}
              {tratamiento.estado === 'CERRADO' && (
                <>
                  <button className="btn btn-secondary" onClick={handleReabrir}><RotateCcw size={14} /> Reabrir</button>
                  <button className="btn btn-primary" onClick={() => setShowPago(true)}><DollarSign size={14} /> Registrar pago</button>
                  <button className="btn btn-danger" onClick={() => setShowAnular(true)}><AlertTriangle size={14} /> Anular</button>
                </>
              )}
            </div>
          </div>
        </div>
      </div>
      {showPago && (
        <div className="dialog-overlay" onClick={() => setShowPago(false)} style={{ zIndex: 210 }}>
          <div className="dialog-pane mw-420" onClick={(e) => e.stopPropagation()}>
            <div className="dialog-header"><h3 className="dialog-title">Registrar Pago</h3><button className="btn btn-ghost btn-sm" onClick={() => setShowPago(false)}><X size={18} /></button></div>
            <div className="dialog-body">
              <div className="sv-grid mb-16">
                <div className="sv-row"><span className="sv-label">Monto total</span><span className="num">{formatMonto(tratamiento.monto)}</span></div>
                <div className="sv-row"><span className="sv-label">Pagado</span><span className="num">{formatMonto(tratamiento.montoPagado)}</span></div>
                <div className="sv-row"><span className="sv-label">Saldo</span><span className="num" style={{ color: saldo > 0 ? 'var(--color-danger-text)' : 'var(--color-success-text)' }}>{formatMonto(saldo)}</span></div>
              </div>
              <div className="form-group"><label className="form-label">Monto a abonar</label><input type="number" className="text-field w-full" value={abono} onChange={(e) => setAbono(Number(e.target.value))} min={0.01} step="0.01" /></div>
            </div>
            <div className="dialog-footer"><button className="btn btn-secondary" onClick={() => setShowPago(false)}>Cancelar</button><button className="btn btn-primary" onClick={handleRegistrarPago} disabled={abono <= 0}>Registrar</button></div>
          </div>
        </div>
      )}
      <ConfirmDialog open={showAnular} title="Anular tratamiento" message={`Confirme que desea anular el tratamiento #${tratamiento.tratamientoID}.`} confirmLabel="Si, anular" variant="danger" requireMotivo onConfirm={handleAnular} onCancel={() => setShowAnular(false)} />
    </>
  );
}
