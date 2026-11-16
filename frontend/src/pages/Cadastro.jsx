import { useRef, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext.jsx';
import './Login.css';
import './Cadastro.css';

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

export default function Cadastro() {
  const { cadastrar } = useAuth();
  const navigate = useNavigate();
  const submitting = useRef(false);
  const [form, setForm] = useState({ nome: '', email: '', senha: '', confirmarSenha: '' });
  const [mostrarSenhas, setMostrarSenhas] = useState(false);
  const [erro, setErro] = useState('');
  const [loading, setLoading] = useState(false);

  function updateField(event) {
    setForm((current) => ({ ...current, [event.target.name]: event.target.value }));
    setErro('');
  }

  async function handleSubmit(event) {
    event.preventDefault();
    if (submitting.current) return;
    if (!form.nome.trim() || !form.email.trim() || !form.senha || !form.confirmarSenha) {
      setErro('Preencha todos os campos para continuar.');
      return;
    }
    if (!EMAIL_PATTERN.test(form.email.trim())) {
      setErro('Informe um endereço de e-mail válido.');
      return;
    }
    if (form.senha.length < 8) {
      setErro('A senha deve ter pelo menos 8 caracteres.');
      return;
    }
    if (form.senha !== form.confirmarSenha) {
      setErro('A confirmação precisa ser igual à senha informada.');
      return;
    }

    submitting.current = true;
    setLoading(true);
    setErro('');
    try {
      await cadastrar({ nome: form.nome.trim(), email: form.email.trim(), senha: form.senha });
      navigate('/login', {
        replace: true,
        state: { success: 'Cadastro realizado com sucesso. Entre na sua conta para continuar.' },
      });
    } catch (requestError) {
      setErro(requestError.message || 'Não foi possível concluir o cadastro.');
    } finally {
      submitting.current = false;
      setLoading(false);
    }
  }

  return (
    <main className="auth-page">
      <section className="auth-card cadastro-card" aria-labelledby="cadastro-title">
        <Link className="brand auth-brand" to="/login" aria-label="PlanEat">
          <span className="brand-mark" aria-hidden="true">p</span>
          <span>Plan<span>Eat</span></span>
        </Link>
        <div className="auth-heading">
          <span className="eyebrow">CRIE SEU ESPAÇO</span>
          <h1 id="cadastro-title">Sua rotina, do seu jeito.</h1>
          <p>Cadastre-se para organizar suas receitas e planejar sua semana.</p>
        </div>
        {erro && <div className="auth-notice auth-notice-error" role="alert">{erro}</div>}
        <form className="auth-form" onSubmit={handleSubmit} noValidate>
          <label className="auth-field">
            Nome completo
            <input name="nome" autoComplete="name" value={form.nome} onChange={updateField} maxLength="100" required />
          </label>
          <label className="auth-field">
            E-mail
            <input type="email" name="email" autoComplete="email" value={form.email} onChange={updateField} maxLength="100" required />
          </label>
          <label className="auth-field">
            Senha
            <input type={mostrarSenhas ? 'text' : 'password'} name="senha" autoComplete="new-password" value={form.senha} onChange={updateField} required />
          </label>
          <label className="auth-field">
            Confirmar senha
            <input type={mostrarSenhas ? 'text' : 'password'} name="confirmarSenha" autoComplete="new-password" value={form.confirmarSenha} onChange={updateField} required />
          </label>
          <label className="show-password-option">
            <input type="checkbox" checked={mostrarSenhas} onChange={(event) => setMostrarSenhas(event.target.checked)} />
            Mostrar senhas
          </label>
          <button className="button button-primary auth-submit" type="submit" disabled={loading}>
            {loading ? <><span className="spinner auth-spinner" /> Criando conta…</> : 'Criar minha conta'}
          </button>
        </form>
        <p className="auth-switch">Já possui uma conta? <Link to="/login">Entrar</Link></p>
      </section>
      <p className="auth-tagline">Planejar para cuidar — uma refeição de cada vez.</p>
    </main>
  );
}
