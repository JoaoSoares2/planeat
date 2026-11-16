import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { listarReceitas } from '../services/receitaService.js';
import { buscarRelatorioDesperdicio, listarPlanejamentos } from '../services/planoService.js';

const quickLinks = [
  { icon: '+', title: 'Cadastrar receita', description: 'Guarde seus pratos favoritos e ingredientes.', to: '/serviços.html', action: 'Ir para receitas', tone: 'mint' },
  { icon: '↗', title: 'Explorar receitas', description: 'Encontre uma opção para cada refeição.', to: '/serviços.html', action: 'Ver catálogo', tone: 'peach' },
  { icon: '▦', title: 'Planejar a semana', description: 'Organize almoço e jantar em poucos minutos.', to: '/planejamentos.html', action: 'Montar cardápio', tone: 'lavender' },
  { icon: '✓', title: 'Lista de compras', description: 'Leve ao mercado uma lista consolidada.', to: '/planejamentos.html#lista', action: 'Abrir lista', tone: 'yellow' },
];

export default function Dashboard() {
  const metricSource = 'Dados da sua conta';
  const [recipes, setRecipes] = useState([]);
  const [plans, setPlans] = useState([]);
  const [impactReport, setImpactReport] = useState(null);
  const [recipesAvailable, setRecipesAvailable] = useState(false);
  const [plansAvailable, setPlansAvailable] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    let active = true;
    Promise.allSettled([listarReceitas(), listarPlanejamentos()])
      .then(async ([recipeResult, planResult]) => {
        if (!active) return;
        const errors = [];
        if (recipeResult.status === 'fulfilled') {
          setRecipes(recipeResult.value);
          setRecipesAvailable(true);
        } else {
          errors.push(recipeResult.reason.message);
        }
        const availablePlans = planResult.status === 'fulfilled' ? planResult.value : [];
        if (planResult.status === 'fulfilled') {
          setPlans(availablePlans);
          setPlansAvailable(true);
        } else {
          errors.push(planResult.reason.message);
        }
        if (availablePlans.length) {
          const latestPlan = [...availablePlans].sort((a, b) => Number(b.id) - Number(a.id))[0];
          try {
            const report = await buscarRelatorioDesperdicio(latestPlan.id);
            if (active) setImpactReport(report);
          } catch (requestError) {
            errors.push(`Relatório de aproveitamento indisponível: ${requestError.message}`);
          }
        }
        if (active && errors.length) setError(errors.join(' '));
      })
      .catch((requestError) => {
        if (active) setError(requestError.message);
      })
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => { active = false; };
  }, []);

  const mealsPlanned = plans.reduce((total, plan) => total + (plan.refeicoes?.length || 0), 0);

  return (
    <div className="page dashboard-page">
      <section className="hero">
        <div className="hero-content">
          <span className="eyebrow"><span className="eyebrow-dot" /> PLANEJE COM PROPÓSITO</span>
          <h1>O futuro sustentável começa na sua <span>despensa.</span></h1>
          <p>Planeje suas refeições, organize suas compras e reduza o desperdício de alimentos de maneira inteligente.</p>
          <Link className="button button-primary hero-button" to="/planejamentos.html">
            Montar cardápio da semana <span aria-hidden="true">→</span>
          </Link>
          <div className="hero-note"><span aria-hidden="true">✦</span> Pequenas escolhas. Um impacto que alimenta o futuro.</div>
        </div>
        <div className="hero-art" aria-hidden="true">
          <div className="hero-sun" />
          <div className="hero-leaf leaf-one" />
          <div className="hero-leaf leaf-two" />
          <div className="hero-bowl"><span>🥗</span></div>
          <div className="hero-plant"><i /><i /><i /></div>
          <span className="hero-sparkle sparkle-one">✳</span>
          <span className="hero-sparkle sparkle-two">✦</span>
          <div className="floating-label"><span>↘</span> Menos desperdício, mais cuidado</div>
        </div>
        <div className="hero-decoration" aria-hidden="true">PlanEat · Planejar para cuidar</div>
      </section>

      <section className="impact-section" aria-labelledby="impact-title">
        <div className="section-heading">
          <div>
            <span className="eyebrow">O IMPACTO DE PLANEJAR</span>
            <h2 id="impact-title">Consciência em cada escolha</h2>
          </div>
          <span className="section-aside">ODS 2 <b>·</b> ODS 11</span>
        </div>
        {error && <div className="notice notice-error" role="alert">{error}</div>}
        <div className="impact-grid">
          <article className="impact-card impact-context">
            <div className="impact-icon">◉</div>
            <div className="impact-value">27 <small>mi</small></div>
            <h3>toneladas de alimentos</h3>
            <p>perdidas por ano no Brasil <span className="context-tag">referência nacional</span></p>
            <span className="impact-footnote">Um dado contextual, não calculado pelo PlanEat.</span>
          </article>
          <article className="impact-card">
            <div className="impact-icon icon-green">▦</div>
            <div className="impact-value">{loading || !plansAvailable ? '—' : mealsPlanned}</div>
            <h3>refeições planejadas</h3>
            <p>somadas dos seus planejamentos cadastrados</p>
            <span className="impact-footnote">{metricSource}</span>
          </article>
          <article className="impact-card">
            <div className="impact-icon icon-orange">♡</div>
            <div className="impact-value">{loading || !recipesAvailable ? '—' : recipes.length}</div>
            <h3>receitas cadastradas</h3>
            <p>prontas para fazer parte do seu cardápio</p>
            <span className="impact-footnote">{metricSource}</span>
          </article>
          <article className="impact-card impact-promise">
            <div className="impact-icon icon-lilac">✳</div>
            <div className="impact-value">{loading ? '—' : impactReport?.totalItensReaproveitados ?? '—'}{impactReport && <small> itens</small>}</div>
            <h3>Aproveitados em mais de uma refeição</h3>
            <p>{impactReport ? `${impactReport.taxaAproveitamentoPercentual}% de aproveitamento calculado pelo back-end` : 'Gere um planejamento para consultar o relatório de aproveitamento.'}</p>
          </article>
        </div>
      </section>

      <section className="quick-section" aria-labelledby="quick-title">
        <div className="section-heading">
          <div>
            <span className="eyebrow">UM BOM COMEÇO</span>
            <h2 id="quick-title">O que você quer fazer hoje?</h2>
          </div>
          <span className="section-aside">Escolha seu próximo passo <b>↘</b></span>
        </div>
        <div className="quick-grid">
          {quickLinks.map((item, index) => (
            <Link to={item.to} className="quick-card" key={item.title}>
              <span className={`quick-icon ${item.tone}`} aria-hidden="true">{item.icon}</span>
              <span className="quick-number">0{index + 1}</span>
              <h3>{item.title}</h3>
              <p>{item.description}</p>
              <span className="quick-action">{item.action} <b aria-hidden="true">→</b></span>
            </Link>
          ))}
        </div>
      </section>
    </div>
  );
}
