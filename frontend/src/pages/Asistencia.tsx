import { useState, useCallback, useEffect, useRef } from 'react';
import { SearchableCombo, type SearchableOption } from '../components/SearchableCombo';
import { MaterialTable, type MaterialRow } from '../components/MaterialTable';
import { ConfirmDialog } from '../components/ConfirmDialog';
import { Badge } from '../components/Badge';
import { useApi } from '../hooks/useApi';
import { useToast } from '../hooks/useToast';
import { api } from '../api';
import { hoyISO, horaActual, formatearHora, calcularDuracion, nombreCompleto } from '../lib/format';
import type { Asistencia, PeriodoAusencia } from '../api/types';
import { X, RotateCcw } from 'lucide-react';

export default function Asistencia() {
  const [q, setQ] = useState('');
  const [selectedDocente, setSelectedDocente] = useState<SearchableOption | null>(null);
  const [fecha, setFecha] = useState(hoyISO());
  const [asistencia, setAsistencia] = useState<Asistencia | null>(null);
  const [ausencias, setAusencias] = useState<PeriodoAusencia[]>([]);
  const [materialRows, setMaterialRows] = useState<MaterialRow[]>([]);
  const [showAnular, setShowAnular] = useState(false);
  const [motivoAusencia, setMotivoAusencia] = useState('');
  const [showEditDefaults, setShowEditDefaults] = useState(false);
  const [defaultMatRows, setDefaultMatRows] = useState<MaterialRow[]>([]);
  const [editDefaultsSaving, setEditDefaultsSaving] = useState(false);
  const [dirty, setDirty] = useState(false);
  const [savingMat, setSavingMat] = useState(false);
  const [showRevertirSalida, setShowRevertirSalida] = useState(false);
  const { addToast } = useToast();

  const materialRowsRef = useRef<MaterialRow[]>([]);
  const originalRowsRef = useRef<MaterialRow[]>([]);
  const mounted = useRef(true);

  const asistenciaPorFecha = useApi(() => api.asistencia.porFecha(fecha), [fecha]);
  const docentes = useApi(() => api.catalogos.docentes.listar(q || undefined), [q]);
  const materiales = useApi(() => api.catalogos.materiales.listar());

  const docenteOptions: SearchableOption[] = (docentes.data ?? []).map((d) => ({
    id: d.docenteID,
    label: nombreCompleto(d.nombres, d.apellidos),
  }));

  const loadDefaults = useCallback(async (): Promise<{ materialId: number; cantidad: number }[]> => {
    try {
      return await api.asistencia.materialesDefault.listar();
    } catch (err) {
      console.error('Error al cargar materiales predeterminados:', err);
      return [];
    }
  }, []);

  const cargarDetalle = useCallback(async (asistenciaId: number) => {
    try {
      const detalle = await api.asistencia.detalle(asistenciaId);
      if (!mounted.current) return;
      setAsistencia(detalle.asistencia);
      setAusencias(detalle.ausencias);

      const mats = detalle.materiales;
      if (mats.length > 0) {
        const rows = mats.map((m, i) => ({
          key: `existing-${m.materialesListID ?? i}`,
          materialId: m.materialID, nombreMaterial: m.nombreMaterial, cantidad: m.cantidad,
        }));
        setMaterialRows(rows);
        materialRowsRef.current = rows;
        originalRowsRef.current = rows.map((r) => ({ ...r }));
        setDirty(false);
      } else {
        setMaterialRows([]);
        materialRowsRef.current = [];
        originalRowsRef.current = [];
        setDirty(false);
      }
    } catch (err) {
      if (mounted.current) addToast('error', err instanceof Error ? err.message : 'Error al cargar detalle');
    }
  }, [addToast]);

  useEffect(() => { mounted.current = true; return () => { mounted.current = false; }; }, []);

  useEffect(() => {
    if (asistencia?.asistenciaID) {
      cargarDetalle(asistencia.asistenciaID);
    }
  }, [asistencia?.asistenciaID, cargarDetalle]);

  useEffect(() => {
    setSelectedDocente(null);
    setAsistencia(null);
    setAusencias([]);
    setMaterialRows([]);
    materialRowsRef.current = [];
    originalRowsRef.current = [];
    setDirty(false);
  }, [fecha]);

  const abrirAsistencia = useCallback(async (docenteId: number, nombre: string) => {
    try {
      const hora = horaActual();
      const a = await api.asistencia.abrirDia({ docenteId, fecha, horaEntrada: hora });
      setAsistencia(a);
      setSelectedDocente({ id: docenteId, label: nombre });
      addToast('success', `Asistencia abierta para ${nombre}`);

      const existingMats = await api.asistencia.materialesDelDia(a.asistenciaID);
      if (existingMats.length === 0) {
        const defaults = await loadDefaults();
        const defaultMats: MaterialRow[] = defaults.map((dm, i) => ({
          key: `default-${i}-${Date.now()}`,
          materialId: dm.materialId,
          nombreMaterial: '',
          cantidad: dm.cantidad,
        }));
        if (defaultMats.length > 0) {
          setMaterialRows(defaultMats);
          materialRowsRef.current = defaultMats;
          originalRowsRef.current = [];

          for (const dm of defaultMats) {
            if (dm.materialId) {
              try { await api.asistencia.acumularMaterial(a.asistenciaID, { materialId: dm.materialId, cantidad: dm.cantidad }); } catch (err) { console.error('Error al acumular material predeterminado:', err); }
            }
          }
          setDirty(false);
        }
      }
      asistenciaPorFecha.refetch();
    } catch (err) {
      addToast('error', err instanceof Error ? err.message : 'Error al abrir asistencia');
    }
  }, [fecha, addToast, asistenciaPorFecha, loadDefaults]);

  const guardarMaterialesEnBloque = async () => {
    setSavingMat(true);
    try {
      if (!asistencia) return;
      const materiales: Record<number, number> = {};
      for (const row of materialRowsRef.current) {
        if (row.materialId == null || row.cantidad <= 0) continue;
        materiales[row.materialId] = (materiales[row.materialId] ?? 0) + row.cantidad;
      }
      await api.asistencia.reemplazarMateriales(asistencia.asistenciaID, materiales);
      await cargarDetalle(asistencia.asistenciaID);
      setDirty(false);
      addToast('success', 'Materiales guardados correctamente');
    } catch (err) {
      addToast('error', err instanceof Error ? err.message : 'Error al guardar materiales');
    } finally {
      setSavingMat(false);
    }
  };

  const handleSelectDocente = async (id: number | null) => {
    if (id === null) return;

    if (asistencia && dirty) {
      await guardarMaterialesEnBloque();
    }

    const opt = docenteOptions.find((d) => d.id === id);
    if (!opt) return;
    const estadoFecha = (asistenciaPorFecha.data ?? []).find((d) => d.docenteID === id);
    if (estadoFecha?.presente && estadoFecha.asistenciaID) {
      const detalle = await api.asistencia.detalle(estadoFecha.asistenciaID);
      setAsistencia(detalle.asistencia);
      setAusencias(detalle.ausencias);
      setSelectedDocente(opt);
    } else {
      await abrirAsistencia(id, opt.label);
    }
  };

  const handleRegistrarSalida = async () => {
    if (!asistencia) return;
    const hora = horaActual();
    try {
      await api.asistencia.registrarSalida(asistencia.asistenciaID, hora);
      setAsistencia((prev) => prev ? { ...prev, horaSalida: hora } : null);
      addToast('success', `Salida registrada a las ${formatearHora(hora)}`);
      asistenciaPorFecha.refetch();
    } catch (err) {
      addToast('error', err instanceof Error ? err.message : 'Error al registrar salida');
    }
  };

  const handleRevertirSalida = async () => {
    if (!asistencia) return;
    try {
      await api.asistencia.revertirSalida(asistencia.asistenciaID);
      setAsistencia((prev) => prev ? { ...prev, horaSalida: '' } : null);
      addToast('success', 'Registro de salida revertido correctamente');
      asistenciaPorFecha.refetch();
    } catch (err) {
      addToast('error', err instanceof Error ? err.message : 'Error al revertir salida');
    }
    setShowRevertirSalida(false);
  };

  const handleEditarEntrada = async (nuevaHora: string) => {
    if (!asistencia) return;
    try {
      await api.asistencia.registrarEntrada(asistencia.asistenciaID, nuevaHora);
      setAsistencia((prev) => prev ? { ...prev, horaEntrada: nuevaHora } : null);
      addToast('success', 'Hora de entrada actualizada');
    } catch (err) {
      addToast('error', err instanceof Error ? err.message : 'Error al actualizar entrada');
    }
  };

  const handleIniciarAusencia = async () => {
    if (!asistencia) return;
    const hora = horaActual();
    try {
      const nueva = await api.asistencia.iniciarAusencia(asistencia.asistenciaID, hora, motivoAusencia || undefined);
      setAusencias((prev) => [...prev, nueva]);
      setMotivoAusencia('');
      addToast('info', `Ausencia iniciada a las ${formatearHora(hora)}`);
      asistenciaPorFecha.refetch();
    } catch (err) {
      addToast('error', err instanceof Error ? err.message : 'Error al iniciar ausencia');
    }
  };

  const handleRegresarAusencia = async (ausId: number) => {
    if (!asistencia) return;
    const hora = horaActual();
    try {
      await api.asistencia.finalizarAusencia(asistencia.asistenciaID, ausId, hora);
      setAusencias((prev) => prev.map((a) => a.ausenciaID === ausId ? { ...a, horaFin: hora } : a));
      addToast('info', `Regreso registrado a las ${formatearHora(hora)}`);
      asistenciaPorFecha.refetch();
    } catch (err) {
      addToast('error', err instanceof Error ? err.message : 'Error al registrar regreso');
    }
  };

  const handleEliminarAusencia = async (ausId: number) => {
    if (!asistencia) return;
    try {
      await api.asistencia.eliminarAusencia(asistencia.asistenciaID, ausId);
      setAusencias((prev) => prev.filter((a) => a.ausenciaID !== ausId));
      addToast('success', 'Periodo de ausencia eliminado');
      asistenciaPorFecha.refetch();
    } catch (err) {
      addToast('error', err instanceof Error ? err.message : 'Error al eliminar ausencia');
    }
  };

  const handleAddMaterial = () => {
    setMaterialRows((prev) => {
      const next = [...prev, { key: `new-${Date.now()}`, materialId: null, nombreMaterial: '', cantidad: 0 }];
      materialRowsRef.current = next;
      return next;
    });
    setDirty(true);
  };

  const handleRemoveMaterial = (key: string) => {
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

  const handleAnular = async (motivo?: string) => {
    if (!asistencia || !motivo) return;
    try {
      await api.asistencia.anular(asistencia.asistenciaID, motivo);
      addToast('success', 'Asistencia anulada correctamente');
      setAsistencia(null); setAusencias([]); setMaterialRows([]); setSelectedDocente(null);
      setDirty(false);
      materialRowsRef.current = [];
      originalRowsRef.current = [];
      asistenciaPorFecha.refetch();
    } catch (err) { addToast('error', err instanceof Error ? err.message : 'Error al anular'); }
    setShowAnular(false);
  };

  const handleRestaurarDefault = async () => {
    if (!asistencia) return;
    const defaults = await loadDefaults();
    const defaultMats: MaterialRow[] = defaults.map((dm, i) => ({
      key: `default-${i}-${Date.now()}`,
      materialId: dm.materialId,
      nombreMaterial: '',
      cantidad: dm.cantidad,
    }));
    setMaterialRows(defaultMats);
    materialRowsRef.current = defaultMats;
    const materialesMap: Record<number, number> = {};
    for (const dm of defaults) {
      if (dm.materialId) {
        materialesMap[dm.materialId] = dm.cantidad;
      }
    }
    try {
      await api.asistencia.reemplazarMateriales(asistencia.asistenciaID, materialesMap);
    } catch (err) {
      console.error('Error al reemplazar materiales:', err);
      addToast('error', err instanceof Error ? err.message : 'Error al restaurar materiales');
      return;
    }
    originalRowsRef.current = defaultMats.map((r) => ({ ...r }));
    setDirty(false);
    addToast('info', 'Lista de materiales restaurada al predeterminado');
  };

  const tieneAusenciaAbierta = ausencias.some((a) => !a.horaFin);
  const diaCerrado = asistencia?.horaSalida != null && asistencia.horaSalida !== '';
  const diaActivo = asistencia && asistencia.horaEntrada && !diaCerrado;
  const totalMat = materialRows.filter((r) => r.materialId != null).length;
  const esHoy = fecha === hoyISO();

  const openEditDefaults = async () => {
    const defaults = await loadDefaults();
    setDefaultMatRows(defaults.map((dm, i) => ({
      key: `def-${i}-${Date.now()}`,
      materialId: dm.materialId,
      nombreMaterial: '',
      cantidad: dm.cantidad,
    })));
    setShowEditDefaults(true);
  };

  const handleEditDefaultsAdd = () => {
    setDefaultMatRows((prev) => [...prev, { key: `defnew-${Date.now()}`, materialId: null, nombreMaterial: '', cantidad: 0 }]);
  };

  const handleEditDefaultsRemove = (key: string) => {
    setDefaultMatRows((prev) => prev.filter((r) => r.key !== key));
  };

  const handleEditDefaultsMatChange = (key: string, materialId: number) => {
    setDefaultMatRows((prev) => prev.map((r) => r.key === key ? { ...r, materialId } : r));
  };

  const handleEditDefaultsCantChange = (key: string, cantidad: number) => {
    setDefaultMatRows((prev) => prev.map((r) => r.key === key ? { ...r, cantidad } : r));
  };

  const handleGuardarDefaults = async () => {
    setEditDefaultsSaving(true);
    try {
      const payload = defaultMatRows
        .filter((r) => r.materialId != null && r.cantidad > 0)
        .map((r) => ({ materialId: r.materialId!, cantidad: r.cantidad }));
      await api.asistencia.materialesDefault.guardar(payload);
      addToast('success', 'Lista predeterminada guardada');
      setShowEditDefaults(false);
    } catch (err) {
      addToast('error', err instanceof Error ? err.message : 'Error al guardar');
    } finally {
      setEditDefaultsSaving(false);
    }
  };

  return (
    <div>
      <div className="view-header">
        <h1 className="view-title">Asistencia Docente</h1>
        <p className="subtitle">Registro de entrada, salida, ausencias y materiales</p>
      </div>

      <div className="card">
        <div className="card-header">
          <h3 className="card-title">Docentes registrados</h3>
        </div>
        <div className="table-container asistencia-table-container">
          <table className="data-table">
            <thead>
              <tr>
                <th>Nombre</th>
                <th>Apellidos</th>
                <th className="w-200">Estado</th>
                <th className="w-120" />
              </tr>
            </thead>
            <tbody>
              {(asistenciaPorFecha.data ?? []).map((d) => {
                let estadoBadge: React.ReactNode;
                if (!d.presente) {
                  estadoBadge = <Badge variant="neutral"><span className="led led-danger" />Ausente</Badge>;
                } else if (d.horaSalida) {
                  estadoBadge = <Badge variant="neutral">Finalizó a las {formatearHora(d.horaSalida)}</Badge>;
                } else if (d.enAusencia) {
                  estadoBadge = <Badge variant="warning"><span className="led led-warning" />Ausente temporalmente</Badge>;
                } else {
                  estadoBadge = <Badge variant="success"><span className="led led-ok" />Presente desde {formatearHora(d.horaEntrada)}</Badge>;
                }

                return (
                  <tr key={d.docenteID}>
                    <td>{d.nombres}</td>
                    <td>{d.apellidos}</td>
                    <td>{estadoBadge}</td>
                    <td>
                      {d.presente ? (
                        <button className="btn btn-ghost btn-sm" onClick={() => handleSelectDocente(d.docenteID)}>
                          Ver detalle
                        </button>
                      ) : (
                        <button className="btn btn-primary btn-sm" onClick={() => handleSelectDocente(d.docenteID)}>
                          Registrar
                        </button>
                      )}
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </div>

      <div className="card mb-20">
        <div className="card-header flex flex-center flex-between">
          <h3 className="card-title">Búsqueda manual</h3>
          <div className="flex flex-center gap-8">
            <button className="btn btn-ghost btn-sm" onClick={handleRestaurarDefault} disabled={!asistencia} title={!asistencia ? 'Abra una asistencia para restaurar su lista de materiales' : undefined}>Restaurar lista predeterminada</button>
            <button className="btn btn-ghost btn-sm" onClick={openEditDefaults}>Editar lista predeterminada</button>
          </div>
        </div>
        <div className="flex flex-center items-end flex-wrap gap-12">
          <div className="flex-1 mw-400">
            <label className="form-label">Docente</label>
            <SearchableCombo options={docenteOptions} value={selectedDocente?.id ?? null} onChange={handleSelectDocente} onSearch={setQ} placeholder="Buscar docente por nombre..." />
          </div>
          <div>
            <label className="form-label">Fecha</label>
            <input type="date" className="text-field w-160" value={fecha} onChange={(e) => setFecha(e.target.value)} />
          </div>
        </div>
      </div>

      {asistencia && selectedDocente && (
        <div className="card">
          <div className="card-header flex flex-center flex-between">
            <h3 className="card-title">Registro del día — {selectedDocente.label} — {fecha}</h3>
            <div className="flex flex-center gap-8">
              {!esHoy && <Badge variant="warning">Registro manual</Badge>}
              {diaCerrado
                ? <Badge variant="neutral">Día finalizado</Badge>
                : tieneAusenciaAbierta
                  ? <Badge variant="warning"><span className="led led-warning" />Ausente temporalmente</Badge>
                  : <Badge variant="success"><span className="led led-ok" />En clínica</Badge>
              }
            </div>
          </div>

          <div className="grid-2col">
            <div>
              <label className="form-label">Hora de entrada</label>
              <div className="flex flex-center gap-8">
                <input
                  type="time"
                  className="text-field w-160"
                  step="1"
                  value={asistencia.horaEntrada?.substring(0, 8) ?? ''}
                  onChange={(e) => handleEditarEntrada(e.target.value)}
                />
                {diaActivo && (
                  <span className="text-muted text-sm">en clínica desde las {formatearHora(asistencia.horaEntrada)}</span>
                )}
              </div>
            </div>
            <div>
              <label className="form-label">Hora de salida</label>
              <div className="flex flex-center gap-8">
                {diaCerrado ? (
                  <>
                    <span className="text-sm">
                      <Badge variant="neutral">{formatearHora(asistencia.horaSalida)}</Badge>
                    </span>
                    <button className="btn btn-ghost btn-sm" onClick={() => setShowRevertirSalida(true)} title="Revertir registro de salida">
                      <RotateCcw size={14} />
                    </button>
                  </>
                ) : (
                  <button className="btn btn-primary btn-sm" onClick={handleRegistrarSalida} disabled={tieneAusenciaAbierta}>
                    Registrar salida ({formatearHora(horaActual())})
                  </button>
                )}
                {tieneAusenciaAbierta && (
                  <span className="text-warning text-sm">Debe registrar el regreso antes</span>
                )}
              </div>
            </div>
          </div>

          <div className="card" style={{ marginBottom: 16, background: 'var(--color-surface-hover)' }}>
            <div className="card-header flex flex-center flex-between">
              <h3 className="card-title text-sm">
                {ausencias.length > 0 ? `Periodos de ausencia (${ausencias.length})` : 'Sin periodos de ausencia'}
              </h3>
              {diaActivo && !tieneAusenciaAbierta && (
                <div className="flex flex-center gap-8">
                  <input
                    type="text"
                    className="text-field w-160"
                    placeholder="Motivo (opcional)"
                    value={motivoAusencia}
                    onChange={(e) => setMotivoAusencia(e.target.value)}
                  />
                  <button className="btn btn-warning btn-sm" onClick={handleIniciarAusencia}>
                    Iniciar ausencia ({formatearHora(horaActual())})
                  </button>
                </div>
              )}
            </div>

            {ausencias.length > 0 ? (
              <table className="data-table" style={{ margin: 0 }}>
                <thead>
                  <tr>
                    <th>Inicio</th>
                    <th>Fin</th>
                    <th>Duración</th>
                    <th>Motivo</th>
                    <th className="w-160" />
                  </tr>
                </thead>
                <tbody>
                  {ausencias.map((a) => (
                    <tr key={a.ausenciaID}>
                      <td>{formatearHora(a.horaInicio)}</td>
                      <td>{a.horaFin ? formatearHora(a.horaFin) : <Badge variant="warning">En curso</Badge>}</td>
                      <td>{calcularDuracion(a.horaInicio, a.horaFin) || '—'}</td>
                      <td><span className="text-muted text-sm">{a.motivo || '—'}</span></td>
                      <td>
                        {!a.horaFin ? (
                          <button className="btn btn-success btn-sm" onClick={() => handleRegresarAusencia(a.ausenciaID)}>
                            Registrar regreso
                          </button>
                        ) : (
                          <button className="btn btn-ghost btn-sm" onClick={() => handleEliminarAusencia(a.ausenciaID)}>
                            Eliminar
                          </button>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            ) : (
              <div className="text-muted text-sm p-12">
                El docente no ha registrado salidas temporales. Use el botón "Iniciar ausencia" cuando el docente se retire de la clínica temporalmente.
              </div>
            )}
          </div>

          <MaterialTable rows={materialRows} materials={materiales.data ?? []} onAdd={handleAddMaterial} onRemove={handleRemoveMaterial} onMaterialChange={handleMaterialChange} onCantidadChange={handleCantidadChange} />

          <div className="flex flex-center flex-between" style={{ marginTop: 16, paddingTop: 16, borderTop: '1px solid var(--color-border)' }}>
            <span className="text-muted text-sm">
              {totalMat} material(es){dirty && <span style={{ color: 'var(--color-warning-text)', marginLeft: 8 }}>(cambios sin guardar)</span>}
            </span>
            <div className="flex gap-8">
              {dirty && (
                <button className="btn btn-primary btn-sm" onClick={guardarMaterialesEnBloque} disabled={savingMat}>
                  {savingMat ? 'Guardando...' : 'Guardar materiales'}
                </button>
              )}
              <button className="btn btn-danger btn-sm" onClick={() => setShowAnular(true)}>Anular asistencia</button>
            </div>
          </div>
        </div>
      )}

      <ConfirmDialog open={showRevertirSalida} title="Revertir registro de salida"
        message={`Confirme que desea revertir el registro de salida de ${selectedDocente?.label}. La asistencia quedará abierta nuevamente.`}
        confirmLabel="Sí, revertir salida" variant="primary"
        onConfirm={handleRevertirSalida} onCancel={() => setShowRevertirSalida(false)} />

      <ConfirmDialog open={showAnular} title="Anular asistencia docente"
        message={`Confirme que desea anular la asistencia de ${selectedDocente?.label} del día ${fecha}.`}
        confirmLabel="Sí, anular asistencia" variant="danger" requireMotivo
        onConfirm={handleAnular} onCancel={() => setShowAnular(false)} />

      {showEditDefaults && (
        <div className="dialog-overlay" onClick={() => setShowEditDefaults(false)}>
          <div className="dialog-pane mw-560" onClick={(e) => e.stopPropagation()}>
            <div className="dialog-header">
              <h3 className="dialog-title">Editar lista predeterminada de materiales</h3>
              <button className="btn btn-ghost btn-sm" onClick={() => setShowEditDefaults(false)}><X size={18} /></button>
            </div>
            <div className="dialog-body">
              <p className="dialog-message">Estos materiales se asignarán automáticamente al abrir la asistencia de un docente.</p>
              <MaterialTable
                rows={defaultMatRows}
                materials={materiales.data ?? []}
                onAdd={handleEditDefaultsAdd}
                onRemove={handleEditDefaultsRemove}
                onMaterialChange={handleEditDefaultsMatChange}
                onCantidadChange={handleEditDefaultsCantChange}
              />
            </div>
            <div className="dialog-footer">
              <button className="btn btn-secondary" onClick={() => setShowEditDefaults(false)}>Cancelar</button>
              <button className="btn btn-primary" onClick={handleGuardarDefaults} disabled={editDefaultsSaving}>
                {editDefaultsSaving ? 'Guardando...' : 'Guardar'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
