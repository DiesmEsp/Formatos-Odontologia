import { useState } from 'react';
import { X } from 'lucide-react';
import { useApi } from '../../hooks/useApi';
import { useToast } from '../../hooks/useToast';
import { api } from '../../api';
import { SearchableCombo, type SearchableOption } from '../../components/SearchableCombo';
import { CrearPacienteOnTheFly } from './CrearPacienteOnTheFly';
import { CrearOperadorOnTheFly } from './CrearOperadorOnTheFly';
import { nombreCompleto } from '../../lib/format';
import type { Tratamiento } from '../../api/types';

export function EditarTratamientoModal({
  tratamiento, onClose, onSuccess, addToast,
}: {
  tratamiento: Tratamiento;
  onClose: () => void;
  onSuccess: () => void;
  addToast: ReturnType<typeof useToast>['addToast'];
}) {
  const [nombre, setNombre] = useState<string>(tratamiento.nombreTratamiento);
  const [montoStr, setMontoStr] = useState<string>(tratamiento.monto ? String(tratamiento.monto) : '');
  const [fecha, setFecha] = useState<string>(tratamiento.fecha);
  const [operadorId, setOperadorId] = useState<number | null>(tratamiento.operadorID);
  const [pacienteId, setPacienteId] = useState<number | null>(tratamiento.pacienteID);
  const [qOpe, setQOpe] = useState('');
  const [qPac, setQPac] = useState('');
  const [showNewPaciente, setShowNewPaciente] = useState(false);
  const [showNewOperador, setShowNewOperador] = useState(false);
  const [saving, setSaving] = useState(false);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);

  const pacientes = useApi(() => api.catalogos.pacientes.listar(qPac || undefined), [qPac]);
  const operadores = useApi(() => api.catalogos.operadores.listar(qOpe || undefined), [qOpe]);

  const pOptions: SearchableOption[] = (pacientes.data ?? []).map((p) => ({
    id: p.pacienteID,
    label: nombreCompleto(p.nombres, p.apellidos),
  }));
  const oOptions: SearchableOption[] = (operadores.data ?? []).map((o) => ({
    id: o.operadorID,
    label: nombreCompleto(o.nombres, o.apellidos),
    badge: o.grado,
  }));

  const esContinuo = tratamiento.tipo === 'CONTINUO';
  const esCerrado = tratamiento.estado === 'CERRADO';

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

  const handleGuardar = async () => {
    setErrorMsg(null);
    const nombreTrim = nombre.trim();
    if (!nombreTrim) {
      setErrorMsg('El nombre del tratamiento no puede estar vacío.');
      return;
    }
    if (!operadorId || !pacienteId) {
      setErrorMsg('Seleccione paciente y operador.');
      return;
    }
    if (!/^\d{4}-\d{2}-\d{2}$/.test(fecha)) {
      setErrorMsg('La fecha debe tener el formato AAAA-MM-DD.');
      return;
    }
    const montoVal = esContinuo ? null : (montoStr === '' ? null : Number(montoStr));
    if (!esContinuo && montoVal != null && montoVal < 0) {
      setErrorMsg('El monto no puede ser negativo.');
      return;
    }

    setSaving(true);
    try {
      const dto = {
        nombreTratamiento: nombreTrim,
        monto: montoVal,
        fecha,
        operadorID: operadorId,
        pacienteID: pacienteId,
      };
      if (esCerrado) {
        const retroDto = {
          tipo: tratamiento.tipo,
          monto: montoVal,
          montoPagado: null,
          estadoPago: null,
          fecha,
          nombreTratamiento: nombreTrim,
          operadorID: operadorId,
          pacienteID: pacienteId,
          cantidadesMateriales: {},
        };
        await api.tratamientos.editarRetroactivo(tratamiento.tratamientoID, retroDto);
      } else {
        await api.tratamientos.editarEnCurso(tratamiento.tratamientoID, dto);
      }
      addToast('success', 'Tratamiento actualizado correctamente');
      onSuccess();
    } catch (err) {
      setErrorMsg(err instanceof Error ? err.message : 'Error al actualizar el tratamiento');
    } finally {
      setSaving(false);
    }
  };

  return (
    <>
      <div className="dialog-overlay" onClick={onClose}>
        <div className="dialog-pane mw-560" onClick={(e) => e.stopPropagation()}>
          <div className="dialog-header">
            <h3 className="dialog-title">Editar Tratamiento #{tratamiento.tratamientoID}</h3>
            <button className="btn btn-ghost btn-sm" onClick={onClose}><X size={18} /></button>
          </div>
          <div className="dialog-body">
            <div className="form-group">
              <label className="form-label">Nombre del tratamiento *</label>
              <input
                type="text"
                className="text-field w-full"
                value={nombre}
                onChange={(e) => setNombre(e.target.value)}
                placeholder="Ej. Limpieza dental"
              />
            </div>

            <div className="form-group">
              <label className="form-label">Monto {esContinuo && '(no editable para CONTINUO)'}</label>
              <input
                type="text"
                inputMode="decimal"
                className="text-field w-full"
                value={montoStr}
                onChange={(e) => setMontoStr(filterNumeric(e.target.value))}
                onKeyDown={handleNumericKeyDown}
                disabled={esContinuo}
                placeholder="0.00"
              />
            </div>

            <div className="form-group">
              <label className="form-label">Fecha *</label>
              <input
                type="date"
                className="text-field w-full"
                value={fecha}
                onChange={(e) => setFecha(e.target.value)}
              />
            </div>

            <div className="form-group">
              <label className="form-label">Operador *</label>
              <SearchableCombo
                options={oOptions}
                value={operadorId}
                onChange={setOperadorId}
                onSearch={setQOpe}
                placeholder="Buscar operador..."
              />
              <button className="btn btn-ghost btn-sm btn-inline-add" onClick={() => setShowNewOperador(true)}>+ Nuevo operador</button>
            </div>

            <div className="form-group">
              <label className="form-label">Paciente *</label>
              <SearchableCombo
                options={pOptions}
                value={pacienteId}
                onChange={setPacienteId}
                onSearch={setQPac}
                placeholder="Buscar paciente..."
              />
              <button className="btn btn-ghost btn-sm btn-inline-add" onClick={() => setShowNewPaciente(true)}>+ Nuevo paciente</button>
            </div>

            {errorMsg && (
              <div className="alert-banner alert-danger mb-16">{errorMsg}</div>
            )}
          </div>
          <div className="dialog-footer">
            <button className="btn btn-secondary" onClick={onClose}>Cancelar</button>
            <button className="btn btn-primary" onClick={handleGuardar} disabled={saving}>
              {saving ? 'Guardando...' : 'Guardar cambios'}
            </button>
          </div>
        </div>
      </div>

      {showNewPaciente && (
        <CrearPacienteOnTheFly
          onClose={() => setShowNewPaciente(false)}
          onCreated={(id) => { setPacienteId(id); setShowNewPaciente(false); pacientes.refetch(); }}
          addToast={addToast}
        />
      )}
      {showNewOperador && (
        <CrearOperadorOnTheFly
          onClose={() => setShowNewOperador(false)}
          onCreated={(id) => { setOperadorId(id); setShowNewOperador(false); operadores.refetch(); }}
          addToast={addToast}
        />
      )}
    </>
  );
}
