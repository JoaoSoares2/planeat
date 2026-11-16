import { useRef, useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useToast } from '../components/Toast.jsx';
import { useAuth } from '../contexts/AuthContext.jsx';
import './Login.css';

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

export default function Login() {
  const { entrar, authError } = useAuth();
  const { notify } = useToast();
  const location = useLocation();
  const navigate = useNavigate();
  const submitting = useRef(false);
  const [email, setEmail] = useState('');
  const [senha, setSenha] = useState('');
  const [mostrarSenha, setMostrarSenha] = useState(false);
  const [erro, setErro] = useState('');
  const [loading, setLoading] = useState(false);

  async function handleSubmit(event) {
    event.preventDefault();
    if (submitting.current) return;
    const normalizedEmail = email.trim();
    if (!normalizedEmail || !senha) {
      setErro('Informe seu e-mail e sua senha.');
      return;
    }
    if (!EMAIL_PATTERN.test(normalizedEmail)) {
      setErro('Informe um endereço de e-mail válido.');
      return;
    }

    submitting.current = true;
    setErro('');
    setLoading(true);
    try {
      await entrar(normalizedEmail, senha);
      notify('Login realizado com sucesso.');
      const destination = location.state?.from;
      const target = destination?.pathname?.startsWith('/') && !destination.pathname.startsWith('//')
        ? `${destination.pathname}${destination.search || ''}${destination.hash || ''}`
        : '/dashboard';
      navigate(target, { replace: true });
    } catch (requestError) {
      setErro(requestError.message || 'Não foi possível entrar. Tente novamente.');
    } finally {
      submitting.current = false;
      setLoading(false);
    }
  }

  return (
    <main className="auth-page">
      <section className="auth-card" aria-labelledby="login-title">
        <Link className="brand auth-brand" to="/login" aria-label="PlanEat">
          <span className="brand-mark" aria-hidden="true">p</span>
          <span>Plan<span>Eat</span></span>
        </Link>
        <div className="auth-heading">
          <span className="eyebrow">SEU PLANEJAMENTO COMEÇA AQUI</span>
          <h1 id="login-title">Bem-vindo de volta!</h1>
          <p>Entre na sua conta para continuar seu planejamento alimentar.</p>
        </div>
        {location.state?.message && <div className="auth-notice" role="status">{location.state.message}</div>}
        {location.state?.success && <div className="auth-notice auth-notice-success" role="status">{location.state.success}</div>}
        {authError && <div className="auth-notice auth-notice-error" role="alert">{authError}</div>}
        {erro && <div className="auth-notice auth-notice-error" role="alert">{erro}</div>}
        <form className="auth-form" onSubmit={handleSubmit} noValidate>
          <label className="auth-field">
            E-mail
            <input
              type="email"
              name="email"
              autoComplete="email"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              placeholder="voce@exemplo.com"
              aria-invalid={Boolean(erro)}
              required
            />
          </label>
          <label className="auth-field">
            Senha
            <span className="password-input">
              <input
                type={mostrarSenha ? 'text' : 'password'}
                name="senha"
                autoComplete="current-password"
                value={senha}
                onChange={(event) => setSenha(event.target.value)}
                placeholder="Digite sua senha"
                required
              />
              <button
                className="password-toggle"
                type="button"
                onClick={() => setMostrarSenha((visible) => !visible)}
                aria-label={mostrarSenha ? 'Ocultar senha' : 'Mostrar senha'}
                aria-pressed={mostrarSenha}
              >
                {mostrarSenha ? 'Ocultar' : 'Mostrar'}
              </button>
            </span>
          </label>
          <button className="button button-primary auth-submit" type="submit" disabled={loading}>
            {loading ? <><span className="spinner auth-spinner" /> Entrando…</> : 'Entrar'}
          </button>
        </form>
        <p className="auth-switch">Não possui uma conta? <Link to="/cadastro">Cadastre-se</Link></p>
      </section>
      <p className="auth-tagline">Planejar para cuidar — uma refeição de cada vez.</p>
    </main>
  );
}
