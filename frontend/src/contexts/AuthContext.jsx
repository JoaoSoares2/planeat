import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import * as authService from '../services/authService.js';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [authError, setAuthError] = useState('');

  const encerrarSessaoLocal = useCallback(() => {
    setUser(null);
    setAuthError('');
  }, []);

  useEffect(() => {
    let active = true;
    authService.verificarSessao()
      .then((authenticatedUser) => {
        if (active) setUser(authenticatedUser);
      })
      .catch((error) => {
        if (active) {
          setUser(null);
          if (error.status !== 401) {
            setAuthError('Não foi possível verificar sua sessão. Confira a conexão com o servidor.');
          }
        }
      })
      .finally(() => {
        if (active) setLoading(false);
      });

    const handleExpiredSession = () => encerrarSessaoLocal();
    window.addEventListener('planeat:session-expired', handleExpiredSession);
    return () => {
      active = false;
      window.removeEventListener('planeat:session-expired', handleExpiredSession);
    };
  }, [encerrarSessaoLocal]);

  const entrar = useCallback(async (email, senha) => {
    const authenticatedUser = await authService.login(email, senha);
    setUser(authenticatedUser);
    setAuthError('');
    return authenticatedUser;
  }, []);

  const cadastrar = useCallback((dados) => authService.cadastro(dados), []);

  const sair = useCallback(async () => {
    try {
      await authService.logout();
    } finally {
      setUser(null);
    }
  }, []);

  const value = useMemo(() => ({
    user,
    authError,
    nome: user?.nome || '',
    email: user?.email || '',
    autenticado: Boolean(user),
    loading,
    entrar,
    sair,
    cadastrar,
  }), [user, authError, loading, entrar, sair, cadastrar]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth deve ser usado dentro de AuthProvider.');
  return context;
}
