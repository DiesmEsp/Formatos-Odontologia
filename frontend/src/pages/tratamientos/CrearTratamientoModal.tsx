import { useState, useRef, useEffect } from 'react';
import { X, GitBranch } from 'lucide-react';
import { useApi } from '../../hooks/useApi';
import { useToast } from '../../hooks/useToast';
import { useModalDraft } from '../../hooks/useModalDraft';
import { api } from '../../api';
import { SearchableCombo, type SearchableOption } from '../../components/SearchableCombo';
import { MaterialTable, type MaterialRow } from '../../components/MaterialTable';
import { Badge } from '../../components/Badge';
import { CrearPacienteOnTheFly } from './CrearPacienteOnTheFly';
import { CrearOperadorOnTheFly } from './CrearOperadorOnTheFly';
import { hoyISO, nombreCompleto, formatMonto } from '../../lib/format';
import type { Tratamiento, Unidad } from '../../api/types';

type TipoTratamiento = 'NORMAL' | 'CONTINUO' | 'AVANCE';

interface TratamientoDraft {
  fecha: string;
  pacienteId: number | null;
  operadorId: number | null;
  tratPredId: number | null;
  nombreTratamiento: string;
  unidadId: number | null;
  montoStr: string;
  pagoStr: string;
  tipo: TipoTratamiento;
  avanceTratamiento: Tratamiento | null;
  materialRows: MaterialRow[];
}

export function CrearTratamientoModal({
  unidad, unidadesList, unidadesOcupadas, tratamientoPadre, onClose, onSuccess, addToast,
}: {
  unidad: Unidad | null;
  unidadesList: Unidad[];
  unidadesOcupadas?: number[];
  tratamientoPadre?: Tratamiento | null;
  onClose: () => void;
  onSuccess: () => void;
  addToast: ReturnType<typeof useToast>['addToast'];
}) {
  const draftKey = tratamientoPadre
    ? `trat-padre-${tratamientoPadre.tratamientoID}`
    : unidad ? `trat-unidad-${unidad.unidadID}` : 'trat-manual';
  const { draft, saveDraft, clearDraft } = useModalDraft<TratamientoDraft>(draftKey);

  const [fecha, setFecha] = useState(draft?.fecha ?? hoyISO());
  const [pacienteId, setPacienteId] = useState<number | null>(draft?.pacienteId ?? tratamientoPadre?.pacienteID ?? null);
  const [operadorId, setOperadorId] = useState<number | null>(draft?.operadorId ?? tratamientoPadre?.operadorID ?? null);
  const [tratPredId, setTratPredId] = useState<number | null>(draft?.tratPredId ?? null);
  const [nombreTratamiento, setNombreTratamiento] = useState<string>(draft?.nombreTratamiento ?? '');
  const [unidadId, setUnidadId] = useState<number | null>(draft?.unidadId ?? unidad?.unidadID ?? tratamientoPadre?.unidadID ?? null);
  const [montoStr, setMontoStr] = useState<string>(draft?.montoStr ?? '');
  const [pagoStr, setPagoStr] = useState<string>(draft?.pagoStr ?? '');
  const [tipo, setTipo] = useState<TipoTratamiento>(draft?.tipo ?? (tratamientoPadre ? 'CONTINUO' : 'NORMAL'));
  const [avanceTratamiento, setAvanceTratamiento] = useState<Tratamiento | null>(draft?.avanceTratamiento ?? null);
  const [saving, setSaving] = useState(false);
  const [qPac, setQPac] = useState('');
  const [qOpe, setQOpe] = useState('');
  const [qTrat, setQTrat] = useState('');
  const [qAvance, setQAvance] = useState('');
  const [showNewPaciente, setShowNewPaciente] = useState(false);
  const [showNewOperador, setShowNewOperador] = useState(false);
  const [materialRows, setMaterialRows] = useState<MaterialRow[]>(draft?.materialRows ?? []);
  const materialRowsRef = useRef<MaterialRow[]>(draft?.materialRows ?? []);

  useEffect(() => {
    saveDraft({ fecha, pacienteId, operadorId, tratPredId, nombreTratamiento, unidadId, montoStr, pagoStr, tipo, avanceTratamiento, materialRows });
  }, [fecha, pacienteId, operadorId, tratPredId, nombreTratamiento, unidadId, montoStr, pagoStr, tipo, avanceTratamiento, materialRows, saveDraft]);

  const pacientes = useApi(() => api.catalogos.pacientes.listar(qPac || undefined), [qPac]);
  const operadores = useApi(() => api.catalogos.operadores.listar(qOpe || undefined), [qOpe]);
  const tratsPred = useApi(() => api.catalogos.tratamientosPred.listar(qTrat || undefined), [qTrat]);
  const materiales = useApi(() => api.catalogos.materiales.listar());
  const todosTratamientos = useApi(() => api.tratamientos.todos(), []);

  const pOptions: SearchableOption[] = (pacientes.data ?? []).map((p) => ({ id: p.pacienteID, label: nombreCompleto(p.nombres, p.apellidos) }));
  const oOptions: SearchableOption[] = (operadores.data ?? []).map((o) => ({ id: o.operadorID, label: nombreCompleto(o.nombres, o.apellidos), badge: o.grado }));
  const tOptions: SearchableOption[] = (tratsPred.data ?? []).map((t) => ({ id: t.tratPredID, label: t.nombreTratamiento, extra: t.montoSugerido != null ? `S/ ${t.montoSugerido.toFixed(2)}` : undefined }));
  const avanceOptions: SearchableOption[] = (todosTratamientos.data ?? [])
    .filter((t) => qAvance.trim() === ''
      || String(t.tratamientoID).includes(qAvance)
      || t.nombreTratamiento.toLowerCase().includes(qAvance.toLowerCase()))
    .map((t) => ({
      id: t.tratamientoID,
      label: `Tratatamiento #${t.tratamientoID} — ${t.nombreTratamiento}`,
      extra: `${t.estado} · S/ ${(t.monto - t.montoPagado).toFixed(2)}`,
    }));

  const unidadesLibres = (unidadesOcupadas?.length ?? 0) > 0
    ? unidadesList.filter((u) => !unidadesOcupadas!.includes(u.unidadID))
    : unidadesList;

  const handlePacienteChange = (id: number | null) => {
    setPacienteId(id);
  };

  const handleTratChange = async (id: number | null) => {
    setTratPredId(id);
    if (id) {
      setNombreTratamiento('');
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

  const handleCrearTratamientoLibre = (query: string) => {
    setTratPredId(null);
    setNombreTratamiento(query.trim());
    setMaterialRows([]);
    materialRowsRef.current = [];
  };

  const handleAvanceTratChange = async (id: number | null) => {
    if (!id) {
      setAvanceTratamiento(null);
      return;
    }
    try {
      const t = await api.tratamientos.buscarPorId(id);
      setAvanceTratamiento(t);
      if (t.unidadID && !unidad) {
        setUnidadId(t.unidadID);
      }
    } catch (err) {
      addToast('error', err instanceof Error ? err.message : 'No se pudo cargar el tratamiento');
      setAvanceTratamiento(null);
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

  const handleTipoChange = (nuevoTipo: TipoTratamiento) => {
    setTipo(nuevoTipo);
    if (nuevoTipo === 'CONTINUO') setMontoStr('');
    if (nuevoTipo !== 'AVANCE') setAvanceTratamiento(null);
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
      (e.target as HTMLInputElement).blur();
    }
  };

  const buildMaterialesMap = (): Record<string, number> => {
    const materialesMap: Record<string, number> = {};
    for (const row of materialRowsRef.current) {
      if (row.materialId != null && row.cantidad > 0) {
        materialesMap[String(row.materialId)] = row.cantidad;
      }
    }
    return materialesMap;
  };

  const handleGuardar = async () => {
    if (tipo === 'AVANCE') {
      await handleGuardarAvance();
      return;
    }
    if (!operadorId || !pacienteId) { addToast('error', 'Seleccione paciente y operador'); return; }
    const montoVal = montoStr === '' ? null : Number(montoStr);
    const nombreTrim = nombreTratamiento.trim();
    setSaving(true);
    try {
      await api.tratamientos.crear({
        operadorID: operadorId, pacienteID: pacienteId, unidadID: unidadId, fecha,
        tratPredID: tratPredId,
        nombreTratamiento: tratPredId == null ? (nombreTrim || null) : null,
        monto: tipo === 'CONTINUO' ? null : montoVal,
        montoPagado: tipo === 'CONTINUO' ? null : (pagoStr === '' ? 0 : Number(pagoStr)),
        tipo,
        tratamientoPadreID: tratamientoPadre?.tratamientoID ?? null,
        materiales: Object.keys(buildMaterialesMap()).length > 0 ? buildMaterialesMap() : undefined,
      });
      addToast('success', 'Tratamiento creado correctamente');
      clearDraft();
      onSuccess();
    } catch (err) { addToast('error', err instanceof Error ? err.message : 'Error al crear tratamiento'); }
    finally { setSaving(false); }
  };

  const handleGuardarAvance = async () => {
    if (!avanceTratamiento) { addToast('error', 'Seleccione un tratamiento'); return; }
    if (avanceTratamiento.estado === 'ANULADO') {
      addToast('error', 'No se pueden agregar avances a tratamientos anulados.');
      return;
    }
    setSaving(true);
    try {
      if (avanceTratamiento.estado === 'CERRADO') {
        await api.tratamientos.reabrir(avanceTratamiento.tratamientoID);
      }
      const materialesMap = buildMaterialesMap();
      const pago = pagoStr === '' ? null : Number(pagoStr);
      await api.tratamientos.agregarAvance(avanceTratamiento.tratamientoID, {
        fecha,
        unidadID: unidadId,
        pago,
        materiales: Object.keys(materialesMap).length > 0 ? materialesMap : undefined,
      });
      addToast('success', `Avance registrado sobre el tratamiento #${avanceTratamiento.tratamientoID}`);
      clearDraft();
      onSuccess();
    } catch (err) { addToast('error', err instanceof Error ? err.message : 'Error al registrar avance'); }
    finally { setSaving(false); }
  };

  const getEstadoBadgeVariant = (estado: string) =>
    estado === 'CERRADO' ? 'success' : estado === 'ANULADO' ? 'danger' : 'info';

  return (
    <>
      <div className="dialog-overlay" onClick={onClose}>
        <div className="dialog-pane mw-560" onClick={(e) => e.stopPropagation()}>
          <div className="dialog-header">
            <h3 className="dialog-title">
              {tipo === 'AVANCE'
                ? 'Registrar Avance sobre Tratamiento Existente'
                : tratamientoPadre ? 'Crear Tratamiento Continuo' : 'Crear Tratamiento'}
            </h3>
            <button className="btn btn-ghost btn-sm" onClick={() => { clearDraft(); onClose(); }}><X size={18} /></button>
          </div>
          <div className="dialog-body">
            {tratamientoPadre && (
              <div className="alert-banner alert-info mb-16">
                <span>Continuación del tratamiento #{tratamientoPadre.tratamientoID} ({tratamientoPadre.nombreTratamiento})</span>
              </div>
            )}

            <div className="form-group">
              <label className="form-label">Tipo de registro</label>
              <div className="flex gap-8">
                {(['NORMAL', 'CONTINUO', 'AVANCE'] as const).map((t) => (
                  <button key={t} type="button"
                    className={`btn ${tipo === t ? 'btn-primary' : 'btn-secondary'}`}
                    onClick={() => handleTipoChange(t)}
                    disabled={!!tratamientoPadre && t !== 'CONTINUO'}>
                    {t === 'NORMAL' ? 'Común' : t === 'CONTINUO' ? 'Continuo' : 'Avance'}
                  </button>
                ))}
              </div>
              {tratamientoPadre && tipo === 'CONTINUO'
                && <span className="text-muted text-sm">Tipo fijado en Continuo para la continuación del tratamiento.</span>}
              {tipo === 'AVANCE'
                && <span className="text-muted text-sm">
                  Seleccione un tratamiento existente (abierto o cerrado) para registrar un nuevo avance sobre él.
                </span>}
            </div>

            {tipo === 'AVANCE' ? (
              <>
                <div className="form-group">
                  <label className="form-label">Tratamiento</label>
                  <SearchableCombo
                    options={avanceOptions}
                    value={avanceTratamiento?.tratamientoID ?? null}
                    onChange={handleAvanceTratChange}
                    onSearch={setQAvance}
                    placeholder="Buscar por ID o nombre..."
                  />
                </div>
                {avanceTratamiento && (
                  <div className="alert-banner alert-info mb-16">
                    <div className="sv-grid">
                      <div className="sv-row">
                        <span className="sv-label">Tratamiento</span>
                        <span>#{avanceTratamiento.tratamientoID} — {avanceTratamiento.nombreTratamiento}</span>
                      </div>
                      <div className="sv-row">
                        <span className="sv-label">Estado</span>
                        <span><Badge variant={getEstadoBadgeVariant(avanceTratamiento.estado)}>{avanceTratamiento.estado}</Badge></span>
                      </div>
                      <div className="sv-row">
                        <span className="sv-label">Monto total</span>
                        <span className="num">{formatMonto(avanceTratamiento.monto)}</span>
                      </div>
                      <div className="sv-row">
                        <span className="sv-label">Monto pagado</span>
                        <span className="num">{formatMonto(avanceTratamiento.montoPagado)}</span>
                      </div>
                      <div className="sv-row">
                        <span className="sv-label">Saldo pendiente</span>
                        <span className="num">{formatMonto(avanceTratamiento.monto - avanceTratamiento.montoPagado)}</span>
                      </div>
                      {avanceTratamiento.estado === 'CERRADO' && (
                        <div className="sv-row">
                          <span className="sv-label">Aviso</span>
                          <span>Al registrar el avance, el tratamiento volverá a estado ABIERTO.</span>
                        </div>
                      )}
                    </div>
                  </div>
                )}
                {unidad ? (
                  <div className="form-group"><label className="form-label">Unidad</label><input className="text-field w-full" value={`Unidad ${unidad.unidadNro}`} readOnly /></div>
                ) : (
                  <div className="form-group">
                    <label className="form-label">Unidad (opcional)</label>
                    <select className="combo-box w-full" value={unidadId ?? ''} onChange={(e) => setUnidadId(e.target.value === '' ? null : Number(e.target.value))}>
                      <option value="">Sin unidad</option>
                      {unidadesLibres.map((u) => <option key={u.unidadID} value={u.unidadID}>Unidad {u.unidadNro}</option>)}
                    </select>
                  </div>
                )}
              </>
            ) : (
              <>
                {unidad ? (
                  <div className="form-group"><label className="form-label">Unidad</label><input className="text-field w-full" value={`Unidad ${unidad.unidadNro}`} readOnly /></div>
                ) : (
                  <div className="form-group">
                    <label className="form-label">Unidad (opcional)</label>
                    <select className="combo-box w-full" value={unidadId ?? ''} onChange={(e) => setUnidadId(e.target.value === '' ? null : Number(e.target.value))}>
                      <option value="">Sin unidad</option>
                      {unidadesLibres.map((u) => <option key={u.unidadID} value={u.unidadID}>Unidad {u.unidadNro}</option>)}
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

                <div className="form-group">
                  <label className="form-label">Tipo de tratamiento</label>
                  <SearchableCombo
                    options={tOptions}
                    value={tratPredId}
                    onChange={handleTratChange}
                    onSearch={setQTrat}
                    onCreateNew={handleCrearTratamientoLibre}
                    allowCreate
                    placeholder={nombreTratamiento ? `Tratamiento libre: ${nombreTratamiento}` : 'Buscar o escribir tratamiento...'}
                  />
                  {nombreTratamiento && tratPredId == null && (
                    <span className="text-muted text-sm">Usando nombre libre. Si elige un predefinido se sobrescribirá.</span>
                  )}
                </div>

                <div className="form-group">
                  <label className="form-label">Monto total</label>
                  <input type="text" inputMode="decimal" className="text-field w-full" value={montoStr} onChange={(e) => setMontoStr(filterNumeric(e.target.value))}
                    onKeyDown={handleMontoKeyDown}
                    disabled={tipo === 'CONTINUO'} placeholder="0.00" />
                </div>
              </>
            )}

            <div className="form-group"><label className="form-label">Fecha del avance</label><input type="date" className="text-field w-full" value={fecha} onChange={(e) => setFecha(e.target.value)} /></div>

            <div className="form-group">
              <label className="form-label">Pago al tratamiento (opcional)</label>
              <input type="text" inputMode="decimal" className="text-field w-full" value={pagoStr} onChange={(e) => setPagoStr(filterNumeric(e.target.value))}
                onKeyDown={handleMontoKeyDown} placeholder="0.00" />
            </div>

            <div className="form-group">
              <label className="form-label">Materiales</label>
              <MaterialTable rows={materialRows} materials={materiales.data ?? []} onAdd={handleAddRow} onRemove={handleRemoveRow} onMaterialChange={handleMaterialChange} onCantidadChange={handleCantidadChange} />
            </div>
          </div>
          <div className="dialog-footer">
            <button className="btn btn-secondary" onClick={onClose}>Cancelar</button>
            <button className="btn btn-primary" onClick={handleGuardar} disabled={saving}>
              {saving ? 'Guardando...' : tipo === 'AVANCE' ? (<><GitBranch size={14} /> Registrar Avance</>) : 'Crear Tratamiento'}
            </button>
          </div>
        </div>
      </div>

      {showNewPaciente && <CrearPacienteOnTheFly onClose={() => setShowNewPaciente(false)} onCreated={(id) => { handlePacienteChange(id); setShowNewPaciente(false); pacientes.refetch(); }} addToast={addToast} />}
      {showNewOperador && <CrearOperadorOnTheFly onClose={() => setShowNewOperador(false)} onCreated={(id) => { setOperadorId(id); setShowNewOperador(false); operadores.refetch(); }} addToast={addToast} />}
    </>
  );
}
