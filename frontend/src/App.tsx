import { HashRouter, Routes, Route } from 'react-router-dom';
import { Layout } from './components/Layout';
import { ToastProvider } from './hooks/useToast';
import { ErrorBoundary } from './components/ErrorBoundary';
import Dashboard from './pages/Dashboard';
import Tratamientos from './pages/Tratamientos';
import Asistencia from './pages/Asistencia';
import Catalogos from './pages/Catalogos';
import Reportes from './pages/Reportes';
import Unidades from './pages/Unidades';

export default function App() {
  return (
    <HashRouter>
      <ToastProvider>
        <ErrorBoundary>
          <Routes>
          <Route element={<Layout />}>
            <Route path="/" element={<Dashboard />} />
            <Route path="/tratamientos" element={<Tratamientos />} />
            <Route path="/asistencia" element={<Asistencia />} />
            <Route path="/catalogos" element={<Catalogos />} />
            <Route path="/reportes" element={<Reportes />} />
            <Route path="/unidades" element={<Unidades />} />
          </Route>
          </Routes>
        </ErrorBoundary>
      </ToastProvider>
    </HashRouter>
  );
}
