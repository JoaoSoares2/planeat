import { useState } from 'react';
import { Link, NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext.jsx';
import { useToast } from './Toast.jsx';

const links = [
  { to: '/dashboard', label: 'Dashboard', active: ['/', '/dashboard', '/Index.html'] },
  { to: '/serviços.html', label: 'Receitas', active: ['/receitas', '/serviços.html'] },
  { to: '/planejamentos.html', label: 'Planejamento', active: ['/planejamento', '/planejamentos.html'] },
];

export default function Navbar() {
  const { nome, sair } = useAuth();
  const { notify } = useToast();
  const navigate = useNavigate();
  const [menuOpen, setMenuOpen] = useState(false);

  async function handleLogout() {
    try {
      await sair();
    } catch (error) {
      notify(error.message, 'error');
    } finally {
      navigate('/login', { replace: true });
    }
  }

  return (
    <header className="site-header">
      <div className="header-inner">
        <Link className="brand" to="/dashboard" aria-label="PlanEat, dashboard">
          <span className="brand-mark" aria-hidden="true">p</span>
          <span>Plan<span>Eat</span></span>
        </Link>
        <button
          className="menu-toggle"
          type="button"
          aria-expanded={menuOpen}
          aria-controls="primary-navigation"
          aria-label={menuOpen ? 'Fechar menu' : 'Abrir menu'}
          onClick={() => setMenuOpen((open) => !open)}
        >
          <span /><span /><span />
        </button>
        <nav id="primary-navigation" className={`main-nav ${menuOpen ? 'is-open' : ''}`} aria-label="Navegação principal">
          {links.map((link) => (
            <NavLink
              key={link.to}
              to={link.to}
              end={link.to === '/'}
              className={({ isActive }) => {
                const active = isActive || link.active.includes(window.location.pathname);
                return `nav-link${active ? ' is-active' : ''}`;
              }}
              onClick={() => setMenuOpen(false)}
            >
              {link.label}
            </NavLink>
          ))}
          <span className="nav-user" title={nome}>{nome}</span>
          <button className="nav-logout" type="button" onClick={handleLogout}>Sair</button>
        </nav>
      </div>
    </header>
  );
}
