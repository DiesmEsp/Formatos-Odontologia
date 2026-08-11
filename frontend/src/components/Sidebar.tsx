import { NavLink } from 'react-router-dom';
import {
  LayoutDashboard,
  Stethoscope,
  CalendarCheck,
  Archive,
  FileSpreadsheet,
  Monitor,
  Activity,
} from 'lucide-react';

const ATENCION_ITEMS = [
  { to: '/', icon: LayoutDashboard, label: 'Dashboard' },
  { to: '/tratamientos', icon: Stethoscope, label: 'Tratamientos' },
  { to: '/asistencia', icon: CalendarCheck, label: 'Asistencia' },
];

const GESTION_ITEMS = [
  { to: '/catalogos', icon: Archive, label: 'Catalogos' },
  { to: '/reportes', icon: FileSpreadsheet, label: 'Reportes' },
  { to: '/unidades', icon: Monitor, label: 'Unidades' },
];

export function Sidebar() {
  return (
    <aside className="sidebar" role="navigation" aria-label="Navegación principal">
      <div className="sidebar-brand">
        <div className="brand-row">
          <div className="brand-mark">
            <Activity size={20} color="#eaf3f3" />
          </div>
          <div>
            <div className="brand-title">Formatos</div>
            <div className="brand-subtitle">Odontologicos</div>
          </div>
        </div>
      </div>

      <nav className="sidebar-nav">
        <span className="nav-eyebrow">Atencion</span>
        {ATENCION_ITEMS.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            end={item.to === '/'}
            className={({ isActive }) => `sidebar-link ${isActive ? 'active' : ''}`}
          >
            <item.icon size={18} />
            <span>{item.label}</span>
          </NavLink>
        ))}

        <span className="nav-eyebrow">Gestion</span>
        {GESTION_ITEMS.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            className={({ isActive }) => `sidebar-link ${isActive ? 'active' : ''}`}
          >
            <item.icon size={18} />
            <span>{item.label}</span>
          </NavLink>
        ))}
      </nav>

      <div className="sidebar-footer">
        Clinica Odontologica UNMSM
      </div>
    </aside>
  );
}
