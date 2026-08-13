import { useState } from "react";
import { useApi } from "../hooks/useApi";
import { useToast } from "../hooks/useToast";
import { api } from "../api";
import { CatalogoTabla, type Column } from "../components/CatalogoTabla";
import { CatalogoModal } from "../components/CatalogoModal";
import { ConfirmDialog } from "../components/ConfirmDialog";
import { Badge } from "../components/Badge";
import { Plus, ChevronDown, Trash2, Pencil } from "lucide-react";
import type {
  Materiales,
  Docente,
  Operador,
  Paciente,
  TratamientoPredefinido,
  Tratamiento,
} from "../api/types";
import { MaterialTable, type MaterialRow } from "../components/MaterialTable";

type TabId = "materiales" | "docentes" | "pacientes" | "operadores" | "tratamientos-pred" | "tratamientos-realizados";

const TABS: { id: TabId; label: string }[] = [
  { id: "materiales", label: "Materiales" },
  { id: "docentes", label: "Docentes" },
  { id: "pacientes", label: "Pacientes" },
  { id: "operadores", label: "Operadores" },
  { id: "tratamientos-pred", label: "Tratamientos Predef." },
  { id: "tratamientos-realizados", label: "Trat. Realizados" },
];

const TIPOS_PRE = ["3", "4", "5"];
const TIPOS_POS = ["R1", "R2", "R3"];

export default function Catalogos() {
  const [tab, setTab] = useState<TabId>("materiales");
  const { addToast } = useToast();

  return (
    <div>
      <div className="view-header">
        <h1 className="view-title">Catalogos</h1>
        <p className="subtitle">Gestion centralizada de catalogos del sistema</p>
      </div>

      <div className="card">
        <div className="tabs">
          {TABS.map((t) => (
            <button
              key={t.id}
              className={`tab ${tab === t.id ? "active" : ""}`}
              onClick={() => setTab(t.id)}
            >
              {t.label}
            </button>
          ))}
        </div>
        <div style={{ paddingTop: 16 }}>
          {tab === "materiales" && <TabMateriales addToast={addToast} />}
          {tab === "docentes" && <TabDocentes addToast={addToast} />}
          {tab === "pacientes" && <TabPacientes addToast={addToast} />}
          {tab === "operadores" && <TabOperadores addToast={addToast} />}
          {tab === "tratamientos-pred" && <TabTratamientosPred addToast={addToast} />}
          {tab === "tratamientos-realizados" && <TabTratamientosRealizados />}
        </div>
      </div>
    </div>
  );
}

function TabMateriales({ addToast }: { addToast: ReturnType<typeof useToast>["addToast"] }) {
  const [q, setQ] = useState("");
  const [modal, setModal] = useState<{ edit?: Materiales } | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<Materiales | null>(null);
  const [saving, setSaving] = useState(false);
  const data = useApi(() => api.catalogos.materiales.listar(q || undefined), [q]);
  const list = data.data ?? [];

  const columns: Column<Materiales>[] = [
    { key: "id", header: "ID", width: 60, render: (r) => <span className="num">{r.materialID}</span> },
    { key: "nombre", header: "Nombre", render: (r) => r.nombre },
    { key: "unidad", header: "Unidad", width: 120, render: (r) => r.unidad },
    { key: "estado", header: "Estado", width: 100, render: (r) => (
      <Badge variant={r.estado === 1 ? "success" : "neutral"}>{r.estado === 1 ? "Activo" : "Inactivo"}</Badge>
    )},
    { key: "acciones", header: "Acciones", width: 100, className: "text-center", render: (r) => (
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
          materialID: modal.edit.materialID, nombre: values.nombre, unidad: values.unidad,
          estado: Number(values.estado ?? modal.edit.estado),
        });
        addToast("success", "Material actualizado");
      } else {
        await api.catalogos.materiales.crear({ nombre: values.nombre, unidad: values.unidad });
        addToast("success", "Material creado");
      }
      data.refetch(); setModal(null);
    } catch (err) { addToast("error", err instanceof Error ? err.message : "Error al guardar"); }
    finally { setSaving(false); }
  };

  return (
    <>
      <div style={{ marginBottom: 12, display: "flex", justifyContent: "flex-end" }}>
        <button className="btn btn-primary btn-sm" onClick={() => setModal({})}><Plus size={14} /> Nuevo Material</button>
      </div>
      <CatalogoTabla columns={columns} data={list} loading={data.loading} searchPlaceholder="Buscar material..." onSearch={setQ} />
      <CatalogoModal key={modal?.edit?.materialID ?? 'new'} open={!!modal} title={modal?.edit ? "Editar Material" : "Nuevo Material"}
        fields={[
          { key: "nombre", label: "Nombre", type: "text", placeholder: "Nombre del material" },
          { key: "unidad", label: "Unidad de medida", type: "text", placeholder: "ej. Caja, Paquete, Unidad" },
          ...(modal?.edit ? [{ key: "estado", label: "Estado", type: "select" as const, options: [{ label: "Activo", value: "1" }, { label: "Inactivo", value: "0" }] }] : []),
        ]}
        initialValues={modal?.edit ? { nombre: modal.edit.nombre, unidad: modal.edit.unidad, estado: String(modal.edit.estado) } : { nombre: "", unidad: "" }}
        onSave={handleSave} onCancel={() => setModal(null)} saving={saving} />
      <ConfirmDialog open={!!deleteTarget} title="Eliminar material" message={`Confirme que desea eliminar "${deleteTarget?.nombre}".`}
        confirmLabel="Eliminar" variant="danger" onConfirm={async () => {
          if (!deleteTarget) return;
          try { await api.catalogos.materiales.eliminar(deleteTarget.materialID); addToast("success", "Material eliminado"); data.refetch(); }
          catch (err) { addToast("error", err instanceof Error ? err.message : "Error al eliminar"); }
          setDeleteTarget(null);
        }} onCancel={() => setDeleteTarget(null)} />
    </>
  );
}

function TabDocentes({ addToast }: { addToast: ReturnType<typeof useToast>["addToast"] }) {
  const [q, setQ] = useState("");
  const [modal, setModal] = useState<{ edit?: Docente } | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<Docente | null>(null);
  const [saving, setSaving] = useState(false);
  const data = useApi(() => api.catalogos.docentes.listar(q || undefined), [q]);
  const list = data.data ?? [];

  const columns: Column<Docente>[] = [
    { key: "id", header: "ID", width: 60, render: (r) => <span className="num">{r.docenteID}</span> },
    { key: "nombres", header: "Nombres", render: (r) => r.nombres },
    { key: "apellidos", header: "Apellidos", render: (r) => r.apellidos },
    { key: "telefono", header: "Telefono", width: 130, render: (r) => r.telefono },
    { key: "estado", header: "Estado", width: 100, render: (r) => (
      <Badge variant={r.estado === 1 ? "success" : "neutral"}>{r.estado === 1 ? "Activo" : "Inactivo"}</Badge>
    )},
    { key: "acciones", header: "Acciones", width: 100, className: "text-center", render: (r) => (
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
          docenteID: modal.edit.docenteID, nombres: values.nombres, apellidos: values.apellidos, telefono: values.telefono,
          estado: Number(values.estado ?? modal.edit.estado),
        });
        addToast("success", "Docente actualizado");
      } else {
        await api.catalogos.docentes.crear({ nombres: values.nombres, apellidos: values.apellidos, telefono: values.telefono });
        addToast("success", "Docente creado");
      }
      data.refetch(); setModal(null);
    } catch (err) { addToast("error", err instanceof Error ? err.message : "Error al guardar"); }
    finally { setSaving(false); }
  };

  return (
    <>
      <div style={{ marginBottom: 12, display: "flex", justifyContent: "flex-end" }}>
        <button className="btn btn-primary btn-sm" onClick={() => setModal({})}><Plus size={14} /> Nuevo Docente</button>
      </div>
      <CatalogoTabla columns={columns} data={list} loading={data.loading} searchPlaceholder="Buscar docente..." onSearch={setQ} />
      <CatalogoModal key={modal?.edit?.docenteID ?? 'new'} open={!!modal} title={modal?.edit ? "Editar Docente" : "Nuevo Docente"}
        fields={[
          { key: "nombres", label: "Nombres", type: "text" },
          { key: "apellidos", label: "Apellidos", type: "text" },
          { key: "telefono", label: "Telefono", type: "text" },
          ...(modal?.edit ? [{ key: "estado", label: "Estado", type: "select" as const, options: [{ label: "Activo", value: "1" }, { label: "Inactivo", value: "0" }] }] : []),
        ]}
        initialValues={modal?.edit ? { nombres: modal.edit.nombres, apellidos: modal.edit.apellidos, telefono: modal.edit.telefono, estado: String(modal.edit.estado) } : { nombres: "", apellidos: "", telefono: "" }}
        onSave={handleSave} onCancel={() => setModal(null)} saving={saving} />
      <ConfirmDialog open={!!deleteTarget} title="Eliminar docente"
        message={`Confirme que desea eliminar a "${deleteTarget?.nombres} ${deleteTarget?.apellidos}".`}
        confirmLabel="Eliminar" variant="danger" onConfirm={async () => {
          if (!deleteTarget) return;
          try { await api.catalogos.docentes.eliminar(deleteTarget.docenteID); addToast("success", "Docente eliminado"); data.refetch(); }
          catch (err) { addToast("error", err instanceof Error ? err.message : "Error al eliminar"); }
          setDeleteTarget(null);
        }} onCancel={() => setDeleteTarget(null)} />
    </>
  );
}

function TabPacientes({ addToast }: { addToast: ReturnType<typeof useToast>["addToast"] }) {
  const [q, setQ] = useState("");
  const [modal, setModal] = useState<{ edit?: Paciente } | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<Paciente | null>(null);
  const [saving, setSaving] = useState(false);
  const data = useApi(() => api.catalogos.pacientes.listar(q || undefined), [q]);
  const list = data.data ?? [];

  const columns: Column<Paciente>[] = [
    { key: "id", header: "ID", width: 60, render: (r) => <span className="num">{r.pacienteID}</span> },
    { key: "nombres", header: "Nombres", render: (r) => r.nombres },
    { key: "apellidos", header: "Apellidos", render: (r) => r.apellidos },
    { key: "estado", header: "Estado", width: 100, render: (r) => (
      <Badge variant={r.estado === 1 ? "success" : "neutral"}>{r.estado === 1 ? "Activo" : "Inactivo"}</Badge>
    )},
    { key: "acciones", header: "Acciones", width: 100, className: "text-center", render: (r) => (
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
        await api.catalogos.pacientes.actualizar({
          pacienteID: modal.edit.pacienteID, nombres: values.nombres, apellidos: values.apellidos,
          estado: Number(values.estado ?? modal.edit.estado),
        });
        addToast("success", "Paciente actualizado");
      } else {
        await api.catalogos.pacientes.crear({ nombres: values.nombres, apellidos: values.apellidos });
        addToast("success", "Paciente creado");
      }
      data.refetch(); setModal(null);
    } catch (err) { addToast("error", err instanceof Error ? err.message : "Error al guardar"); }
    finally { setSaving(false); }
  };

  return (
    <>
      <div style={{ marginBottom: 12, display: "flex", justifyContent: "flex-end" }}>
        <button className="btn btn-primary btn-sm" onClick={() => setModal({})}><Plus size={14} /> Nuevo Paciente</button>
      </div>
      <CatalogoTabla columns={columns} data={list} loading={data.loading} searchPlaceholder="Buscar paciente..." onSearch={setQ} />
      <CatalogoModal key={modal?.edit?.pacienteID ?? 'new'} open={!!modal} title={modal?.edit ? "Editar Paciente" : "Nuevo Paciente"}
        fields={[
          { key: "nombres", label: "Nombres", type: "text" },
          { key: "apellidos", label: "Apellidos", type: "text" },
          ...(modal?.edit ? [{ key: "estado", label: "Estado", type: "select" as const, options: [{ label: "Activo", value: "1" }, { label: "Inactivo", value: "0" }] }] : []),
        ]}
        initialValues={modal?.edit ? { nombres: modal.edit.nombres, apellidos: modal.edit.apellidos, estado: String(modal.edit.estado) } : { nombres: "", apellidos: "" }}
        onSave={handleSave} onCancel={() => setModal(null)} saving={saving} />
      <ConfirmDialog open={!!deleteTarget} title="Eliminar paciente"
        message={`Confirme que desea eliminar a "${deleteTarget?.nombres} ${deleteTarget?.apellidos}".`}
        confirmLabel="Eliminar" variant="danger" onConfirm={async () => {
          if (!deleteTarget) return;
          try { await api.catalogos.pacientes.eliminar(deleteTarget.pacienteID); addToast("success", "Paciente eliminado"); data.refetch(); }
          catch (err) { addToast("error", err instanceof Error ? err.message : "Error al eliminar"); }
          setDeleteTarget(null);
        }} onCancel={() => setDeleteTarget(null)} />
    </>
  );
}

function TabOperadores({ addToast }: { addToast: ReturnType<typeof useToast>["addToast"] }) {
  const [q, setQ] = useState("");
  const [modal, setModal] = useState<{ edit?: Operador } | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<Operador | null>(null);
  const [saving, setSaving] = useState(false);
  const data = useApi(() => api.catalogos.operadores.listar(q || undefined), [q]);
  const list = data.data ?? [];

  const columns: Column<Operador>[] = [
    { key: "id", header: "ID", width: 50, render: (r) => <span className="num">{r.operadorID}</span> },
    { key: "nombres", header: "Nombres", render: (r) => r.nombres },
    { key: "apellidos", header: "Apellidos", render: (r) => r.apellidos },
    { key: "dni", header: "DNI", width: 100, render: (r) => r.dni || "-" },
    { key: "grado", header: "Grado", width: 70, render: (r) => r.grado },
    { key: "tipo", header: "Tipo", width: 70, render: (r) => r.tipo },
    { key: "periodo", header: "Periodo", width: 80, render: (r) => <span className="num">{r.periodo}</span> },
    { key: "estado", header: "Estado", width: 90, render: (r) => (
      <Badge variant={r.estado === 1 ? "success" : "neutral"}>{r.estado === 1 ? "Activo" : "Inactivo"}</Badge>
    )},
    { key: "acciones", header: "", width: 80, className: "text-center", render: (r) => (
      <div className="flex gap-4 justify-center">
        <button className="btn btn-ghost btn-sm" onClick={() => setModal({ edit: r })}><Pencil size={14} /></button>
        <button className="btn btn-ghost btn-sm" onClick={() => setDeleteTarget(r)}><Trash2 size={14} /></button>
      </div>
    )},
  ];

  const handleSave = async (values: Record<string, any>) => {
    setSaving(true);
    try {
      const payload = { ...values, periodo: Number(values.periodo), estado: modal?.edit ? Number(values.estado ?? modal.edit.estado) : 1 };
      if (modal?.edit) {
        await api.catalogos.operadores.actualizar({ ...modal.edit, ...payload });
        addToast("success", "Operador actualizado");
      } else {
        await api.catalogos.operadores.crear(payload as any);
        addToast("success", "Operador creado");
      }
      data.refetch(); setModal(null);
    } catch (err) { addToast("error", err instanceof Error ? err.message : "Error al guardar"); }
    finally { setSaving(false); }
  };

  const openNew = () => { setModal({}); };

  return (
    <>
      <div style={{ marginBottom: 12, display: "flex", justifyContent: "flex-end" }}>
        <button className="btn btn-primary btn-sm" onClick={openNew}><Plus size={14} /> Nuevo Operador</button>
      </div>
      <CatalogoTabla columns={columns} data={list} loading={data.loading} searchPlaceholder="Buscar operador..." onSearch={setQ} />
      <CatalogoModal key={modal?.edit?.operadorID ?? 'new'} open={!!modal} title={modal?.edit ? "Editar Operador" : "Nuevo Operador"}
        fields={[
          { key: "nombres", label: "Nombres", type: "text" },
          { key: "apellidos", label: "Apellidos", type: "text" },
          { key: "dni", label: "DNI (opcional)", type: "text", placeholder: "Opcional" },
          { key: "grado", label: "Grado", type: "select", options: [{ label: "Pregrado (PRE)", value: "PRE" }, { label: "Posgrado (POS)", value: "POS" }], onFieldChange: (v, setField) => setField("tipo", v === "PRE" ? "3" : "R1") },
          { key: "tipo", label: "Tipo", type: "select", options: (values) => (values.grado === "PRE" ? TIPOS_PRE : values.grado === "POS" ? TIPOS_POS : []).map((v) => ({ label: v, value: v })) },
          { key: "periodo", label: "Periodo (ano)", type: "number", integer: true },
          ...(modal?.edit ? [{ key: "estado", label: "Estado", type: "select" as const, options: [{ label: "Activo", value: "1" }, { label: "Inactivo", value: "0" }] }] : []),
        ]}
        initialValues={modal?.edit ? {
          nombres: modal.edit.nombres, apellidos: modal.edit.apellidos, dni: modal.edit.dni || "",
          grado: modal.edit.grado, tipo: modal.edit.tipo, periodo: String(modal.edit.periodo), estado: String(modal.edit.estado),
        } : { nombres: "", apellidos: "", dni: "", grado: "", tipo: "", periodo: String(new Date().getFullYear()) }}
        onSave={handleSave} onCancel={() => setModal(null)} saving={saving} />
      <ConfirmDialog open={!!deleteTarget} title="Eliminar operador"
        message={`Confirme que desea eliminar a "${deleteTarget?.nombres} ${deleteTarget?.apellidos}".`}
        confirmLabel="Eliminar" variant="danger" onConfirm={async () => {
          if (!deleteTarget) return;
          try { await api.catalogos.operadores.eliminar(deleteTarget.operadorID); addToast("success", "Operador eliminado"); data.refetch(); }
          catch (err) { addToast("error", err instanceof Error ? err.message : "Error al eliminar"); }
          setDeleteTarget(null);
        }} onCancel={() => setDeleteTarget(null)} />
    </>
  );
}

function TabTratamientosPred({ addToast }: { addToast: ReturnType<typeof useToast>["addToast"] }) {
  const [q, setQ] = useState("");
  const [modal, setModal] = useState<{ edit?: TratamientoPredefinido } | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<TratamientoPredefinido | null>(null);
  const [expanded, setExpanded] = useState<Set<number>>(new Set());
  const [saving, setSaving] = useState(false);
  const [matRows, setMatRows] = useState<MaterialRow[]>([]);
  const mats = useApi(() => api.catalogos.materiales.listar());
  const data = useApi(() => api.catalogos.tratamientosPred.listar(q || undefined), [q]);
  const list = data.data ?? [];

  const columns: Column<TratamientoPredefinido>[] = [
    { key: "chevron", header: "", width: 36, render: (r) => (
      <button className="btn btn-ghost btn-sm" onClick={() => setExpanded((prev) => { const n = new Set(prev); n.has(r.tratPredID) ? n.delete(r.tratPredID) : n.add(r.tratPredID); return n; })}>
        <ChevronDown size={14} style={{ transform: expanded.has(r.tratPredID) ? "rotate(180deg)" : "", transition: "transform 0.18s" }} />
      </button>
    )},
    { key: "id", header: "ID", width: 50, render: (r) => <span className="num">{r.tratPredID}</span> },
    { key: "nombre", header: "Nombre", render: (r) => r.nombreTratamiento },
    { key: "costo", header: "Costo", width: 100, render: (r) => r.montoSugerido != null ? `S/ ${r.montoSugerido.toFixed(2)}` : "-" },
    { key: "estado", header: "Estado", width: 100, render: (r) => (
      <Badge variant={r.estado === 1 ? "success" : "neutral"}>{r.estado === 1 ? "Activo" : "Inactivo"}</Badge>
    )},
    { key: "acciones", header: "", width: 80, className: "text-center", render: (r) => (
      <div className="flex gap-4 justify-center">
        <button className="btn btn-ghost btn-sm" onClick={async () => {
          try {
            const matsData = await api.catalogos.tratamientosPred.materiales(r.tratPredID);
            setMatRows(matsData.map((m) => ({ key: `mat-${m.materialListPredID}`, materialId: m.materialID, nombreMaterial: "", cantidad: m.cantidad })));
          } catch { setMatRows([]); }
          setModal({ edit: r });
        }}><Pencil size={14} /></button>
        <button className="btn btn-ghost btn-sm" onClick={() => setDeleteTarget(r)}><Trash2 size={14} /></button>
      </div>
    )},
  ];

  const handleSave = async (values: Record<string, any>) => {
    setSaving(true);
    try {
      const monto = values.montoSugerido !== "" && values.montoSugerido != null ? Number(values.montoSugerido) : null;
      const estado = modal?.edit ? Number(values.estado ?? modal.edit.estado) : 1;
      let id: number;
      if (modal?.edit) {
        await api.catalogos.tratamientosPred.actualizar({ tratPredID: modal.edit.tratPredID, nombreTratamiento: values.nombreTratamiento, montoSugerido: monto, estado });
        id = modal.edit.tratPredID;
        addToast("success", "Tratamiento actualizado");
      } else {
        const result = await api.catalogos.tratamientosPred.crear({ nombreTratamiento: values.nombreTratamiento, montoSugerido: monto });
        id = result.id;
        addToast("success", "Tratamiento creado");
      }
      if (matRows.length > 0) {
        const matPayload = matRows.filter((r) => r.materialId != null).map((r) => ({ materialID: r.materialId!, cantidad: r.cantidad }));
        if (matPayload.length > 0) {
          await api.catalogos.tratamientosPred.guardarMateriales(id, matPayload);
        }
      }
      data.refetch();
      setModal(null);
      setMatRows([]);
    } catch (err) { addToast("error", err instanceof Error ? err.message : "Error al guardar"); }
    finally { setSaving(false); }
  };

  const handleAddMat = () => { setMatRows((prev) => [...prev, { key: `new-${Date.now()}`, materialId: null, nombreMaterial: "", cantidad: 0 }]); };
  const handleRemoveMat = (key: string) => { setMatRows((prev) => prev.filter((r) => r.key !== key)); };
  const handleMatChange = (key: string, materialId: number) => { setMatRows((prev) => prev.map((r) => r.key === key ? { ...r, materialId } : r)); };
  const handleCantChange = (key: string, cantidad: number) => { setMatRows((prev) => prev.map((r) => r.key === key ? { ...r, cantidad } : r)); };

  return (
    <>
      <div style={{ marginBottom: 12, display: "flex", justifyContent: "flex-end" }}>
        <button className="btn btn-primary btn-sm" onClick={() => { setMatRows([]); setModal({}); }}>
          <Plus size={14} /> Nuevo Tratamiento
        </button>
      </div>
      <CatalogoTabla columns={columns} data={list} loading={data.loading} searchPlaceholder="Buscar tratamiento..." onSearch={setQ} />
      {list.map((item) => expanded.has(item.tratPredID) && (
        <div key={`det-${item.tratPredID}`} className="row-detail">
          <MaterialesDetalle tratPredID={item.tratPredID} />
        </div>
      ))}
      <CatalogoModal key={modal?.edit?.tratPredID ?? 'new'} open={!!modal}
        title={modal?.edit ? "Editar Tratamiento Predefinido" : "Nuevo Tratamiento Predefinido"}
        fields={[
          { key: "nombreTratamiento", label: "Nombre del tratamiento", type: "text" },
          { key: "montoSugerido", label: "Monto sugerido", type: "number", placeholder: "0.00" },
          ...(modal?.edit ? [{ key: "estado", label: "Estado", type: "select" as const, options: [{ label: "Activo", value: "1" }, { label: "Inactivo", value: "0" }] }] : []),
        ]}
        initialValues={modal?.edit ? { nombreTratamiento: modal.edit.nombreTratamiento, montoSugerido: modal.edit.montoSugerido ?? "", estado: String(modal.edit.estado) } : { nombreTratamiento: "", montoSugerido: "" }}
        onSave={handleSave} onCancel={() => { setModal(null); setMatRows([]); }} saving={saving}
        width={560}
      >
        <div style={{ borderTop: '1px solid var(--color-border)', paddingTop: 14, marginTop: 4 }}>
          <h4 style={{ fontSize: 'var(--font-lg)', fontWeight: 600, marginBottom: 10, color: 'var(--color-text)' }}>Materiales sugeridos</h4>
          <MaterialTable rows={matRows} materials={mats.data ?? []} onAdd={handleAddMat} onRemove={handleRemoveMat} onMaterialChange={handleMatChange} onCantidadChange={handleCantChange} />
        </div>
      </CatalogoModal>
      <ConfirmDialog open={!!deleteTarget} title="Eliminar tratamiento"
        message={`Confirme que desea eliminar "${deleteTarget?.nombreTratamiento}".`}
        confirmLabel="Eliminar" variant="danger" onConfirm={async () => {
          if (!deleteTarget) return;
          try { await api.catalogos.tratamientosPred.eliminar(deleteTarget.tratPredID); addToast("success", "Tratamiento eliminado"); data.refetch(); }
          catch (err) { addToast("error", err instanceof Error ? err.message : "Error al eliminar"); }
          setDeleteTarget(null);
        }} onCancel={() => setDeleteTarget(null)} />
    </>
  );
}

function MaterialesDetalle({ tratPredID }: { tratPredID: number }) {
  const detalle = useApi(() => api.catalogos.tratamientosPred.materiales(tratPredID), [tratPredID]);
  const mats = detalle.data ?? [];
  return (
    <div style={{ padding: "8px 0" }}>
      {mats.length === 0 ? (
        <span className="mat-empty">Sin materiales asignados</span>
      ) : (
        <div className="flex gap-8" style={{ flexWrap: "wrap" }}>
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
  const data = useApi(() => api.tratamientos.cerrados());
  const [expanded, setExpanded] = useState<Set<number>>(new Set());
  const list = data.data ?? [];

  const columns: Column<Tratamiento>[] = [
    { key: "chevron", header: "", width: 36, render: (r) => (
      <button className="btn btn-ghost btn-sm" onClick={() => setExpanded((prev) => { const n = new Set(prev); n.has(r.tratamientoID) ? n.delete(r.tratamientoID) : n.add(r.tratamientoID); return n; })}>
        <ChevronDown size={14} style={{ transform: expanded.has(r.tratamientoID) ? "rotate(180deg)" : "", transition: "transform 0.18s" }} />
      </button>
    )},
    { key: "id", header: "ID", width: 50, render: (r) => <span className="num">{r.tratamientoID}</span> },
    { key: "nombre", header: "Tratamiento", render: (r) => r.nombreTratamiento },
    { key: "fecha", header: "Fecha", width: 100, render: (r) => r.fecha },
    { key: "monto", header: "Monto", width: 100, render: (r) => `S/ ${r.monto.toFixed(2)}` },
    { key: "estado", header: "Estado", width: 90, render: (r) => {
      const v = r.estado === "CERRADO" ? "success" : r.estado === "ANULADO" ? "danger" : "info";
      return <Badge variant={v}>{r.estado}</Badge>;
    }},
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
    <div style={{ padding: "8px 0" }}>
      {mats.length === 0 ? (
        <span className="mat-empty">Sin materiales registrados</span>
      ) : (
        <div className="flex gap-8" style={{ flexWrap: "wrap" }}>
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
