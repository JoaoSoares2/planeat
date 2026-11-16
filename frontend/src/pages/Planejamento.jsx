import { useCallback, useEffect, useMemo, useState } from 'react';
import { useToast } from '../components/Toast.jsx';
import {
  atualizarStatusItem,
  buscarListaCompras,
  buscarPlanejamento,
  criarPlanejamento,
  listarPlanejamentos,
} from '../services/planoService.js';
import { listarReceitas } from '../services/receitaService.js';

const DAYS = ['Segunda-feira', 'Terça-feira', 'Quarta-feira', 'Quinta-feira', 'Sexta-feira', 'Sábado', 'Domingo'];
const MEALS = ['Almoço', 'Jantar'];

function emptySlots() {
  return Object.fromEntries(DAYS.flatMap((day) => MEALS.map((meal) => [`${day}|${meal}`, ''])));
}

function getWeekDates() {
  const today = new Date();
  const day = today.getDay() || 7;
  const monday = new Date(today);
  monday.setDate(today.getDate() - day + 1);
  const sunday = new Date(monday);
  sunday.setDate(monday.getDate() + 6);
  const format = (date) => {
    const localDate = new Date(date.getTime() - date.getTimezoneOffset() * 60000);
    return localDate.toISOString().slice(0, 10);
  };
  return { start: format(monday), end: format(sunday) };
}

function slotsFromPlan(plan) {
  const slots = emptySlots();
  (plan.refeicoes || []).forEach((meal) => {
    const key = `${meal.diaSemana}|${meal.tipoRefeicao}`;
    if (key in slots) slots[key] = String(meal.receitaId ?? '');
  });
  return slots;
}

function displayCategory(category) {
  const normalized = (category || '').toLocaleLowerCase('pt-BR');
  if (normalized.includes('hort') || normalized.includes('frut')) return 'Hortifrúti';
  if (normalized.includes('açougue') || normalized.includes('acougue') || normalized.includes('peix')) return 'Açougue & Peixaria';
  if (normalized.includes('mercearia') || normalized.includes('grão') || normalized.includes('grao')) return 'Mercearia & Grãos';
  if (normalized.includes('latic') || normalized.includes('ovo')) return 'Laticínios & Ovos';
  return category || 'Outros';
}

function formatAmount(value) {
  return new Intl.NumberFormat('pt-BR', { maximumFractionDigits: 2 }).format(Number(value) || 0);
}

export default function Planejamento() {
  const { notify } = useToast();
  const [recipes, setRecipes] = useState([]);
  const [plans, setPlans] = useState([]);
  const [planId, setPlanId] = useState('');
  const [planTitle, setPlanTitle] = useState('Meu cardápio semanal');
  const initialDates = useMemo(getWeekDates, []);
  const [startDate, setStartDate] = useState(initialDates.start);
  const [endDate, setEndDate] = useState(initialDates.end);
  const [slots, setSlots] = useState(emptySlots);
  const [savedSlots, setSavedSlots] = useState(emptySlots);
  const [loading, setLoading] = useState(true);
  const [pageError, setPageError] = useState('');
  const [saving, setSaving] = useState(false);
  const [generating, setGenerating] = useState(false);
  const [shoppingList, setShoppingList] = useState(null);
  const [listError, setListError] = useState('');
  const [updatingItems, setUpdatingItems] = useState({});
  const [sharing, setSharing] = useState(false);

  const dirty = JSON.stringify(slots) !== JSON.stringify(savedSlots);
  const selectedMeals = Object.values(slots).filter(Boolean).length;
  const groupedItems = useMemo(() => {
    const items = shoppingList?.itens || [];
    return items.reduce((groups, item) => {
      const category = displayCategory(item.categoria);
      if (!groups[category]) groups[category] = [];
      groups[category].push(item);
      return groups;
    }, {});
  }, [shoppingList]);
  const totalItems = shoppingList?.itens?.length || 0;
  const boughtItems = shoppingList?.itens?.filter((item) => item.comprado).length || 0;
  const pendingItems = totalItems - boughtItems;
  const progress = totalItems ? Math.round((boughtItems / totalItems) * 100) : 0;

  const loadPlan = useCallback(async (id, allPlans = plans) => {
    if (!id) return;
    const plan = await buscarPlanejamento(id);
    const nextSlots = slotsFromPlan(plan);
    setPlanId(String(plan.id));
    setPlans(allPlans);
    setPlanTitle(plan.titulo || 'Meu cardápio semanal');
    setStartDate(plan.dataInicio || initialDates.start);
    setEndDate(plan.dataFim || initialDates.end);
    setSlots(nextSlots);
    setSavedSlots(nextSlots);
    setShoppingList(null);
    setListError('');
  }, [initialDates.end, initialDates.start, plans]);

  useEffect(() => {
    let active = true;
    async function loadPage() {
      try {
        const [recipeResult, planResult] = await Promise.allSettled([listarReceitas(), listarPlanejamentos()]);
        if (!active) return;
        if (recipeResult.status === 'fulfilled') {
          setRecipes(recipeResult.value);
        } else {
          setPageError(recipeResult.reason.message);
        }
        if (planResult.status === 'fulfilled') {
          const nextPlans = planResult.value;
          setPlans(nextPlans);
          if (nextPlans.length) {
            const latest = [...nextPlans].sort((a, b) => Number(b.id) - Number(a.id))[0];
            try {
              const plan = await buscarPlanejamento(latest.id);
              if (!active) return;
              const nextSlots = slotsFromPlan(plan);
              setPlanId(String(plan.id));
              setPlanTitle(plan.titulo || 'Meu cardápio semanal');
              setStartDate(plan.dataInicio || initialDates.start);
              setEndDate(plan.dataFim || initialDates.end);
              setSlots(nextSlots);
              setSavedSlots(nextSlots);
            } catch (requestError) {
              if (active) setPageError(requestError.message);
            }
          }
        } else {
          setPageError((current) => current ? `${current} ${planResult.reason.message}` : planResult.reason.message);
        }
      } finally {
        if (active) setLoading(false);
      }
    }
    loadPage();
    return () => { active = false; };
  }, [initialDates.end, initialDates.start]);

  useEffect(() => {
    if (window.location.hash === '#lista') {
      window.setTimeout(() => document.getElementById('lista')?.scrollIntoView({ behavior: 'smooth' }), 200);
    }
  }, []);

  function updateSlot(day, meal, recipeId) {
    setSlots((current) => ({ ...current, [`${day}|${meal}`]: recipeId }));
    setListError('');
  }

  async function persistPlan() {
    if (!planTitle.trim()) {
      notify('Informe um título para o planejamento.', 'error');
      return null;
    }
    if (!startDate || !endDate || endDate < startDate) {
      notify('Confira as datas: o término deve ser igual ou posterior ao início.', 'error');
      return null;
    }
    if (!selectedMeals) {
      notify('Escolha ao menos uma refeição antes de salvar.', 'error');
      return null;
    }
    const refeicoes = DAYS.flatMap((day) => MEALS
      .filter((meal) => slots[`${day}|${meal}`])
      .map((meal) => {
        const receitaId = Number(slots[`${day}|${meal}`]);
        const recipe = recipes.find((item) => Number(item.id) === receitaId);
        return { receitaId, nomeReceita: recipe?.nome || '', diaSemana: day, tipoRefeicao: meal };
      }));
    const payload = {
      usuarioId: 1,
      titulo: planTitle.trim(),
      dataInicio: startDate,
      dataFim: endDate,
      status: 'ATIVO',
      refeicoes,
    };
    const created = await criarPlanejamento(payload);
    if (created.id == null) throw new Error('O planejamento foi criado, mas a API não retornou seu identificador.');
    const savedId = String(created.id);
    setPlanId(savedId);
    setPlans((current) => [created, ...current.filter((plan) => String(plan.id) !== savedId)]);
    setSavedSlots({ ...slots });
    setShoppingList(null);
    setListError('');
    return created;
  }

  async function handleSave() {
    if (planId && !dirty) {
      notify('Este cardápio já está salvo.');
      return;
    }
    setSaving(true);
    try {
      const created = await persistPlan();
      if (created) notify(planId ? 'Nova versão do planejamento salva.' : 'Planejamento salvo com sucesso.');
    } catch (requestError) {
      notify(requestError.message, 'error');
    } finally {
      setSaving(false);
    }
  }

  async function handleGenerateList() {
    setGenerating(true);
    setListError('');
    try {
      let activePlanId = planId;
      if (!planId || dirty) {
        const created = await persistPlan();
        if (!created) return;
        activePlanId = created.id;
      }
      const result = await buscarListaCompras(activePlanId);
      if (!result || !Array.isArray(result.itens)) {
        throw new Error('A API retornou uma lista de compras em formato inesperado.');
      }
      setShoppingList(result);
      notify('Lista inteligente consolidada pelo PlanEat.');
      window.setTimeout(() => document.getElementById('lista')?.scrollIntoView({ behavior: 'smooth', block: 'start' }), 100);
    } catch (requestError) {
      setListError(requestError.message);
      notify(requestError.message, 'error');
    } finally {
      setGenerating(false);
    }
  }

  async function toggleItem(item) {
    if (item.id == null || updatingItems[item.id]) return;
    const nextBought = !item.comprado;
    setUpdatingItems((current) => ({ ...current, [item.id]: true }));
    setShoppingList((current) => ({
      ...current,
      itens: current.itens.map((entry) => entry.id === item.id ? { ...entry, comprado: nextBought } : entry),
    }));
    try {
      const updated = await atualizarStatusItem(item.id, nextBought);
      if (updated && typeof updated.comprado === 'boolean' && updated.comprado !== nextBought) {
        throw new Error('O back-end não confirmou o status solicitado para este item.');
      }
    } catch (requestError) {
      setShoppingList((current) => ({
        ...current,
        itens: current.itens.map((entry) => entry.id === item.id ? { ...entry, comprado: !nextBought } : entry),
      }));
      notify(requestError.message, 'error');
    } finally {
      setUpdatingItems((current) => ({ ...current, [item.id]: false }));
    }
  }

  function buildShareText() {
    const lines = [`🛒 Lista de compras PlanEat${shoppingList?.titulo ? ` — ${shoppingList.titulo}` : ''}`, ''];
    Object.entries(groupedItems).forEach(([category, items]) => {
      lines.push(`${category.toLocaleUpperCase('pt-BR')}`);
      items.forEach((item) => lines.push(`${item.comprado ? '☑' : '☐'} ${item.nome} — ${formatAmount(item.quantidadeTotal)} ${item.unidadeMedida || ''}`.trim()));
      lines.push('');
    });
    lines.push(`Progresso: ${boughtItems}/${totalItems} itens comprados`);
    return lines.join('\n');
  }

  async function shareList() {
    if (!shoppingList) return;
    setSharing(true);
    const text = buildShareText();
    try {
      if (navigator.share) {
        await navigator.share({ title: 'Lista de compras PlanEat', text });
        notify('Lista compartilhada.');
      } else if (navigator.clipboard?.writeText) {
        await navigator.clipboard.writeText(text);
        notify('Lista copiada. Agora é só colar na conversa.');
      } else {
        throw new Error('O compartilhamento e a cópia não estão disponíveis neste navegador.');
      }
    } catch (shareError) {
      if (shareError.name !== 'AbortError') notify(shareError.message, 'error');
    } finally {
      setSharing(false);
    }
  }

  async function choosePlan(event) {
    const id = event.target.value;
    if (!id) return;
    setLoading(true);
    try {
      await loadPlan(id);
    } catch (requestError) {
      notify(requestError.message, 'error');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="page planning-page">
      <section className="page-intro planning-intro">
        <div>
          <span className="eyebrow">PLANEJA · PROCESSA · EXECUTA</span>
          <h1>Uma semana mais <span>leve.</span></h1>
          <p>Monte seu cardápio. A lista inteligente organiza os ingredientes por você.</p>
        </div>
        <div className="planning-stamp"><span>7</span><small>dias<br />de cuidado</small></div>
      </section>

      {pageError && <div className="notice notice-error" role="alert">{pageError}</div>}
      {loading && <div className="loading-panel"><span className="spinner" /> Carregando receitas e planejamentos…</div>}

      {!loading && (
        <>
          <section className="weekly-section" aria-labelledby="week-title">
            <div className="section-heading">
              <div>
                <span className="eyebrow">SEU CARDÁPIO</span>
                <h2 id="week-title">Planejamento semanal</h2>
              </div>
              {plans.length > 0 && (
                <label className="plan-picker">
                  <span>Planejamento salvo</span>
                  <select value={planId} onChange={choosePlan} aria-label="Selecionar planejamento salvo">
                    {plans.map((plan) => <option key={plan.id} value={plan.id}>{plan.titulo || `Planejamento ${plan.id}`}</option>)}
                  </select>
                </label>
              )}
            </div>
            <div className="planning-details">
              <label className="field plan-title-field">Nome do planejamento
                <input value={planTitle} onChange={(event) => setPlanTitle(event.target.value)} />
              </label>
              <label className="field">Início
                <input type="date" value={startDate} onChange={(event) => setStartDate(event.target.value)} />
              </label>
              <label className="field">Término
                <input type="date" value={endDate} min={startDate} onChange={(event) => setEndDate(event.target.value)} />
              </label>
            </div>
            {recipes.length === 0 ? (
              <div className="notice notice-warning">
                Não há receitas no catálogo para montar o cardápio. Cadastre uma receita em <a href="/serviços.html">Receitas</a> e volte para planejar.
              </div>
            ) : (
              <div className="week-grid">
                {DAYS.map((day, index) => (
                  <article className="day-card" key={day}>
                    <header className="day-card-heading">
                      <span className="day-number">0{index + 1}</span>
                      <h3>{day}</h3>
                    </header>
                    {MEALS.map((meal) => (
                      <label className="meal-field" key={`${day}-${meal}`}>
                        <span className="meal-label"><span aria-hidden="true">{meal === 'Almoço' ? '☀' : '☾'}</span>{meal}</span>
                        <select
                          value={slots[`${day}|${meal}`]}
                          onChange={(event) => updateSlot(day, meal, event.target.value)}
                          aria-label={`${meal} de ${day}`}
                        >
                          <option value="">Escolher receita</option>
                          {recipes.map((recipe) => <option key={recipe.id} value={recipe.id}>{recipe.nome}</option>)}
                        </select>
                      </label>
                    ))}
                  </article>
                ))}
              </div>
            )}
            <div className="plan-actions">
              <span className="plan-hint">
                {dirty && planId ? 'A API não possui rota de edição: salvar suas mudanças cria uma nova versão do planejamento.' : `${selectedMeals} ${selectedMeals === 1 ? 'refeição escolhida' : 'refeições escolhidas'}`}
              </span>
              <button className="button button-outline" type="button" onClick={handleSave} disabled={saving || generating || !recipes.length}>
                {saving ? 'Salvando…' : planId && !dirty ? '✓ Planejamento salvo' : 'Salvar planejamento'}
              </button>
              <button className="button button-primary button-generate" type="button" onClick={handleGenerateList} disabled={generating || saving || !recipes.length}>
                <span aria-hidden="true">⚡</span> {generating ? 'Consolidando no servidor…' : 'Gerar Lista de Compras Inteligente'}
              </button>
            </div>
          </section>

          <section className="shopping-section" id="lista" aria-labelledby="shopping-title">
            <div className="section-heading">
              <div>
                <span className="eyebrow">PRONTA PARA O MERCADO</span>
                <h2 id="shopping-title">Lista de compras</h2>
              </div>
              {shoppingList && <button className="button button-outline share-button" type="button" onClick={shareList} disabled={sharing}>{sharing ? 'Preparando…' : '↗ Compartilhar lista'}</button>}
            </div>
            {listError && <div className="notice notice-error" role="alert">{listError}</div>}
            {!shoppingList && !listError && (
              <div className="shopping-empty">
                <span className="shopping-empty-icon" aria-hidden="true">▤</span>
                <div><h3>Sua lista aparece aqui</h3><p>Salve e gere o cardápio para consolidar os ingredientes no servidor.</p></div>
                <span className="empty-arrow" aria-hidden="true">↓</span>
              </div>
            )}
            {shoppingList && (
              <div className="shopping-board">
                <div className="progress-card">
                  <div className="progress-summary">
                    <div><span className="eyebrow">SEU PROGRESSO</span><h3>{boughtItems} <span>de</span> {totalItems} itens</h3></div>
                    <strong>{progress}%</strong>
                  </div>
                  <div className="progress-track" role="progressbar" aria-label="Progresso das compras" aria-valuenow={progress} aria-valuemin="0" aria-valuemax="100">
                    <span style={{ width: `${progress}%` }} />
                  </div>
                  <div className="progress-legend"><span>{boughtItems} comprados</span><span>{pendingItems} pendentes</span></div>
                </div>
                {!totalItems ? (
                  <div className="empty-state shopping-no-items"><h3>Nenhum ingrediente nesta lista</h3><p>Confira se o planejamento possui receitas com ingredientes cadastrados.</p></div>
                ) : (
                  <div className="shopping-groups">
                    {Object.entries(groupedItems).map(([category, items]) => (
                      <section className="shopping-group" key={category} aria-labelledby={`group-${category}`}>
                        <h3 id={`group-${category}`}><span aria-hidden="true">▸</span> {category}<span className="group-count">{items.length}</span></h3>
                        <ul>
                          {items.map((item, index) => {
                            const checked = Boolean(item.comprado);
                            const disabled = item.id == null || updatingItems[item.id];
                            return (
                              <li className={`shopping-item${checked ? ' is-bought' : ''}`} key={item.id ?? `${item.nome}-${index}`}>
                                <label className="check-label">
                                  <input type="checkbox" checked={checked} disabled={disabled} onChange={() => toggleItem(item)} aria-label={`${checked ? 'Desmarcar' : 'Marcar como comprado'}: ${item.nome}`} />
                                  <span className="custom-check" aria-hidden="true">{checked ? '✓' : ''}</span>
                                  <span className="shopping-name">{item.nome}</span>
                                  <strong className="shopping-amount">{formatAmount(item.quantidadeTotal)} {item.unidadeMedida}</strong>
                                </label>
                                {item.id == null && <span className="item-warning">Este item não possui identificador para atualização.</span>}
                              </li>
                            );
                          })}
                        </ul>
                      </section>
                    ))}
                  </div>
                )}
              </div>
            )}
          </section>
        </>
      )}
    </div>
  );
}
