import { useState } from 'react';
import { Link, NavLink } from 'react-router-dom';

const links = [
  { to: '/', label: 'Início', active: ['/', '/dashboard', '/Index.html'] },
  { to: '/serviços.html', label: 'Receitas', active: ['/receitas', '/serviços.html'] },
  { to: '/planejamentos.html', label: 'Planejamento', active: ['/planejamento', '/planejamentos.html'] },
];

export default function Navbar() {
  const [menuOpen, setMenuOpen] = useState(false);
  return (
    <header className="site-header">
      <div className="header-inner">
        <Link className="brand" to="/" aria-label="PlanEat, início">
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
        </nav>
      </div>
    </header>
  );
}
