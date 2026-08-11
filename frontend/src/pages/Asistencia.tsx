import { useState, useCallback, useEffect } from 'react';
import { SearchableCombo, type SearchableOption } from '../components/SearchableCombo';
import { MaterialTable, type MaterialRow } from '../components/MaterialTable';
import { ConfirmDialog } from '../components/ConfirmDialog';
import { Badge } from '../components/Badge';
import { useApi } from '../hooks/useApi';
import { useToast } from '../hooks/useToast';
import { api } from '../api';
import { hoyISO, horaActual, formatearHora, calcularDuracion, nombreCompleto } from '../lib/format';
import type { Asistencia, PeriodoAusencia } from '../api/types';
import { X } from 'lucide-react';

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
  const { addToast } = useToast();

  const asistenciaHoy = useApi(() => api.dashboard.asistenciaHoy());
  const docentes = useApi(() => api.catalogos.docentes.listar(q || undefined), [q]);
  const materiales = useApi(() => api.catalogos.materiales.listar());

  const docenteOptions: SearchableOption[] = (docentes.data ?? []).map((d) => ({
    id: d.docenteID,
    label: nombreCompleto(d.nombres, d.apellidos),
  }));

  const loadDefaults = useCallback(async (): Promise<{ materialId: number; cantidad: number }[]> => {
    try {
      return await api.asistencia.materialesDefault.listar();
    } catch {
      return [];
    }
  }, []);

  const cargarDetalle = useCallback(async (asistenciaId: number) => {
    try {
      const detalle = await api.asistencia.detalle(asistenciaId);
      setAsistencia(detalle.asistencia);
      setAusencias(detalle.ausencias);

      const mats = detalle.materiales;
      if (mats.length > 0) {
        setMaterialRows(mats.map((m, i) => ({
          key: `existing-${m.materialesListID ?? i}`,
          materialId: m.materialID, nombreMaterial: m.nombreMaterial, cantidad: m.cantidad,
        })));
      } else {
        setMaterialRows([]);
      }
    } catch (err) {
      addToast('error', err instanceof Error ? err.message : 'Error al cargar detalle');
    }
  }, [addToast]);

  useEffect(() => {
    if (asistencia?.asistenciaID) {
      cargarDetalle(asistencia.asistenciaID);
    }
  }, [asistencia?.asistenciaID, cargarDetalle]);

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
          for (const dm of defaultMats) {
            if (dm.materialId) {
              try { await api.asistencia.acumularMaterial(a.asistenciaID, { materialId: dm.materialId, cantidad: dm.cantidad }); } catch {}
            }
          }
        }
      }
      asistenciaHoy.refetch();
    } catch (err) {
      addToast('error', err instanceof Error ? err.message : 'Error al abrir asistencia');
    }
  }, [fecha, addToast, asistenciaHoy, loadDefaults]);

  const handleSelectDocente = async (id: number | null) => {
    if (id === null) return;
    const opt = docenteOptions.find((d) => d.id === id);
    if (!opt) return;
    const estadoHoy = (asistenciaHoy.data ?? []).find((d) => d.docenteID === id);
    if (estadoHoy?.presente && estadoHoy.asistenciaID) {
      const detalle = await api.asistencia.detalle(estadoHoy.asistenciaID);
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
      asistenciaHoy.refetch();
    } catch (err) {
      addToast('error', err instanceof Error ? err.message : 'Error al registrar salida');
    }
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
      asistenciaHoy.refetch();
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
      asistenciaHoy.refetch();
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
      asistenciaHoy.refetch();
    } catch (err) {
      addToast('error', err instanceof Error ? err.message : 'Error al eliminar ausencia');
    }
  };

  const handleAddMaterial = () => {
    setMaterialRows((prev) => [...prev, { key: `new-${Date.now()}`, materialId: null, nombreMaterial: '', cantidad: 0 }]);
  };

  const handleRemoveMaterial = (key: string) => {
    setMaterialRows((prev) => prev.filter((r) => r.key !== key));
  };

  const handleMaterialChange = async (key: string, materialId: number) => {
    setMaterialRows((prev) => prev.map((r) => r.key === key ? { ...r, materialId } : r));
    if (asistencia) {
      const row = materialRows.find((r) => r.key === key);
      if (row?.cantidad && row.cantidad > 0) {
        try { await api.asistencia.acumularMaterial(asistencia.asistenciaID, { materialId, cantidad: row.cantidad }); } catch {}
      }
    }
  };

  const handleCantidadChange = async (key: string, cantidad: number) => {
    setMaterialRows((prev) => prev.map((r) => r.key === key ? { ...r, cantidad } : r));
    if (asistencia) {
      const row = materialRows.find((r) => r.key === key);
      if (row?.materialId && cantidad > 0) {
        try { await api.asistencia.acumularMaterial(asistencia.asistenciaID, { materialId: row.materialId, cantidad }); } catch {}
      }
    }
  };

  const handleAnular = async (motivo?: string) => {
    if (!asistencia || !motivo) return;
    try {
      await api.asistencia.anular(asistencia.asistenciaID, motivo);
      addToast('success', 'Asistencia anulada correctamente');
      setAsistencia(null); setAusencias([]); setMaterialRows([]); setSelectedDocente(null);
      asistenciaHoy.refetch();
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
    for (const dm of defaultMats) {
      if (dm.materialId) {
        try { await api.asistencia.acumularMaterial(asistencia.asistenciaID, { materialId: dm.materialId, cantidad: dm.cantidad }); } catch {}
      }
    }
    addToast('info', 'Lista de materiales restaurada al predeterminado');
  };

  const tieneAusenciaAbierta = ausencias.some((a) => !a.horaFin);
  const diaCerrado = asistencia?.horaSalida != null;
  const diaActivo = asistencia && asistencia.horaEntrada && !diaCerrado;

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
        <div className="table-container">
          <table className="data-table">
            <thead>
              <tr>
                <th>Nombre</th>
                <th>Apellidos</th>
                <th style={{ width: 200 }}>Estado</th>
                <th style={{ width: 120 }} />
              </tr>
            </thead>
            <tbody>
              {(asistenciaHoy.data ?? []).map((d) => {
                let estadoBadge: React.ReactNode;
                if (!d.presente) {
                  estadoBadge = <Badge variant="neutral"><span className="led led-danger" />Ausente</Badge>;
                } else if (d.horaSalida) {
                  estadoBadge = <Badge variant="neutral">Finalizo a las {formatearHora(d.horaSalida)}</Badge>;
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

      <div className="card" style={{ marginBottom: 20 }}>
        <div className="card-header">
          <h3 className="card-title">Busqueda manual</h3>
        </div>
        <div style={{ display: 'flex', gap: 12, alignItems: 'flex-end', flexWrap: 'wrap' }}>
          <div style={{ flex: 1, maxWidth: 400 }}>
            <label className="form-label">Docente</label>
            <SearchableCombo options={docenteOptions} value={selectedDocente?.id ?? null} onChange={handleSelectDocente} onSearch={setQ} placeholder="Buscar docente por nombre..." />
          </div>
          <div>
            <label className="form-label">Fecha</label>
            <input type="date" className="text-field" value={fecha} onChange={(e) => setFecha(e.target.value)} style={{ width: 160 }} />
          </div>
        </div>
      </div>

      {asistencia && selectedDocente && (
        <div className="card">
          <div className="card-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <h3 className="card-title">Registro del dia — {selectedDocente.label} — {fecha}</h3>
            {diaCerrado
              ? <Badge variant="neutral">Dia finalizado</Badge>
              : tieneAusenciaAbierta
                ? <Badge variant="warning"><span className="led led-warning" />Ausente temporalmente</Badge>
                : <Badge variant="success"><span className="led led-ok" />En clinica</Badge>
            }
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 16, marginBottom: 20 }}>
            <div>
              <label className="form-label">Hora de entrada</label>
              <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
                <input
                  type="time"
                  className="text-field"
                  step="1"
                  value={asistencia.horaEntrada?.substring(0, 8) ?? ''}
                  onChange={(e) => handleEditarEntrada(e.target.value + ':00')}
                  style={{ width: 160 }}
                />
                {diaActivo && (
                  <span className="text-muted text-sm">en clinica desde las {formatearHora(asistencia.horaEntrada)}</span>
                )}
              </div>
            </div>
            <div>
              <label className="form-label">Hora de salida</label>
              <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
                {diaCerrado ? (
                  <span className="text-sm">
                    <Badge variant="neutral">{formatearHora(asistencia.horaSalida)}</Badge>
                  </span>
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
            <div className="card-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <h3 className="card-title" style={{ fontSize: 14 }}>
                {ausencias.length > 0 ? `Periodos de ausencia (${ausencias.length})` : 'Sin periodos de ausencia'}
              </h3>
              {diaActivo && !tieneAusenciaAbierta && (
                <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
                  <input
                    type="text"
                    className="text-field"
                    placeholder="Motivo (opcional)"
                    value={motivoAusencia}
                    onChange={(e) => setMotivoAusencia(e.target.value)}
                    style={{ width: 160 }}
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
                    <th>Duracion</th>
                    <th>Motivo</th>
                    <th style={{ width: 160 }} />
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
              <div className="text-muted text-sm" style={{ padding: 12 }}>
                El docente no ha registrado salidas temporales. Use el boton "Iniciar ausencia" cuando el docente se retire de la clinica temporalmente.
              </div>
            )}
          </div>

          <MaterialTable rows={materialRows} materials={materiales.data ?? []} onAdd={handleAddMaterial} onRemove={handleRemoveMaterial} onMaterialChange={handleMaterialChange} onCantidadChange={handleCantidadChange} />

          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: 16, paddingTop: 16, borderTop: '1px solid var(--color-border)' }}>
            <span className="text-muted text-sm">{materialRows.length} materiales registrados hoy</span>
            <div style={{ display: 'flex', gap: 8 }}>
              <button className="btn btn-ghost btn-sm" onClick={handleRestaurarDefault}>Restaurar lista predeterminada</button>
              <button className="btn btn-ghost btn-sm" onClick={openEditDefaults}>Editar lista predeterminada</button>
              <button className="btn btn-danger btn-sm" onClick={() => setShowAnular(true)}>Anular asistencia</button>
            </div>
          </div>
        </div>
      )}

      <ConfirmDialog open={showAnular} title="Anular asistencia docente"
        message={`Confirme que desea anular la asistencia de ${selectedDocente?.label} del dia ${fecha}.`}
        confirmLabel="Si, anular asistencia" variant="danger" requireMotivo
        onConfirm={handleAnular} onCancel={() => setShowAnular(false)} />

      {showEditDefaults && (
        <div className="dialog-overlay" onClick={() => setShowEditDefaults(false)}>
          <div className="dialog-pane" onClick={(e) => e.stopPropagation()} style={{ maxWidth: 560 }}>
            <div className="dialog-header">
              <h3 className="dialog-title">Editar lista predeterminada de materiales</h3>
              <button className="btn btn-ghost btn-sm" onClick={() => setShowEditDefaults(false)}><X size={18} /></button>
            </div>
            <div className="dialog-body">
              <p className="dialog-message">Estos materiales se asignaran automaticamente al abrir la asistencia de un docente.</p>
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
