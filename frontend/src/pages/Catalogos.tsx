import { useState, useEffect } from "react";
import { useApi } from "../hooks/useApi";
import { useToast } from "../hooks/useToast";
import { api } from "../api";
import { CatalogoTabla, type Column } from "../components/CatalogoTabla";
import { CatalogoModal } from "../components/CatalogoModal";
import { ConfirmDialog } from "../components/ConfirmDialog";
import { RegistrarPagoModal } from "../components/RegistrarPagoModal";
import { Badge } from "../components/Badge";
import { Plus, ChevronDown, Trash2, Pencil, X, DollarSign } from "lucide-react";
import type {
  Materiales,
  Docente,
  Operador,
  Paciente,
  TratamientoPredefinido,
  Tratamiento,
} from "../api/types";
import { MaterialTable, type MaterialRow } from "../components/MaterialTable";
import { SearchableCombo, type SearchableOption } from "../components/SearchableCombo";
import { hoyISO, nombreCompleto } from "../lib/format";

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
    { key: "id", header: "ID", width: 60, render: (r) => <span className="num">{r.materialID}</span>, sortValue: (r) => r.materialID },
    { key: "nombre", header: "Nombre", render: (r) => r.nombre, sortValue: (r) => r.nombre },
    { key: "unidad", header: "Unidad", width: 120, render: (r) => r.unidad, sortValue: (r) => r.unidad },
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
    { key: "id", header: "ID", width: 60, render: (r) => <span className="num">{r.docenteID}</span>, sortValue: (r) => r.docenteID },
    { key: "nombres", header: "Nombres", render: (r) => r.nombres, sortValue: (r) => r.nombres },
    { key: "apellidos", header: "Apellidos", render: (r) => r.apellidos, sortValue: (r) => r.apellidos },
    { key: "telefono", header: "Telefono", width: 130, render: (r) => r.telefono, sortValue: (r) => r.telefono },
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
    { key: "id", header: "ID", width: 60, render: (r) => <span className="num">{r.pacienteID}</span>, sortValue: (r) => r.pacienteID },
    { key: "nombres", header: "Nombres", render: (r) => r.nombres, sortValue: (r) => r.nombres },
    { key: "apellidos", header: "Apellidos", render: (r) => r.apellidos, sortValue: (r) => r.apellidos },
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
    { key: "id", header: "ID", width: 50, render: (r) => <span className="num">{r.operadorID}</span>, sortValue: (r) => r.operadorID },
    { key: "nombres", header: "Nombres", render: (r) => r.nombres, sortValue: (r) => r.nombres },
    { key: "apellidos", header: "Apellidos", render: (r) => r.apellidos, sortValue: (r) => r.apellidos },
    { key: "dni", header: "DNI", width: 100, render: (r) => r.dni || "-", sortValue: (r) => r.dni ?? "" },
    { key: "grado", header: "Grado", width: 70, render: (r) => r.grado, sortValue: (r) => r.grado },
    { key: "tipo", header: "Tipo", width: 70, render: (r) => r.tipo, sortValue: (r) => r.tipo },
    { key: "periodo", header: "Periodo", width: 80, render: (r) => <span className="num">{r.periodo}</span>, sortValue: (r) => r.periodo },
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
    { key: "id", header: "ID", width: 50, render: (r) => <span className="num">{r.tratPredID}</span>, sortValue: (r) => r.tratPredID },
    { key: "nombre", header: "Nombre", render: (r) => r.nombreTratamiento, sortValue: (r) => r.nombreTratamiento },
    { key: "costo", header: "Costo", width: 100, render: (r) => r.montoSugerido != null ? `S/ ${r.montoSugerido.toFixed(2)}` : "-", sortValue: (r) => r.montoSugerido ?? 0 },
    { key: "estado", header: "Estado", width: 100, render: (r) => (
      <Badge variant={r.estado === 1 ? "success" : "neutral"}>{r.estado === 1 ? "Activo" : "Inactivo"}</Badge>
    )},
    { key: "acciones", header: "", width: 80, className: "text-center", render: (r) => (
      <div className="flex gap-4 justify-center">
        <button className="btn btn-ghost btn-sm" onClick={async () => {
          try {
            const matsData = await api.catalogos.tratamientosPred.materiales(r.tratPredID);
            setMatRows(matsData.map((m) => ({ key: `mat-${m.materialID}`, materialId: m.materialID, nombreMaterial: m.nombreMaterial, cantidad: m.cantidad })));
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
      <CatalogoTabla columns={columns} data={list} loading={data.loading} searchPlaceholder="Buscar tratamiento..." onSearch={setQ}
        rowKey={(r) => r.tratPredID} renderDetail={(r) => <MaterialesDetalle tratPredID={r.tratPredID} />} expanded={expanded} />
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
    <div style={{ padding: "4px 0" }}>
      {mats.length === 0 ? (
        <span className="mat-empty">Sin materiales asignados</span>
      ) : (
        <ul className="material-list">
          {mats.map((m) => (
            <li key={m.materialID} className="material-list-item">
              <span className="material-list-name">{m.nombreMaterial}</span>
              <span className="material-list-cant">{m.cantidad}</span>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}

function TabTratamientosRealizados() {
  const data = useApi(() => api.tratamientos.cerrados());
  const [expanded, setExpanded] = useState<Set<number>>(new Set());
  const [registrarOpen, setRegistrarOpen] = useState(false);
  const [editarTarget, setEditarTarget] = useState<Tratamiento | null>(null);
  const [pagoTarget, setPagoTarget] = useState<Tratamiento | null>(null);
  const [anularTarget, setAnularTarget] = useState<Tratamiento | null>(null);
  const { addToast } = useToast();
  const list = data.data ?? [];

  const columns: Column<Tratamiento>[] = [
    { key: "chevron", header: "", width: 36, render: (r) => (
      <button className="btn btn-ghost btn-sm" onClick={() => setExpanded((prev) => { const n = new Set(prev); n.has(r.tratamientoID) ? n.delete(r.tratamientoID) : n.add(r.tratamientoID); return n; })}>
        <ChevronDown size={14} style={{ transform: expanded.has(r.tratamientoID) ? "rotate(180deg)" : "", transition: "transform 0.18s" }} />
      </button>
    )},
    { key: "id", header: "ID", width: 50, render: (r) => <span className="num">{r.tratamientoID}</span>, sortValue: (r) => r.tratamientoID },
    { key: "nombre", header: "Tratamiento", render: (r) => r.nombreTratamiento, sortValue: (r) => r.nombreTratamiento },
    { key: "fecha", header: "Fecha", width: 100, render: (r) => r.fecha, sortValue: (r) => r.fecha },
    { key: "monto", header: "Monto", width: 100, render: (r) => `S/ ${r.monto.toFixed(2)}`, sortValue: (r) => r.monto },
    { key: "estado", header: "Estado", width: 90, render: (r) => {
      const v = r.estado === "CERRADO" ? "success" : r.estado === "ANULADO" ? "danger" : "info";
      return <Badge variant={v}>{r.estado}</Badge>;
    }},
    { key: "acciones", header: "", width: 120, className: "text-center", render: (r) => (
      <div className="flex gap-4 justify-center">
        <button type="button" className="btn btn-ghost btn-sm" title="Editar" onClick={() => setEditarTarget(r)}><Pencil size={14} /></button>
        <button type="button" className="btn btn-ghost btn-sm" title="Registrar pago" onClick={() => setPagoTarget(r)}><DollarSign size={14} /></button>
        <button type="button" className="btn btn-ghost btn-sm" title="Anular" onClick={() => setAnularTarget(r)}><Trash2 size={14} /></button>
      </div>
    )},
  ];

  return (
    <>
      <div style={{ marginBottom: 12, display: "flex", justifyContent: "flex-end" }}>
        <button className="btn btn-primary btn-sm" onClick={() => setRegistrarOpen(true)}><Plus size={14} /> Registrar tratamiento realizado</button>
      </div>
      <CatalogoTabla columns={columns} data={list} loading={data.loading} searchEnabled={false} emptyTitle="Sin tratamientos" emptyText="No hay tratamientos registrados."
        rowKey={(r) => r.tratamientoID} renderDetail={(r) => <TratamientoMaterialesDetalle tratamientoID={r.tratamientoID} />} expanded={expanded} />
      {registrarOpen && <RegistrarRealizadoModal onClose={() => setRegistrarOpen(false)} onSuccess={() => { setRegistrarOpen(false); data.refetch(); }} addToast={addToast} />}
      {editarTarget && <EditarRealizadoModal tratamiento={editarTarget} onClose={() => setEditarTarget(null)} onSuccess={() => { setEditarTarget(null); data.refetch(); }} addToast={addToast} />}
      {pagoTarget && <RegistrarPagoModal tratamiento={pagoTarget} onClose={() => setPagoTarget(null)} onSuccess={() => { setPagoTarget(null); data.refetch(); }} addToast={addToast} />}
      <ConfirmDialog open={!!anularTarget} title="Anular tratamiento" message={`Confirme que desea anular el tratamiento #${anularTarget?.tratamientoID}.`} confirmLabel="Sí, anular" variant="danger" requireMotivo
        onConfirm={async (motivo) => {
          if (!anularTarget || !motivo) return;
          try { await api.tratamientos.anular(anularTarget.tratamientoID, motivo); addToast("success", "Tratamiento anulado"); data.refetch(); }
          catch (err) { addToast("error", err instanceof Error ? err.message : "Error al anular"); }
          setAnularTarget(null);
        }}
        onCancel={() => setAnularTarget(null)} />
    </>
  );
}

function TratamientoMaterialesDetalle({ tratamientoID }: { tratamientoID: number }) {
  const consolidado = useApi(() => api.tratamientos.consolidado(tratamientoID), [tratamientoID]);
  const avances = useApi(() => api.tratamientos.avances(tratamientoID), [tratamientoID]);
  const data = consolidado.data;
  const avanceList = avances.data ?? [];
  return (
    <div style={{ padding: "4px 0" }}>
      {data && data.materiales.length > 0 && (
        <div style={{ marginBottom: 8 }}>
          <span style={{ fontSize: '0.8rem', fontWeight: 600, color: 'var(--text-secondary)' }}>Materiales consolidados</span>
          <ul className="material-list">
            {data.materiales.map((m) => (
              <li key={m.materialID} className="material-list-item">
                <span className="material-list-name">{m.nombreMaterial}</span>
                <span className="material-list-cant">{m.cantidad} {m.unidad}</span>
              </li>
            ))}
          </ul>
        </div>
      )}
      {avanceList.length > 0 && (
        <div>
          <span style={{ fontSize: '0.8rem', fontWeight: 600, color: 'var(--text-secondary)' }}>Avances</span>
          <ul className="material-list">
            {avanceList.map((a) => (
              <li key={a.avanceID} className="material-list-item">
                <span className="material-list-name">#{a.avanceID} - {a.fecha}</span>
                <Badge variant={a.estado === 'ANULADO' ? 'danger' : a.estado === 'TERMINADO' ? 'success' : 'info'}>{a.estado}</Badge>
              </li>
            ))}
          </ul>
        </div>
      )}
      {(!data || data.materiales.length === 0) && avanceList.length === 0 && (
        <span className="mat-empty">Sin materiales ni avances registrados</span>
      )}
    </div>
  );
}

function RegistrarRealizadoModal({ onClose, onSuccess, addToast }: { onClose: () => void; onSuccess: () => void; addToast: ReturnType<typeof useToast>["addToast"] }) {
  const [fecha, setFecha] = useState(hoyISO());
  const [pacienteId, setPacienteId] = useState<number | null>(null);
  const [operadorId, setOperadorId] = useState<number | null>(null);
  const [tratPredId, setTratPredId] = useState<number | null>(null);
  const [montoStr, setMontoStr] = useState("");
  const [tipo, setTipo] = useState("NORMAL");
  const [matRows, setMatRows] = useState<MaterialRow[]>([]);
  const [saving, setSaving] = useState(false);
  const [qPac, setQPac] = useState("");
  const [qOpe, setQOpe] = useState("");
  const [qTrat, setQTrat] = useState("");

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
      if (tp?.montoSugerido != null && tipo !== "CONTINUO") setMontoStr(String(tp.montoSugerido));
      try {
        const mats = await api.catalogos.tratamientosPred.materiales(id);
        setMatRows(mats.map((m, i) => ({ key: `pred-${i}-${m.materialID}`, materialId: m.materialID, nombreMaterial: m.nombreMaterial, cantidad: m.cantidad })));
      } catch { setMatRows([]); }
    }
  };

  const handleGuardar = async () => {
    if (!operadorId || !pacienteId) { addToast("error", "Seleccione paciente y operador"); return; }
    setSaving(true);
    try {
      const materialesMap: Record<string, number> = {};
      for (const r of matRows) {
        if (r.materialId == null) continue;
        materialesMap[String(r.materialId)] = r.cantidad > 0 ? r.cantidad : 1;
      }
      await api.tratamientos.registrarCerrado({
        operadorID: operadorId,
        pacienteID: pacienteId,
        fecha,
        tratPredID: tratPredId,
        monto: tipo === "CONTINUO" ? null : (montoStr === "" ? null : Number(montoStr)),
        tipo,
        materiales: materialesMap,
      });
      addToast("success", "Tratamiento registrado correctamente");
      onSuccess();
    } catch (err) { addToast("error", err instanceof Error ? err.message : "Error al registrar"); }
    finally { setSaving(false); }
  };

  return (
    <div className="dialog-overlay" onClick={onClose}>
      <div className="dialog-pane mw-560" onClick={(e) => e.stopPropagation()}>
        <div className="dialog-header">
          <h3 className="dialog-title">Registrar tratamiento realizado</h3>
          <button className="btn btn-ghost btn-sm" onClick={onClose}><X size={18} /></button>
        </div>
        <div className="dialog-body">
          <div className="form-group"><label className="form-label">Fecha</label><input type="date" className="text-field w-full" value={fecha} onChange={(e) => setFecha(e.target.value)} /></div>
          <div className="form-group"><label className="form-label">Paciente</label><SearchableCombo options={pOptions} value={pacienteId} onChange={setPacienteId} onSearch={setQPac} placeholder="Buscar paciente..." /></div>
          <div className="form-group"><label className="form-label">Operador</label><SearchableCombo options={oOptions} value={operadorId} onChange={setOperadorId} onSearch={setQOpe} placeholder="Buscar operador..." /></div>
          <div className="form-group"><label className="form-label">Tipo de tratamiento</label><SearchableCombo options={tOptions} value={tratPredId} onChange={handleTratChange} onSearch={setQTrat} placeholder="Buscar tratamiento..." /></div>
          <div className="form-row">
            <div className="form-group">
              <label className="form-label">Tipo</label>
              <select className="combo-box w-full" value={tipo} onChange={(e) => setTipo(e.target.value)}>
                <option value="NORMAL">Normal</option>
                <option value="CONTINUO">Continuo</option>
              </select>
            </div>
            <div className="form-group">
              <label className="form-label">Monto total</label>
              <input type="text" inputMode="decimal" className="text-field w-full" value={montoStr} onChange={(e) => setMontoStr(e.target.value.replace(/[^0-9.]/g, ""))} disabled={tipo === "CONTINUO"} placeholder="0.00" />
            </div>
          </div>
          <div style={{ borderTop: '1px solid var(--color-border)', paddingTop: 14, marginTop: 4 }}>
            <h4 style={{ fontSize: 'var(--font-lg)', fontWeight: 600, marginBottom: 10, color: 'var(--color-text)' }}>Materiales</h4>
            <MaterialTable
              rows={matRows}
              materials={materiales.data ?? []}
              onAdd={() => setMatRows((prev) => [...prev, { key: `new-${Date.now()}`, materialId: null, nombreMaterial: "", cantidad: 0 }])}
              onRemove={(key) => setMatRows((prev) => prev.filter((r) => r.key !== key))}
              onMaterialChange={(key, materialId) => setMatRows((prev) => prev.map((r) => r.key === key ? { ...r, materialId } : r))}
              onCantidadChange={(key, cantidad) => setMatRows((prev) => prev.map((r) => r.key === key ? { ...r, cantidad } : r))}
            />
          </div>
        </div>
        <div className="dialog-footer">
          <button className="btn btn-secondary" onClick={onClose}>Cancelar</button>
          <button className="btn btn-primary" onClick={handleGuardar} disabled={saving}>{saving ? "Guardando..." : "Registrar"}</button>
        </div>
      </div>
    </div>
  );
}

function EditarRealizadoModal({ tratamiento, onClose, onSuccess, addToast }: { tratamiento: Tratamiento; onClose: () => void; onSuccess: () => void; addToast: ReturnType<typeof useToast>["addToast"] }) {
  const [fecha, setFecha] = useState(tratamiento.fecha);
  const [nombre, setNombre] = useState(tratamiento.nombreTratamiento);
  const [montoStr, setMontoStr] = useState(String(tratamiento.monto));
  const [tipo, setTipo] = useState(tratamiento.tipo);
  const [pacienteId, setPacienteId] = useState<number | null>(tratamiento.pacienteID);
  const [operadorId, setOperadorId] = useState<number | null>(tratamiento.operadorID);
  const [matRows, setMatRows] = useState<MaterialRow[]>([]);
  const [originalMat, setOriginalMat] = useState<MaterialRow[]>([]);
  const [saving, setSaving] = useState(false);

  const pacientes = useApi(() => api.catalogos.pacientes.listar());
  const operadores = useApi(() => api.catalogos.operadores.listar());
  const materiales = useApi(() => api.catalogos.materiales.listar());

  useEffect(() => {
    (async () => {
      try {
        const mats = await api.tratamientos.materialesConNombre(tratamiento.tratamientoID);
        const rows = mats.map((m) => ({ key: `mat-${m.materialesListID}`, materialId: m.materialID, nombreMaterial: m.nombreMaterial, cantidad: m.cantidad }));
        setMatRows(rows);
        setOriginalMat(rows);
      } catch {
        setMatRows([]);
        setOriginalMat([]);
      }
    })();
  }, [tratamiento.tratamientoID]);

  const pOptions: SearchableOption[] = (pacientes.data ?? []).map((p) => ({ id: p.pacienteID, label: nombreCompleto(p.nombres, p.apellidos) }));
  const oOptions: SearchableOption[] = (operadores.data ?? []).map((o) => ({ id: o.operadorID, label: nombreCompleto(o.nombres, o.apellidos), badge: o.grado }));

  const handleGuardar = async () => {
    if (!operadorId || !pacienteId) { addToast("error", "Seleccione paciente y operador"); return; }
    setSaving(true);
    try {
      const cantidades: Record<string, number> = {};
      const currentIds = new Set(matRows.filter((r) => r.materialId != null).map((r) => r.materialId));
      for (const orig of originalMat) {
        if (orig.materialId != null && !currentIds.has(orig.materialId)) {
          cantidades[String(orig.materialId)] = 0;
        }
      }
      for (const r of matRows) {
        if (r.materialId == null) continue;
        cantidades[String(r.materialId)] = r.cantidad > 0 ? r.cantidad : 1;
      }
      await api.tratamientos.editarRetroactivo(tratamiento.tratamientoID, {
        tipo,
        monto: tipo === "CONTINUO" ? null : (montoStr === "" ? null : Number(montoStr)),
        montoPagado: null,
        estadoPago: null,
        fecha,
        nombreTratamiento: nombre,
        operadorID: operadorId,
        pacienteID: pacienteId,
        cantidadesMateriales: cantidades,
      });
      addToast("success", "Tratamiento actualizado");
      onSuccess();
    } catch (err) { addToast("error", err instanceof Error ? err.message : "Error al guardar"); }
    finally { setSaving(false); }
  };

  return (
    <div className="dialog-overlay" onClick={onClose}>
      <div className="dialog-pane mw-560" onClick={(e) => e.stopPropagation()}>
        <div className="dialog-header">
          <h3 className="dialog-title">Editar Tratamiento #{tratamiento.tratamientoID}</h3>
          <button className="btn btn-ghost btn-sm" onClick={onClose}><X size={18} /></button>
        </div>
        <div className="dialog-body">
          <div className="form-group"><label className="form-label">Nombre del tratamiento</label><input className="text-field w-full" value={nombre} onChange={(e) => setNombre(e.target.value)} /></div>
          <div className="form-row">
            <div className="form-group"><label className="form-label">Fecha</label><input type="date" className="text-field w-full" value={fecha} onChange={(e) => setFecha(e.target.value)} /></div>
            <div className="form-group">
              <label className="form-label">Tipo</label>
              <select className="combo-box w-full" value={tipo} onChange={(e) => setTipo(e.target.value)}>
                <option value="NORMAL">Normal</option>
                <option value="CONTINUO">Continuo</option>
              </select>
            </div>
          </div>
          <div className="form-group"><label className="form-label">Monto total</label><input type="text" inputMode="decimal" className="text-field w-full" value={montoStr} onChange={(e) => setMontoStr(e.target.value.replace(/[^0-9.]/g, ""))} disabled={tipo === "CONTINUO"} placeholder="0.00" /></div>
          <div className="form-group"><label className="form-label">Paciente</label><SearchableCombo options={pOptions} value={pacienteId} onChange={setPacienteId} placeholder="Buscar paciente..." /></div>
          <div className="form-group"><label className="form-label">Operador</label><SearchableCombo options={oOptions} value={operadorId} onChange={setOperadorId} placeholder="Buscar operador..." /></div>
          <div style={{ borderTop: '1px solid var(--color-border)', paddingTop: 14, marginTop: 4 }}>
            <h4 style={{ fontSize: 'var(--font-lg)', fontWeight: 600, marginBottom: 10, color: 'var(--color-text)' }}>Materiales</h4>
            <MaterialTable
              rows={matRows}
              materials={materiales.data ?? []}
              onAdd={() => setMatRows((prev) => [...prev, { key: `new-${Date.now()}`, materialId: null, nombreMaterial: "", cantidad: 0 }])}
              onRemove={(key) => setMatRows((prev) => prev.filter((r) => r.key !== key))}
              onMaterialChange={(key, materialId) => setMatRows((prev) => prev.map((r) => r.key === key ? { ...r, materialId } : r))}
              onCantidadChange={(key, cantidad) => setMatRows((prev) => prev.map((r) => r.key === key ? { ...r, cantidad } : r))}
            />
          </div>
        </div>
        <div className="dialog-footer">
          <button className="btn btn-secondary" onClick={onClose}>Cancelar</button>
          <button className="btn btn-primary" onClick={handleGuardar} disabled={saving}>{saving ? "Guardando..." : "Guardar"}</button>
        </div>
      </div>
    </div>
  );
}

