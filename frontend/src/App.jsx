import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import Footer from './components/Footer.jsx';
import Navbar from './components/Navbar.jsx';
import { ToastProvider } from './components/Toast.jsx';
import { isDemoMode } from './services/api.js';
import Dashboard from './pages/Dashboard.jsx';
import Planejamento from './pages/Planejamento.jsx';
import Receitas from './pages/Receitas.jsx';

function AppLayout() {
  return (
    <div className="app-shell">
      <Navbar />
      {isDemoMode && (
        <aside className="demo-banner" role="status">
          <strong>Modo demonstração</strong>
          <span>Dados de exemplo; alterações ficam apenas nesta sessão e não são enviadas à API.</span>
        </aside>
      )}
      <main className="main-content">
        <Routes>
          <Route path="/" element={<Dashboard />} />
          <Route path="/dashboard" element={<Dashboard />} />
          <Route path="/Index.html" element={<Dashboard />} />
          <Route path="/receitas" element={<Receitas />} />
          <Route path="/serviços.html" element={<Receitas />} />
          <Route path="/planejamento" element={<Planejamento />} />
          <Route path="/planejamentos.html" element={<Planejamento />} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </main>
      <Footer />
    </div>
  );
}

export default function App() {
  return (
    <BrowserRouter future={{ v7_startTransition: true, v7_relativeSplatPath: true }}>
      <ToastProvider>
        <AppLayout />
      </ToastProvider>
    </BrowserRouter>
  );
}
