import { Outlet } from 'react-router-dom';
import { Sidebar } from './Sidebar';
import { ClinicSwitcher } from './ClinicSwitcher';

export function Layout() {
  return (
    <div className="app-layout">
      <Sidebar />
      <main className="content-area">
        <div className="topbar">
          <div className="topbar-title">Clinica activa</div>
          <ClinicSwitcher />
        </div>
        <Outlet />
      </main>
    </div>
  );
}