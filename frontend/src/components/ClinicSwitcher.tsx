import { useRef, useState } from 'react';
import { Building2, Check, ChevronDown } from 'lucide-react';
import { api } from '../api';
import { useApi } from '../hooks/useApi';
import { useClinica } from '../contexts/ClinicaContext';

export function ClinicSwitcher() {
  const { clinica, seleccionarClinica } = useClinica();
  const [abierto, setAbierto] = useState(false);
  const rootRef = useRef<HTMLDivElement>(null);

  const { data: clinicas } = useApi(() => api.clinicas.listar(), [abierto]);

  const cambiar = (clinicaID: number, nombre: string, grupo?: string | null) => {
    seleccionarClinica({ clinicaID, nombre, grupo });
    setAbierto(false);
  };

  if (!clinica) return null;

  return (
    <div className="clinic-switcher" ref={rootRef}>
      <button
        className="clinic-switcher-trigger"
        onClick={() => setAbierto((v) => !v)}
        aria-haspopup="listbox"
        aria-expanded={abierto}
      >
        <Building2 size={15} />
        <span className="clinic-switcher-nombre">{clinica.nombre}</span>
        <ChevronDown size={14} />
      </button>

      {abierto && (
        <ul className="clinic-switcher-menu" role="listbox">
          {(clinicas ?? []).map((c) => (
            <li key={c.clinicaID}>
              <button
                className={`clinic-switcher-option ${c.nombre === clinica.nombre ? 'selected' : ''}`}
                onClick={() => cambiar(c.clinicaID, c.nombre, c.grupo)}
                role="option"
                aria-selected={c.nombre === clinica.nombre}
              >
                <span className="clinic-switcher-option-text">
                  <span>{c.nombre}</span>
                  {c.grupo && <span className="clinic-switcher-grupo">{c.grupo}</span>}
                </span>
                {c.nombre === clinica.nombre && <Check size={15} />}
              </button>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}