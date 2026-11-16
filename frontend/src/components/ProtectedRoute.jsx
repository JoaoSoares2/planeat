import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext.jsx';

export default function ProtectedRoute({ children }) {
  const { autenticado, loading } = useAuth();
  const location = useLocation();

  if (loading) {
    return <div className="auth-loading" role="status"><span className="spinner" /> Verificando sua sessão…</div>;
  }
  if (!autenticado) {
    return (
      <Navigate
        to="/login"
        replace
        state={{ from: location, message: 'Faça seu login para acessar o PlanEat.' }}
      />
    );
  }
  return children;
}
