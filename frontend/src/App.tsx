import { useEffect, useState } from 'react';

function App() {
  const [backendStatus, setBackendStatus] = useState<'loading' | 'online' | 'offline'>('loading');

  useEffect(() => {
    async function checkHealth() {
      try {
        const result = await window.api.health.check();
        if (result.status === 'OK') {
          setBackendStatus('online');
        } else {
          setBackendStatus('offline');
        }
      } catch {
        setBackendStatus('offline');
      }
    }

    checkHealth();
    const interval = setInterval(checkHealth, 10000);
    return () => clearInterval(interval);
  }, []);

  return (
    <div className="app-container">
      <header className="app-header">
        <h1>Formatos Odontologicos</h1>
        <span className={`status-badge status-${backendStatus}`}>
          {backendStatus === 'loading' && 'Conectando...'}
          {backendStatus === 'online' && 'Backend conectado'}
          {backendStatus === 'offline' && 'Sin conexion'}
        </span>
      </header>
      <main className="app-main">
        <div className="welcome-card">
          <h2>Clinica Odontologica UNMSM</h2>
          <p>Version 2.0.0 - React + Electron</p>
          <p>Migracion progresiva en curso. Fase 0.5 completada.</p>
          <ul>
            <li>Estructura del proyecto creada</li>
            <li>Configuracion Vite + React + Electron lista</li>
            <li>Backend Javalin esqueleto creado</li>
            <li>Pipeline CI configurado</li>
          </ul>
        </div>
      </main>
    </div>
  );
}

export default App;
