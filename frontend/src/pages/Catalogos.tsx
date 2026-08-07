import { useState } from 'react';
import { useApi } from '../hooks/useApi';
import { useToast } from '../hooks/useToast';
import { api } from '../api';
import { CatalogoTabla, type Column } from '../components/CatalogoTabla';
import { CatalogoModal } from '../components/CatalogoModal';
import { ConfirmDialog } from '../components/ConfirmDialog';
import { Badge } from '../components/Badge';
import { Plus, ChevronDown, Trash2, Pencil } from 'lucide-react';
import type {
  Materiales,
  Docente,
  Operador,
  TratamientoPredefinido,
  Tratamiento,
} from '../api/types';

type TabId = 'materiales' | 'docentes' | 'operadores' | 'tratamientos-pred' | 'tratamientos-realizados';

const TABS: { id: TabId; label: string }[] = [
  { id: 'materiales', label: 'Materiales' },
  { id: 'docentes', label: 'Docentes' },
  { id: 'operadores', label: 'Especialistas' },
  { id: 'tratamientos-pred', label: 'Tratamientos Predef.' },
  { id: 'tratamientos-realizados', label: 'Trat. Realizados' },
];

export default function Catalogos() {
  const [tab, setTab] = useState<TabId>('materiales');
  const { addToast } = useToast();

  return (
    <div>
      <div className="view-header">
        <h1 className="view-title">Catalogos</h1>
        <p className="view-subtitle">Gestion centralizada de catalogos del sistema</p>
      </div>

      <div className="card">
        <div className="tabs">
          {TABS.map((t) => (
            <button
              key={t.id}
              className={`tab ${tab === t.id ? 'active' : ''}`}
              onClick={() => setTab(t.id)}
            >
              {t.label}
            </button>
          ))}
        </div>
        <div style={{ paddingTop: 16 }}>
          {tab === 'materiales' && <TabMateriales addToast={addToast} />}
          {tab === 'docentes' && <TabDocentes addToast={addToast} />}
          {tab === 'operadores' && <TabOperadores addToast={addToast} />}
          {tab === 'tratamientos-pred' && <TabTratamientosPred addToast={addToast} />}
          {tab === 'tratamientos-realizados' && <TabTratamientosRealizados />}
        </div>
      </div>
    </div>
  );
}

function TabMateriales({ addToast }: { addToast: ReturnType<typeof useToast>['addToast'] }) {
  const [q, setQ] = useState('');
  const [modal, setModal] = useState<{ edit?: Materiales } | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<Materiales | null>(null);
  const [saving, setSaving] = useState(false);

  const data = useApi(() => api.catalogos.materiales.listar(q || undefined), [q]);
  const list = data.data ?? [];

  const columns: Column<Materiales>[] = [
    { key: 'id', header: 'ID', width: 60, render: (r) => <span className="num">{r.materialID}</span> },
    { key: 'nombre', header: 'Nombre', render: (r) => r.nombre },
    { key: 'unidad', header: 'Unidad', width: 120, render: (r) => r.unidad },
    { key: 'estado', header: 'Estado', width: 100, render: (r) => (
      <Badge variant={r.estado === 1 ? 'success' : 'neutral'}>{r.estado === 1 ? 'Activo' : 'Inactivo'}</Badge>
    )},
    { key: 'acciones', header: 'Acciones', width: 100, className: 'text-center', render: (r) => (
      <div className="flex gap-4 justify-center">
        <button className="btn btn-ghost btn-sm" onClick={() => setModal({ edit: r })}><Pencil size={14} /></button>
        <button className="btn btn-ghost btn-sm" onClick={() => setDeleteTarget(r)}><Trash2 size={14} /></button>
      </div>
    )},
  ];

  const handleSave = async (values: Record<string, any>) => {
    setSaving(true);
    try {
      if (modal?.edit) {
        await api.catalogos.materiales.actualizar({
          materialID: modal.edit.materialID,
          nombre: values.nombre,
          unidad: values.unidad,
          estado: modal.edit.estado,
        });
        addToast('success', 'Material actualizado');
      } else {
        await api.catalogos.materiales.crear({ nombre: values.nombre, unidad: values.unidad });
        addToast('success', 'Material creado');
      }
      data.refetch();
      setModal(null);
    } catch (err) {
      addToast('error', err instanceof Error ? err.message : 'Error al guardar');
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async () => {
    if (!deleteTarget) return;
    try {
      await api.catalogos.materiales.eliminar(deleteTarget.materialID);
      addToast('success', 'Material eliminado');
      data.refetch();
    } catch (err) {
      addToast('error', err instanceof Error ? err.message : 'Error al eliminar');
    } finally {
      setDeleteTarget(null);
    }
  };

  return (
    <>
      <div style={{ marginBottom: 12, display: 'flex', justifyContent: 'flex-end' }}>
        <button className="btn btn-primary btn-sm" onClick={() => setModal({})}>
          <Plus size={14} /> Nuevo Material
        </button>
      </div>
      <CatalogoTabla
        columns={columns}
        data={list}
        loading={data.loading}
        searchPlaceholder="Buscar material..."
        onSearch={setQ}
      />
      <CatalogoModal
        open={!!modal}
        title={modal?.edit ? 'Editar Material' : 'Nuevo Material'}
        fields={[
          { key: 'nombre', label: 'Nombre', type: 'text', placeholder: 'Nombre del material' },
          { key: 'unidad', label: 'Unidad de medida', type: 'text', placeholder: 'ej. Caja, Paquete, Unidad' },
        ]}
        initialValues={modal?.edit ? { nombre: modal.edit.nombre, unidad: modal.edit.unidad } : { nombre: '', unidad: '' }}
        onSave={handleSave}
        onCancel={() => setModal(null)}
        saving={saving}
      />
      <ConfirmDialog
        open={!!deleteTarget}
        title="Eliminar material"
        message={`Confirme que desea eliminar "${deleteTarget?.nombre}".`}
        confirmLabel="Eliminar"
        variant="danger"
        onConfirm={handleDelete}
        onCancel={() => setDeleteTarget(null)}
      />
    </>
  );
}

function TabDocentes({ addToast }: { addToast: ReturnType<typeof useToast>['addToast'] }) {
  const [q, setQ] = useState('');
  const [modal, setModal] = useState<{ edit?: Docente } | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<Docente | null>(null);
  const [saving, setSaving] = useState(false);

  const data = useApi(() => api.catalogos.docentes.listar(q || undefined), [q]);
  const list = data.data ?? [];

  const columns: Column<Docente>[] = [
    { key: 'id', header: 'ID', width: 60, render: (r) => <span className="num">{r.docenteID}</span> },
    { key: 'nombres', header: 'Nombres', render: (r) => r.nombres },
    { key: 'apellidos', header: 'Apellidos', render: (r) => r.apellidos },
    { key: 'telefono', header: 'Telefono', width: 130, render: (r) => r.telefono },
    { key: 'estado', header: 'Estado', width: 100, render: (r) => (
      <Badge variant={r.estado === 1 ? 'success' : 'neutral'}>{r.estado === 1 ? 'Activo' : 'Inactivo'}</Badge>
    )},
    { key: 'acciones', header: 'Acciones', width: 100, className: 'text-center', render: (r) => (
      <div className="flex gap-4 justify-center">
        <button className="btn btn-ghost btn-sm" onClick={() => setModal({ edit: r })}><Pencil size={14} /></button>
        <button className="btn btn-ghost btn-sm" onClick={() => setDeleteTarget(r)}><Trash2 size={14} /></button>
      </div>
    )},
  ];

  const handleSave = async (values: Record<string, any>) => {
    setSaving(true);
    try {
      if (modal?.edit) {
        await api.catalogos.docentes.actualizar({
          docenteID: modal.edit.docenteID,
          nombres: values.nombres,
          apellidos: values.apellidos,
          telefono: values.telefono,
          estado: modal.edit.estado,
        });
        addToast('success', 'Docente actualizado');
      } else {
        await api.catalogos.docentes.crear(values as any);
        addToast('success', 'Docente creado');
      }
      data.refetch();
      setModal(null);
    } catch (err) {
      addToast('error', err instanceof Error ? err.message : 'Error al guardar');
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async () => {
    if (!deleteTarget) return;
    try {
      await api.catalogos.docentes.eliminar(deleteTarget.docenteID);
      addToast('success', 'Docente eliminado');
      data.refetch();
    } catch (err) {
      addToast('error', err instanceof Error ? err.message : 'Error al eliminar');
    } finally {
      setDeleteTarget(null);
    }
  };

  return (
    <>
      <div style={{ marginBottom: 12, display: 'flex', justifyContent: 'flex-end' }}>
        <button className="btn btn-primary btn-sm" onClick={() => setModal({})}>
          <Plus size={14} /> Nuevo Docente
        </button>
      </div>
      <CatalogoTabla columns={columns} data={list} loading={data.loading} searchPlaceholder="Buscar docente..." onSearch={setQ} />
      <CatalogoModal
        open={!!modal}
        title={modal?.edit ? 'Editar Docente' : 'Nuevo Docente'}
        fields={[
          { key: 'nombres', label: 'Nombres', type: 'text' },
          { key: 'apellidos', label: 'Apellidos', type: 'text' },
          { key: 'telefono', label: 'Telefono', type: 'text' },
        ]}
        initialValues={modal?.edit ? { nombres: modal.edit.nombres, apellidos: modal.edit.apellidos, telefono: modal.edit.telefono } : { nombres: '', apellidos: '', telefono: '' }}
        onSave={handleSave}
        onCancel={() => setModal(null)}
        saving={saving}
      />
      <ConfirmDialog open={!!deleteTarget} title="Eliminar docente" message={`Confirme que desea eliminar a "${deleteTarget?.nombres} ${deleteTarget?.apellidos}".`} confirmLabel="Eliminar" variant="danger" onConfirm={handleDelete} onCancel={() => setDeleteTarget(null)} />
    </>
  );
}

function TabOperadores({ addToast }: { addToast: ReturnType<typeof useToast>['addToast'] }) {
  const [q, setQ] = useState('');
  const [modal, setModal] = useState<{ edit?: Operador } | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<Operador | null>(null);
  const [saving, setSaving] = useState(false);

  const data = useApi(() => api.catalogos.operadores.listar(q || undefined), [q]);
  const list = data.data ?? [];

  const columns: Column<Operador>[] = [
    { key: 'id', header: 'ID', width: 50, render: (r) => <span className="num">{r.operadorID}</span> },
    { key: 'nombres', header: 'Nombres', render: (r) => r.nombres },
    { key: 'apellidos', header: 'Apellidos', render: (r) => r.apellidos },
    { key: 'dni', header: 'DNI', width: 100, render: (r) => r.dni },
    { key: 'grado', header: 'Grado', width: 100, render: (r) => r.grado },
    { key: 'tipo', header: 'Tipo', width: 100, render: (r) => r.tipo },
    { key: 'periodo', header: 'Periodo', width: 80, render: (r) => <span className="num">{r.periodo}</span> },
    { key: 'estado', header: 'Estado', width: 90, render: (r) => (
      <Badge variant={r.estado === 1 ? 'success' : 'neutral'}>{r.estado === 1 ? 'Activo' : 'Inactivo'}</Badge>
    )},
    { key: 'acciones', header: '', width: 80, className: 'text-center', render: (r) => (
      <div className="flex gap-4 justify-center">
        <button className="btn btn-ghost btn-sm" onClick={() => setModal({ edit: r })}><Pencil size={14} /></button>
        <button className="btn btn-ghost btn-sm" onClick={() => setDeleteTarget(r)}><Trash2 size={14} /></button>
      </div>
    )},
  ];

  const handleSave = async (values: Record<string, any>) => {
    setSaving(true);
    try {
      if (modal?.edit) {
        await api.catalogos.operadores.actualizar({ ...modal.edit, ...values });
        addToast('success', 'Especialista actualizado');
      } else {
        await api.catalogos.operadores.crear(values as any);
        addToast('success', 'Especialista creado');
      }
      data.refetch();
      setModal(null);
    } catch (err) {
      addToast('error', err instanceof Error ? err.message : 'Error al guardar');
    } finally {
      setSaving(false);
    }
  };

  return (
    <>
      <div style={{ marginBottom: 12, display: 'flex', justifyContent: 'flex-end' }}>
        <button className="btn btn-primary btn-sm" onClick={() => setModal({})}>
          <Plus size={14} /> Nuevo Especialista
        </button>
      </div>
      <CatalogoTabla columns={columns} data={list} loading={data.loading} searchPlaceholder="Buscar especialista..." onSearch={setQ} />
      <CatalogoModal
        open={!!modal}
        title={modal?.edit ? 'Editar Especialista' : 'Nuevo Especialista'}
        fields={[
          { key: 'nombres', label: 'Nombres', type: 'text' },
          { key: 'apellidos', label: 'Apellidos', type: 'text' },
          { key: 'dni', label: 'DNI', type: 'text' },
          { key: 'grado', label: 'Grado', type: 'select', options: [{ label: 'Estudiante', value: 'ESTUDIANTE' }, { label: 'Especialista', value: 'ESPECIALISTA' }] },
          { key: 'tipo', label: 'Tipo', type: 'select', options: [{ label: 'Operador', value: 'OPERADOR' }, { label: 'Docente', value: 'DOCENTE' }] },
          { key: 'periodo', label: 'Periodo (año)', type: 'number' },
        ]}
        initialValues={modal?.edit ? {
          nombres: modal.edit.nombres, apellidos: modal.edit.apellidos, dni: modal.edit.dni,
          grado: modal.edit.grado, tipo: modal.edit.tipo, periodo: modal.edit.periodo,
        } : { nombres: '', apellidos: '', dni: '', grado: '', tipo: '', periodo: new Date().getFullYear() }}
        onSave={handleSave}
        onCancel={() => setModal(null)}
        saving={saving}
      />
      <ConfirmDialog open={!!deleteTarget} title="Eliminar especialista" message={`Confirme que desea eliminar a "${deleteTarget?.nombres} ${deleteTarget?.apellidos}".`} confirmLabel="Eliminar" variant="danger" onConfirm={async () => {
        if (!deleteTarget) return;
        try {
          await api.catalogos.operadores.eliminar(deleteTarget.operadorID);
          addToast('success', 'Especialista eliminado');
          data.refetch();
        } catch (err) { addToast('error', err instanceof Error ? err.message : 'Error al eliminar'); }
        setDeleteTarget(null);
      }} onCancel={() => setDeleteTarget(null)} />
    </>
  );
}

function TabTratamientosPred({ addToast }: { addToast: ReturnType<typeof useToast>['addToast'] }) {
  const [q, setQ] = useState('');
  const [modal, setModal] = useState<{ edit?: TratamientoPredefinido } | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<TratamientoPredefinido | null>(null);
  const [expanded, setExpanded] = useState<Set<number>>(new Set());
  const [saving, setSaving] = useState(false);

  const data = useApi(() => api.catalogos.tratamientosPred.listar(q || undefined), [q]);
  const list = data.data ?? [];

  const columns: Column<TratamientoPredefinido>[] = [
    {
      key: 'chevron', header: '', width: 36, render: (r) => (
        <button className="btn btn-ghost btn-sm" onClick={() => setExpanded((prev) => { const n = new Set(prev); n.has(r.tratPredID) ? n.delete(r.tratPredID) : n.add(r.tratPredID); return n; })}>
          <ChevronDown size={14} style={{ transform: expanded.has(r.tratPredID) ? 'rotate(180deg)' : '', transition: 'transform 0.2s' }} />
        </button>
      ),
    },
    { key: 'id', header: 'ID', width: 50, render: (r) => <span className="num">{r.tratPredID}</span> },
    { key: 'nombre', header: 'Nombre', render: (r) => r.nombreTratamiento },
    { key: 'costo', header: 'Costo', width: 100, render: (r) => r.montoSugerido != null ? `S/ ${r.montoSugerido.toFixed(2)}` : '-' },
    { key: 'acciones', header: '', width: 80, className: 'text-center', render: (r) => (
      <div className="flex gap-4 justify-center">
        <button className="btn btn-ghost btn-sm" onClick={() => setModal({ edit: r })}><Pencil size={14} /></button>
        <button className="btn btn-ghost btn-sm" onClick={() => setDeleteTarget(r)}><Trash2 size={14} /></button>
      </div>
    )},
  ];

  return (
    <>
      <div style={{ marginBottom: 12, display: 'flex', justifyContent: 'flex-end' }}>
        <button className="btn btn-primary btn-sm" onClick={() => setModal({})}>
          <Plus size={14} /> Nuevo Tratamiento
        </button>
      </div>
      <CatalogoTabla columns={columns} data={list} loading={data.loading} searchPlaceholder="Buscar tratamiento..." onSearch={setQ} />
      {list.map((item) => expanded.has(item.tratPredID) && (
        <div key={`det-${item.tratPredID}`} className="row-detail">
          <MaterialesDetalle tratPredID={item.tratPredID} />
        </div>
      ))}
      <CatalogoModal
        open={!!modal}
        title={modal?.edit ? 'Editar Tratamiento Predefinido' : 'Nuevo Tratamiento Predefinido'}
        fields={[
          { key: 'nombreTratamiento', label: 'Nombre del tratamiento', type: 'text' },
          { key: 'montoSugerido', label: 'Monto sugerido', type: 'number', placeholder: '0.00' },
        ]}
        initialValues={modal?.edit ? { nombreTratamiento: modal.edit.nombreTratamiento, montoSugerido: modal.edit.montoSugerido ?? '' } : { nombreTratamiento: '', montoSugerido: '' }}
        onSave={async (values) => {
          setSaving(true);
          try {
            const monto = values.montoSugerido !== '' && values.montoSugerido != null ? Number(values.montoSugerido) : null;
            if (modal?.edit) {
              await api.catalogos.tratamientosPred.actualizar({ tratPredID: modal.edit.tratPredID, nombreTratamiento: values.nombreTratamiento, montoSugerido: monto });
              addToast('success', 'Tratamiento actualizado');
            } else {
              await api.catalogos.tratamientosPred.crear({ nombreTratamiento: values.nombreTratamiento, montoSugerido: monto });
              addToast('success', 'Tratamiento creado');
            }
            data.refetch();
            setModal(null);
          } catch (err) { addToast('error', err instanceof Error ? err.message : 'Error al guardar'); }
          finally { setSaving(false); }
        }}
        onCancel={() => setModal(null)}
        saving={saving}
      />
      <ConfirmDialog open={!!deleteTarget} title="Eliminar tratamiento" message={`Confirme que desea eliminar "${deleteTarget?.nombreTratamiento}".`} confirmLabel="Eliminar" variant="danger" onConfirm={async () => {
        if (!deleteTarget) return;
        try {
          await api.catalogos.tratamientosPred.eliminar(deleteTarget.tratPredID);
          addToast('success', 'Tratamiento eliminado');
          data.refetch();
        } catch (err) { addToast('error', err instanceof Error ? err.message : 'Error al eliminar'); }
        setDeleteTarget(null);
      }} onCancel={() => setDeleteTarget(null)} />
    </>
  );
}

function MaterialesDetalle({ tratPredID }: { tratPredID: number }) {
  const detalle = useApi(() => api.catalogos.tratamientosPred.materiales(tratPredID), [tratPredID]);
  const mats = detalle.data ?? [];

  return (
    <div style={{ padding: '8px 0' }}>
      {mats.length === 0 ? (
        <span className="text-muted text-sm">Sin materiales asignados</span>
      ) : (
        <div className="flex gap-8" style={{ flexWrap: 'wrap' }}>
          {mats.map((m) => (
            <span key={m.materialListPredID} className="mat-chip">
              Material #{m.materialID}
              <span className="chip-cant">{m.cantidad}</span>
            </span>
          ))}
        </div>
      )}
    </div>
  );
}

function TabTratamientosRealizados() {
  const data = useApi(() => api.tratamientos.activos());
  const [expanded, setExpanded] = useState<Set<number>>(new Set());
  const list = data.data ?? [];

  const columns: Column<Tratamiento>[] = [
    {
      key: 'chevron', header: '', width: 36, render: (r) => (
        <button className="btn btn-ghost btn-sm" onClick={() => setExpanded((prev) => { const n = new Set(prev); n.has(r.tratamientoID) ? n.delete(r.tratamientoID) : n.add(r.tratamientoID); return n; })}>
          <ChevronDown size={14} style={{ transform: expanded.has(r.tratamientoID) ? 'rotate(180deg)' : '', transition: 'transform 0.2s' }} />
        </button>
      ),
    },
    { key: 'id', header: 'ID', width: 50, render: (r) => <span className="num">{r.tratamientoID}</span> },
    { key: 'nombre', header: 'Tratamiento', render: (r) => r.nombreTratamiento },
    { key: 'fecha', header: 'Fecha', width: 100, render: (r) => r.fecha },
    { key: 'monto', header: 'Monto', width: 100, render: (r) => `S/ ${r.monto.toFixed(2)}` },
    {
      key: 'estado', header: 'Estado', width: 90, render: (r) => {
        const v = r.estado === 'CERRADO' ? 'success' : r.estado === 'ANULADO' ? 'danger' : 'info';
        return <Badge variant={v}>{r.estado}</Badge>;
      },
    },
  ];

  return (
    <>
      <CatalogoTabla columns={columns} data={list} loading={data.loading} searchEnabled={false} emptyTitle="Sin tratamientos" emptyText="No hay tratamientos registrados." />
      {list.map((item) => expanded.has(item.tratamientoID) && (
        <div key={`det-${item.tratamientoID}`} className="row-detail">
          <TratamientoMaterialesDetalle tratamientoID={item.tratamientoID} />
        </div>
      ))}
    </>
  );
}

function TratamientoMaterialesDetalle({ tratamientoID }: { tratamientoID: number }) {
  const detalle = useApi(() => api.tratamientos.materialesConNombre(tratamientoID), [tratamientoID]);
  const mats = detalle.data ?? [];

  return (
    <div style={{ padding: '8px 0' }}>
      {mats.length === 0 ? (
        <span className="text-muted text-sm">Sin materiales registrados</span>
      ) : (
        <div className="flex gap-8" style={{ flexWrap: 'wrap' }}>
          {mats.map((m) => (
            <span key={m.materialesListID} className="mat-chip">
              {m.nombreMaterial}
              <span className="chip-cant">{m.cantidad}</span>
            </span>
          ))}
        </div>
      )}
    </div>
  );
}
