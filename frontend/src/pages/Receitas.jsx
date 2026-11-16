import { useCallback, useEffect, useMemo, useState } from 'react';
import Modal from '../components/Modal.jsx';
import { useToast } from '../components/Toast.jsx';
import {
  atualizarReceita,
  buscarReceita,
  criarReceita,
  excluirReceita,
  listarIngredientes,
  listarReceitas,
} from '../services/receitaService.js';

const EMPTY_FORM = {
  nome: '',
  modoPreparo: '',
  porcoes: '',
  tempoPreparoMinutos: '',
  tipoRefeicao: 'Almoço',
  ingredientes: [{ ingredienteId: '', quantidade: '', unidadeMedida: 'g' }],
};
const UNITS = ['g', 'kg', 'ml', 'l', 'un', 'colher de sopa', 'colher de chá', 'xícara'];

function normalizeIngredient(item) {
  return {
    ingredienteId: item.ingredienteId ?? item.id ?? item.ingrediente?.id ?? '',
    nomeIngrediente: item.nomeIngrediente ?? item.nome ?? item.ingrediente?.nome ?? '',
    categoria: item.categoria?.nome ?? item.categoria ?? item.ingrediente?.categoria?.nome ?? '',
    quantidade: item.quantidade ?? '',
    unidadeMedida: item.unidadeMedida ?? item.ingrediente?.unidadePadrao ?? 'g',
  };
}

function asForm(receita) {
  return {
    nome: receita.nome || '',
    modoPreparo: receita.modoPreparo || '',
    porcoes: receita.porcoes ?? '',
    tempoPreparoMinutos: receita.tempoPreparoMinutos ?? '',
    tipoRefeicao: receita.tipoRefeicao || 'Almoço',
    ingredientes: (receita.ingredientes || []).map(normalizeIngredient),
  };
}

function validate(form) {
  const errors = {};
  if (!form.nome.trim()) errors.nome = 'Informe o nome da receita.';
  if (!form.modoPreparo.trim()) errors.modoPreparo = 'Descreva como preparar a receita.';
  if (!Number.isFinite(Number(form.porcoes)) || Number(form.porcoes) <= 0) errors.porcoes = 'Use um número maior que zero.';
  if (!Number.isFinite(Number(form.tempoPreparoMinutos)) || Number(form.tempoPreparoMinutos) <= 0) errors.tempoPreparoMinutos = 'Use um número maior que zero.';
  if (!form.ingredientes.length || form.ingredientes.some((item) => !item.ingredienteId)) errors.ingredientes = 'Selecione ao menos um ingrediente do catálogo.';
  form.ingredientes.forEach((item, index) => {
    if (!Number.isFinite(Number(item.quantidade)) || Number(item.quantidade) <= 0) {
      errors[`quantidade-${index}`] = 'A quantidade deve ser maior que zero.';
    }
    if (!item.unidadeMedida) errors[`unidade-${index}`] = 'Selecione uma unidade.';
  });
  return errors;
}

export default function Receitas() {
  const { notify } = useToast();
  const [recipes, setRecipes] = useState([]);
  const [ingredients, setIngredients] = useState([]);
  const [loading, setLoading] = useState(true);
  const [catalogLoading, setCatalogLoading] = useState(true);
  const [error, setError] = useState('');
  const [catalogError, setCatalogError] = useState('');
  const [formOpen, setFormOpen] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [form, setForm] = useState(EMPTY_FORM);
  const [errors, setErrors] = useState({});
  const [saving, setSaving] = useState(false);
  const [viewing, setViewing] = useState(null);
  const [deleting, setDeleting] = useState(null);
  const [deleteLoading, setDeleteLoading] = useState(false);

  const loadRecipes = useCallback(async () => {
    setLoading(true);
    try {
      const data = await listarReceitas();
      setRecipes(data);
      setError('');
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadRecipes();
    listarIngredientes()
      .then((data) => setIngredients(data))
      .catch((requestError) => setCatalogError(requestError.message))
      .finally(() => setCatalogLoading(false));
  }, [loadRecipes]);

  const sortedRecipes = useMemo(() => [...recipes].sort((a, b) => (a.nome || '').localeCompare(b.nome || '', 'pt-BR')), [recipes]);
  const openNewForm = () => {
    setEditingId(null);
    setForm({ ...EMPTY_FORM, ingredientes: [{ ...EMPTY_FORM.ingredientes[0] }] });
    setErrors({});
    setFormOpen(true);
  };

  async function openEditForm(recipe) {
    try {
      const fullRecipe = await buscarReceita(recipe.id);
      setEditingId(recipe.id);
      setForm(asForm(fullRecipe));
      setErrors({});
      setFormOpen(true);
    } catch (requestError) {
      notify(requestError.message, 'error');
    }
  }

  function updateField(event) {
    const { name, value } = event.target;
    setForm((current) => ({ ...current, [name]: value }));
    setErrors((current) => ({ ...current, [name]: '' }));
  }

  function updateIngredient(index, field, value) {
    setForm((current) => ({
      ...current,
      ingredientes: current.ingredientes.map((item, itemIndex) => {
        if (itemIndex !== index) return item;
        if (field === 'ingredienteId') {
          const ingredient = ingredients.find((option) => String(option.id) === value);
          return {
            ...item,
            ingredienteId: value,
            nomeIngrediente: ingredient?.nome || '',
            categoria: ingredient?.categoria?.nome || ingredient?.categoria || '',
            unidadeMedida: ingredient?.unidadePadrao || item.unidadeMedida || 'g',
          };
        }
        return { ...item, [field]: value };
      }),
    }));
    setErrors((current) => ({ ...current, [`${field}-${index}`]: '', ingredientes: '' }));
  }

  function addIngredient() {
    setForm((current) => ({
      ...current,
      ingredientes: [...current.ingredientes, { ingredienteId: '', quantidade: '', unidadeMedida: 'g' }],
    }));
  }

  function removeIngredient(index) {
    setForm((current) => ({ ...current, ingredientes: current.ingredientes.filter((_, itemIndex) => itemIndex !== index) }));
    setErrors((current) => ({ ...current, ingredientes: '' }));
  }

  async function handleSubmit(event) {
    event.preventDefault();
    const validationErrors = validate(form);
    setErrors(validationErrors);
    if (Object.values(validationErrors).some(Boolean)) return;

    const payload = {
      usuarioId: 1,
      nome: form.nome.trim(),
      modoPreparo: form.modoPreparo.trim(),
      porcoes: Number(form.porcoes),
      tempoPreparoMinutos: Number(form.tempoPreparoMinutos),
      tipoRefeicao: form.tipoRefeicao,
      ingredientes: form.ingredientes.map((item) => ({
        ingredienteId: Number(item.ingredienteId),
        quantidade: Number(item.quantidade),
        unidadeMedida: item.unidadeMedida,
      })),
    };

    setSaving(true);
    try {
      if (editingId) {
        await atualizarReceita(editingId, payload);
        notify('Receita atualizada com sucesso.');
      } else {
        await criarReceita(payload);
        notify('Receita cadastrada com sucesso.');
      }
      setFormOpen(false);
      await loadRecipes();
    } catch (requestError) {
      notify(requestError.message, 'error');
    } finally {
      setSaving(false);
    }
  }

  async function handleDelete() {
    setDeleteLoading(true);
    try {
      await excluirReceita(deleting.id);
      setRecipes((current) => current.filter((recipe) => recipe.id !== deleting.id));
      setDeleting(null);
      notify('Receita excluída com sucesso.');
    } catch (requestError) {
      notify(requestError.message, 'error');
    } finally {
      setDeleteLoading(false);
    }
  }

  async function showDetails(recipe) {
    try {
      setViewing(await buscarReceita(recipe.id));
    } catch (requestError) {
      notify(requestError.message, 'error');
    }
  }

  return (
    <div className="page recipes-page">
      <section className="page-intro">
        <div>
          <span className="eyebrow">SEU REPERTÓRIO, COM CARINHO</span>
          <h1>Receitas que fazem <span>bem.</span></h1>
          <p>Organize seus pratos favoritos e tenha tudo à mão na hora de planejar a semana.</p>
        </div>
        <button className="button button-primary" type="button" onClick={openNewForm}>
          <span aria-hidden="true">＋</span> Nova receita
        </button>
      </section>

      <section className="catalog-section" aria-labelledby="catalog-title">
        <div className="section-heading">
          <div>
            <span className="eyebrow">CATÁLOGO DA CASA</span>
            <h2 id="catalog-title">Suas receitas <span className="count-pill">{recipes.length}</span></h2>
          </div>
          <button className="button button-quiet" type="button" onClick={loadRecipes} disabled={loading}>
            {loading ? 'Atualizando…' : '↻ Atualizar'}
          </button>
        </div>

        {error && <div className="notice notice-error" role="alert">{error}</div>}
        {catalogError && <div className="notice notice-error" role="alert">Não foi possível carregar o catálogo de ingredientes: {catalogError}</div>}
        {loading && <div className="loading-panel"><span className="spinner" /> Carregando suas receitas…</div>}
        {!loading && !error && !sortedRecipes.length && (
          <div className="empty-state">
            <span className="empty-illustration" aria-hidden="true">♡</span>
            <h3>Sua coleção começa aqui</h3>
            <p>Cadastre uma receita para montar seu primeiro cardápio.</p>
            <button type="button" className="button button-primary" onClick={openNewForm}>Cadastrar primeira receita</button>
          </div>
        )}
        {!loading && sortedRecipes.length > 0 && (
          <div className="recipe-grid">
            {sortedRecipes.map((recipe, index) => (
              <article className="recipe-card" key={recipe.id}>
                <div className={`recipe-art recipe-art-${index % 4}`} aria-hidden="true">
                  <span>{['🥗', '🍲', '🥑', '🍋'][index % 4]}</span>
                  <span className="recipe-kind">{recipe.tipoRefeicao || 'Refeição'}</span>
                </div>
                <div className="recipe-card-content">
                  <h3>{recipe.nome}</h3>
                  <div className="recipe-meta">
                    <span><b aria-hidden="true">◷</b> {recipe.tempoPreparoMinutos ?? '—'} min</span>
                    <span><b aria-hidden="true">♧</b> {recipe.porcoes ?? '—'} porções</span>
                  </div>
                  <p className="ingredient-summary">
                    {(recipe.ingredientes || []).slice(0, 3).map((item) => item.nomeIngrediente || item.nome || item.ingrediente?.nome).filter(Boolean).join(' · ') || 'Ingredientes disponíveis nos detalhes'}
                    {(recipe.ingredientes || []).length > 3 ? ` +${recipe.ingredientes.length - 3}` : ''}
                  </p>
                  <div className="recipe-actions">
                    <button className="button button-small button-outline" type="button" onClick={() => showDetails(recipe)}>Ver receita</button>
                    <button className="icon-button" type="button" aria-label={`Editar ${recipe.nome}`} onClick={() => openEditForm(recipe)}>✎</button>
                    <button className="icon-button danger-icon" type="button" aria-label={`Excluir ${recipe.nome}`} onClick={() => setDeleting(recipe)}>⌫</button>
                  </div>
                </div>
              </article>
            ))}
          </div>
        )}
      </section>

      {formOpen && (
        <Modal title={editingId ? 'Editar receita' : 'Nova receita'} onClose={() => setFormOpen(false)} className="recipe-modal">
          {catalogLoading ? (
            <div className="loading-panel"><span className="spinner" /> Carregando catálogo de ingredientes…</div>
          ) : ingredients.length === 0 ? (
            <div className="notice notice-warning">
              {catalogError || 'O catálogo de ingredientes está vazio. Cadastre ingredientes no back-end para criar receitas.'}
            </div>
          ) : (
            <form className="recipe-form" onSubmit={handleSubmit} noValidate>
              <div className="form-grid">
                <label className="field field-wide">
                  Nome da receita
                  <input name="nome" value={form.nome} onChange={updateField} aria-invalid={Boolean(errors.nome)} aria-describedby={errors.nome ? 'error-nome' : undefined} placeholder="Ex.: Bowl de legumes" />
                  {errors.nome && <span className="field-error" id="error-nome">{errors.nome}</span>}
                </label>
                <label className="field">
                  Tipo de refeição
                  <select name="tipoRefeicao" value={form.tipoRefeicao} onChange={updateField}>
                    {['Café da manhã', 'Almoço', 'Jantar', 'Lanche'].map((type) => <option key={type}>{type}</option>)}
                  </select>
                </label>
                <label className="field">
                  Rendimento (porções)
                  <input name="porcoes" type="number" min="1" step="1" value={form.porcoes} onChange={updateField} aria-invalid={Boolean(errors.porcoes)} />
                  {errors.porcoes && <span className="field-error">{errors.porcoes}</span>}
                </label>
                <label className="field">
                  Tempo de preparo (min)
                  <input name="tempoPreparoMinutos" type="number" min="1" step="1" value={form.tempoPreparoMinutos} onChange={updateField} aria-invalid={Boolean(errors.tempoPreparoMinutos)} />
                  {errors.tempoPreparoMinutos && <span className="field-error">{errors.tempoPreparoMinutos}</span>}
                </label>
                <label className="field field-wide">
                  Modo de preparo
                  <textarea name="modoPreparo" rows="4" value={form.modoPreparo} onChange={updateField} aria-invalid={Boolean(errors.modoPreparo)} placeholder="Conte o passo a passo…" />
                  {errors.modoPreparo && <span className="field-error">{errors.modoPreparo}</span>}
                </label>
              </div>
              <div className="ingredient-editor">
                <div className="ingredient-editor-heading">
                  <div><h3>Ingredientes</h3><p>Escolha no catálogo oficial do PlanEat.</p></div>
                  <button className="button button-small button-outline" type="button" onClick={addIngredient}>＋ Adicionar</button>
                </div>
                {form.ingredientes.map((item, index) => (
                  <div className="ingredient-row" key={`ingredient-${index}`}>
                    <label className="field ingredient-select">
                      <span className="sr-only">Ingrediente {index + 1}</span>
                      <select value={item.ingredienteId} onChange={(event) => updateIngredient(index, 'ingredienteId', event.target.value)} aria-label={`Selecionar ingrediente ${index + 1}`} aria-invalid={Boolean(errors.ingredientes)}>
                        <option value="">Selecione ingrediente</option>
                        {ingredients.map((option) => <option key={option.id} value={option.id}>{option.nome}{option.categoria?.nome ? ` · ${option.categoria.nome}` : ''}</option>)}
                      </select>
                    </label>
                    <label className="field ingredient-quantity">
                      <span className="sr-only">Quantidade do ingrediente {index + 1}</span>
                      <input type="number" min="0.01" step="0.01" placeholder="Qtd." value={item.quantidade} onChange={(event) => updateIngredient(index, 'quantidade', event.target.value)} aria-label={`Quantidade de ${item.nomeIngrediente || `ingrediente ${index + 1}`}`} aria-invalid={Boolean(errors[`quantidade-${index}`])} />
                    </label>
                    <label className="field ingredient-unit">
                      <span className="sr-only">Unidade do ingrediente {index + 1}</span>
                      <select value={item.unidadeMedida} onChange={(event) => updateIngredient(index, 'unidadeMedida', event.target.value)} aria-label={`Unidade de ${item.nomeIngrediente || `ingrediente ${index + 1}`}`}>
                        {!UNITS.includes(item.unidadeMedida) && item.unidadeMedida && <option>{item.unidadeMedida}</option>}
                        {UNITS.map((unit) => <option key={unit}>{unit}</option>)}
                      </select>
                    </label>
                    <button className="icon-button danger-icon remove-ingredient" type="button" aria-label={`Remover ingrediente ${index + 1}`} onClick={() => removeIngredient(index)}>×</button>
                    {errors[`quantidade-${index}`] && <span className="field-error ingredient-error">{errors[`quantidade-${index}`]}</span>}
                  </div>
                ))}
                {errors.ingredientes && <span className="field-error">{errors.ingredientes}</span>}
              </div>
              <div className="modal-actions">
                <button className="button button-quiet" type="button" onClick={() => setFormOpen(false)}>Cancelar</button>
                <button className="button button-primary" type="submit" disabled={saving}>{saving ? 'Salvando…' : editingId ? 'Salvar alterações' : 'Cadastrar receita'}</button>
              </div>
            </form>
          )}
        </Modal>
      )}

      {viewing && (
        <Modal title={viewing.nome} onClose={() => setViewing(null)}>
          <div className="recipe-detail">
            <div className="detail-meta"><span>◷ {viewing.tempoPreparoMinutos} minutos</span><span>♧ {viewing.porcoes} porções</span><span>{viewing.tipoRefeicao}</span></div>
            <h3>Ingredientes</h3>
            <ul className="detail-ingredients">
              {(viewing.ingredientes || []).map((item, index) => (
                <li key={`${item.ingredienteId}-${index}`}>
                  <span>{item.nomeIngrediente || item.nome || item.ingrediente?.nome}</span>
                  <b>{item.quantidade} {item.unidadeMedida}</b>
                </li>
              ))}
            </ul>
            <h3>Modo de preparo</h3>
            <p className="preparation-text">{viewing.modoPreparo}</p>
          </div>
        </Modal>
      )}

      {deleting && (
        <Modal title="Excluir receita?" onClose={() => setDeleting(null)} className="confirm-modal">
          <p>Tem certeza de que deseja excluir <strong>{deleting.nome}</strong>? Essa ação não pode ser desfeita.</p>
          <div className="modal-actions">
            <button className="button button-quiet" type="button" onClick={() => setDeleting(null)}>Cancelar</button>
            <button className="button button-danger" type="button" onClick={handleDelete} disabled={deleteLoading}>{deleteLoading ? 'Excluindo…' : 'Excluir receita'}</button>
          </div>
        </Modal>
      )}
    </div>
  );
}
