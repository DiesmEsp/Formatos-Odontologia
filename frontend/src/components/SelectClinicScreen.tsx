import { useState } from 'react';
import { Building2, Plus, RefreshCw } from 'lucide-react';
import { api } from '../api';
import { useApi } from '../hooks/useApi';
import { useToast } from '../hooks/useToast';
import { useClinica } from '../contexts/ClinicaContext';
import type { Clinica } from '../api/types';
import { ToothMark } from './ToothMark';

export function SelectClinicScreen() {
  const { seleccionarClinica } = useClinica();
  const { addToast } = useToast();
  const [nombre, setNombre] = useState('');
  const [grupo, setGrupo] = useState('');
  const [creando, setCreando] = useState(false);

  const { data: clinicas, loading, error, refetch } = useApi(() => api.clinicas.listar());

  const seleccionar = (clinica: Clinica) => {
    seleccionarClinica({ clinicaID: clinica.clinicaID, nombre: clinica.nombre, grupo: clinica.grupo });
  };

  const crearYSeleccionar = async () => {
    const nombreLimpio = nombre.trim();
    if (!nombreLimpio) {
      addToast('warning', 'Indique el nombre de la clínica.');
      return;
    }
    setCreando(true);
    try {
      const { id } = await api.clinicas.crear({ nombre: nombreLimpio, grupo: grupo.trim() || null });
      seleccionarClinica({ clinicaID: id, nombre: nombreLimpio, grupo: grupo.trim() || null });
      addToast('success', 'Clínica creada.');
    } catch (err) {
      addToast('error', err instanceof Error ? err.message : 'No se pudo crear la clínica.');
    } finally {
      setCreando(false);
    }
  };

  return (
    <div className="clinic-screen">
      <div className="clinic-screen-card">
        <div className="clinic-screen-brand">
          <div className="brand-mark">
            <ToothMark size={22} color="#eaf3f3" />
          </div>
          <div className="brand-title">Formatos Odontologicos</div>
        </div>

        <h1 className="clinic-screen-title">Seleccione una clinica</h1>
        <p className="clinic-screen-subtitle">
          Elija la clinica con la que trabajara. Puede cambiarla en cualquier momento.
        </p>

        {loading && <div className="empty-state">Cargando clinicas...</div>}

        {error && !loading && (
          <div className="alert-banner alert-warning" style={{ marginBottom: 12 }}>
            <span>No se pudieron cargar las clinicas. {error}</span>
            <button className="btn btn-ghost btn-sm" onClick={() => refetch()}>
              <RefreshCw size={14} /> Reintentar
            </button>
          </div>
        )}

        {!loading && !error && (
          <>
            {clinicas && clinicas.length === 0 && (
              <div className="empty-state" style={{ marginBottom: 16 }}>
                Aun no existe ninguna clinica. Cree la primera para comenzar.
              </div>
            )}

            {clinicas && clinicas.length > 0 && (
              <ul className="clinic-list">
                {clinicas.map((clinica) => (
                  <li key={clinica.clinicaID} className="clinic-list-item">
                    <div className="clinic-list-info">
                      <Building2 size={18} />
                      <div>
                        <div className="clinic-list-nombre">{clinica.nombre}</div>
                        {clinica.grupo && <div className="clinic-list-grupo">{clinica.grupo}</div>}
                      </div>
                    </div>
                    <button className="btn btn-primary btn-sm" onClick={() => seleccionar(clinica)}>
                      Usar
                    </button>
                  </li>
                ))}
              </ul>
            )}

            <div className="clinic-create">
              <div className="clinic-create-head">
                <Plus size={16} />
                <span>{clinicas && clinicas.length > 0 ? 'Crear otra clinica' : 'Crear clinica'}</span>
              </div>
              <div className="clinic-create-fields">
                <input
                  className="form-input"
                  placeholder="Nombre de la clinica"
                  value={nombre}
                  onChange={(e) => setNombre(e.target.value)}
                />
                <input
                  className="form-input"
                  placeholder="Grupo (opcional)"
                  value={grupo}
                  onChange={(e) => setGrupo(e.target.value)}
                />
                <button className="btn btn-secondary" disabled={creando} onClick={crearYSeleccionar}>
                  {creando ? 'Creando...' : 'Crear'}
                </button>
              </div>
            </div>
          </>
        )}
      </div>
    </div>
  );
}