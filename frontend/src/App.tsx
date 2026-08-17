import { HashRouter, Routes, Route } from 'react-router-dom';
import { Layout } from './components/Layout';
import { ToastProvider } from './hooks/useToast';
import { ErrorBoundary } from './components/ErrorBoundary';
import { ClinicaProvider, useClinica } from './contexts/ClinicaContext';
import { SelectClinicScreen } from './components/SelectClinicScreen';
import Dashboard from './pages/Dashboard';
import Tratamientos from './pages/Tratamientos';
import Asistencia from './pages/Asistencia';
import Catalogos from './pages/Catalogos';
import Reportes from './pages/Reportes';
import Unidades from './pages/Unidades';
import Pagos from './pages/Pagos';
import Clinicas from './pages/Clinicas';

function AppContent() {
  const { clinica } = useClinica();

  if (!clinica) {
    return <SelectClinicScreen />;
  }

  return (
    <ErrorBoundary>
      <Routes key={clinica.clinicaID}>
        <Route element={<Layout />}>
          <Route path="/" element={<Dashboard />} />
          <Route path="/tratamientos" element={<Tratamientos />} />
          <Route path="/asistencia" element={<Asistencia />} />
          <Route path="/catalogos" element={<Catalogos />} />
          <Route path="/reportes" element={<Reportes />} />
          <Route path="/unidades" element={<Unidades />} />
          <Route path="/pagos" element={<Pagos />} />
          <Route path="/clinicas" element={<Clinicas />} />
        </Route>
      </Routes>
    </ErrorBoundary>
  );
}

export default function App() {
  return (
    <HashRouter>
      <ToastProvider>
        <ClinicaProvider>
          <AppContent />
        </ClinicaProvider>
      </ToastProvider>
    </HashRouter>
  );
}