import { useEffect } from 'react';
import { BrowserRouter, Navigate, Outlet, Route, Routes, useLocation, useNavigate } from 'react-router-dom';
import Footer from './components/Footer.jsx';
import Navbar from './components/Navbar.jsx';
import ProtectedRoute from './components/ProtectedRoute.jsx';
import { ToastProvider } from './components/Toast.jsx';
import { AuthProvider, useAuth } from './contexts/AuthContext.jsx';
import Cadastro from './pages/Cadastro.jsx';
import Dashboard from './pages/Dashboard.jsx';
import Login from './pages/Login.jsx';
import Planejamento from './pages/Planejamento.jsx';
import Receitas from './pages/Receitas.jsx';

function AppLayout() {
  return (
    <div className="app-shell">
      <Navbar />
      <main className="main-content"><Outlet /></main>
      <Footer />
    </div>
  );
}

function PublicRoute({ children }) {
  const { autenticado, loading } = useAuth();
  const location = useLocation();
  if (loading) return <div className="auth-loading" role="status"><span className="spinner" /> Verificando sua sessão…</div>;
  if (autenticado) {
    const destination = location.state?.from?.pathname;
    return <Navigate to={destination?.startsWith('/') && !destination.startsWith('//') ? destination : '/dashboard'} replace />;
  }
  return children;
}

function SessionExpiryRedirector() {
  const navigate = useNavigate();
  useEffect(() => {
    const redirect = () => navigate('/login', {
      replace: true,
      state: { message: 'Sua sessão expirou. Faça seu login novamente.' },
    });
    window.addEventListener('planeat:session-expired', redirect);
    return () => window.removeEventListener('planeat:session-expired', redirect);
  }, [navigate]);
  return null;
}

function AppRoutes() {
  return (
    <>
      <SessionExpiryRedirector />
      <Routes>
        <Route path="/login" element={<PublicRoute><Login /></PublicRoute>} />
        <Route path="/cadastro" element={<PublicRoute><Cadastro /></PublicRoute>} />
        <Route element={<ProtectedRoute><AppLayout /></ProtectedRoute>}>
          <Route path="/" element={<Dashboard />} />
          <Route path="/dashboard" element={<Dashboard />} />
          <Route path="/Index.html" element={<Dashboard />} />
          <Route path="/receitas" element={<Receitas />} />
          <Route path="/serviços.html" element={<Receitas />} />
          <Route path="/planejamento" element={<Planejamento />} />
          <Route path="/planejamentos.html" element={<Planejamento />} />
        </Route>
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </>
  );
}

export default function App() {
  return (
    <BrowserRouter future={{ v7_startTransition: true, v7_relativeSplatPath: true }}>
      <ToastProvider>
        <AuthProvider><AppRoutes /></AuthProvider>
      </ToastProvider>
    </BrowserRouter>
  );
}
