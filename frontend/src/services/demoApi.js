const ingredients = [
  { id: 1, nome: 'Cebola roxa', categoria: { id: 1, nome: 'Hortifrúti' }, unidadePadrao: 'g' },
  { id: 2, nome: 'Tomate italiano', categoria: { id: 1, nome: 'Hortifrúti' }, unidadePadrao: 'g' },
  { id: 3, nome: 'Peito de frango', categoria: { id: 2, nome: 'Açougue & Peixaria' }, unidadePadrao: 'g' },
  { id: 4, nome: 'Arroz integral', categoria: { id: 3, nome: 'Mercearia & Grãos' }, unidadePadrao: 'g' },
  { id: 5, nome: 'Azeite de oliva', categoria: { id: 3, nome: 'Mercearia & Grãos' }, unidadePadrao: 'ml' },
  { id: 6, nome: 'Ovos caipiras', categoria: { id: 4, nome: 'Laticínios & Ovos' }, unidadePadrao: 'un' },
  { id: 7, nome: 'Abobrinha', categoria: { id: 1, nome: 'Hortifrúti' }, unidadePadrao: 'g' },
  { id: 8, nome: 'Aveia em flocos', categoria: { id: 3, nome: 'Mercearia & Grãos' }, unidadePadrao: 'g' },
];

const clone = (value) => JSON.parse(JSON.stringify(value));
const recipes = [
  {
    id: 1,
    usuarioId: 1,
    nome: 'Frango com tomate e cebola',
    modoPreparo: 'Tempere o frango. Grelhe até dourar e refogue com tomate e cebola. Sirva com arroz integral.',
    porcoes: 2,
    tempoPreparoMinutos: 30,
    tipoRefeicao: 'Almoço',
    ingredientes: [
      { ingredienteId: 3, quantidade: 350, unidadeMedida: 'g' },
      { ingredienteId: 1, quantidade: 80, unidadeMedida: 'g' },
      { ingredienteId: 2, quantidade: 120, unidadeMedida: 'g' },
      { ingredienteId: 4, quantidade: 160, unidadeMedida: 'g' },
      { ingredienteId: 5, quantidade: 10, unidadeMedida: 'ml' },
    ],
  },
  {
    id: 2,
    usuarioId: 1,
    nome: 'Omelete de tomate e abobrinha',
    modoPreparo: 'Bata os ovos, misture os vegetais picados e cozinhe em frigideira antiaderente até firmar.',
    porcoes: 1,
    tempoPreparoMinutos: 15,
    tipoRefeicao: 'Jantar',
    ingredientes: [
      { ingredienteId: 6, quantidade: 2, unidadeMedida: 'un' },
      { ingredienteId: 1, quantidade: 30, unidadeMedida: 'g' },
      { ingredienteId: 2, quantidade: 60, unidadeMedida: 'g' },
      { ingredienteId: 7, quantidade: 80, unidadeMedida: 'g' },
    ],
  },
  {
    id: 3,
    usuarioId: 1,
    nome: 'Aveia com banana',
    modoPreparo: 'Misture a aveia com leite ou água e finalize com banana fatiada.',
    porcoes: 1,
    tempoPreparoMinutos: 8,
    tipoRefeicao: 'Café da manhã',
    ingredientes: [
      { ingredienteId: 8, quantidade: 45, unidadeMedida: 'g' },
      { ingredienteId: 7, quantidade: 100, unidadeMedida: 'g' },
    ],
  },
].map((recipe) => ({ ...recipe, ingredientes: recipe.ingredientes.map(enrichRecipeIngredient) }));

function enrichRecipeIngredient(item) {
  const ingredient = ingredients.find((option) => option.id === Number(item.ingredienteId));
  return {
    ...item,
    nomeIngrediente: ingredient?.nome || '',
    categoria: ingredient?.categoria?.nome || '',
  };
}

function weekDates() {
  const date = new Date();
  const offset = (date.getDay() || 7) - 1;
  const start = new Date(date.getFullYear(), date.getMonth(), date.getDate() - offset);
  const end = new Date(start.getFullYear(), start.getMonth(), start.getDate() + 6);
  const format = (item) => {
    const localDate = new Date(item.getTime() - item.getTimezoneOffset() * 60000);
    return localDate.toISOString().slice(0, 10);
  };
  return { dataInicio: format(start), dataFim: format(end) };
}

const dates = weekDates();
let plans = [{
  id: 1,
  usuarioId: 1,
  titulo: 'Cardápio leve da semana',
  ...dates,
  status: 'ATIVO',
  refeicoes: [
    { id: 1, receitaId: 1, nomeReceita: recipes[0].nome, diaSemana: 'Segunda-feira', tipoRefeicao: 'Almoço' },
    { id: 2, receitaId: 2, nomeReceita: recipes[1].nome, diaSemana: 'Segunda-feira', tipoRefeicao: 'Jantar' },
    { id: 3, receitaId: 1, nomeReceita: recipes[0].nome, diaSemana: 'Quarta-feira', tipoRefeicao: 'Almoço' },
    { id: 4, receitaId: 3, nomeReceita: recipes[2].nome, diaSemana: 'Sábado', tipoRefeicao: 'Jantar' },
  ],
}];
let nextRecipeId = 4;
let nextPlanId = 2;
let nextMealId = 5;
let nextShoppingItemId = 1;
const shoppingLists = new Map();

function createShoppingList(planId) {
  const plan = plans.find((item) => item.id === Number(planId));
  if (!plan) throw new Error('Planejamento não encontrado no modo demonstração.');

  const aggregated = new Map();
  plan.refeicoes.forEach((meal) => {
    const recipe = recipes.find((item) => item.id === Number(meal.receitaId));
    recipe?.ingredientes.forEach((item) => {
      const key = `${item.ingredienteId}_${item.unidadeMedida.toLowerCase()}`;
      const current = aggregated.get(key);
      if (current) current.quantidadeTotal = Math.round((current.quantidadeTotal + Number(item.quantidade)) * 100) / 100;
      else {
        const ingredient = ingredients.find((option) => option.id === Number(item.ingredienteId));
        aggregated.set(key, {
          id: nextShoppingItemId++,
          ingredienteId: ingredient.id,
          nome: ingredient.nome,
          categoria: ingredient.categoria?.nome || 'Outros',
          quantidadeTotal: Number(item.quantidade),
          unidadeMedida: item.unidadeMedida,
          comprado: false,
        });
      }
    });
  });

  const previous = shoppingLists.get(Number(planId));
  const previousStatus = new Map((previous?.itens || []).map((item) => [`${item.ingredienteId}_${item.unidadeMedida.toLowerCase()}`, item.comprado]));
  const itens = [...aggregated.entries()].map(([key, item]) => ({
    ...item,
    comprado: previousStatus.get(key) ?? item.comprado,
  }));
  const result = { planejamentoId: plan.id, titulo: plan.titulo, totalItens: itens.length, itens };
  shoppingLists.set(plan.id, result);
  return result;
}

function makeWasteReport(planId) {
  const plan = plans.find((item) => item.id === Number(planId));
  if (!plan) throw new Error('Planejamento não encontrado no modo demonstração.');
  const counts = new Map();
  plan.refeicoes.forEach((meal) => {
    recipes.find((item) => item.id === Number(meal.receitaId))?.ingredientes.forEach((item) => {
      const ingredient = ingredients.find((option) => option.id === Number(item.ingredienteId));
      const key = `${item.ingredienteId}_${item.unidadeMedida}`;
      const entry = counts.get(key) || { name: ingredient.nome, category: ingredient.categoria?.nome || 'Outros', frequency: 0, amount: 0, unit: item.unidadeMedida };
      entry.frequency += 1;
      entry.amount += Number(item.quantidade);
      counts.set(key, entry);
    });
  });
  const rows = [...counts.values()];
  const reused = rows.filter((item) => item.frequency > 1).length;
  return {
    planejamentoId: plan.id,
    tituloPlanejamento: plan.titulo,
    totalRefeicoesPlanejadas: plan.refeicoes.length,
    totalIngredientesDistintos: rows.length,
    totalItensReaproveitados: reused,
    taxaAproveitamentoPercentual: rows.length ? Math.round((reused / rows.length) * 1000) / 10 : 0,
    itensAproveitamento: rows.map((item) => ({
      nomeIngrediente: item.name,
      categoria: item.category,
      frequenciaCardapio: item.frequency,
      consumoTotalPlanejado: item.amount,
      unidadeMedida: item.unit,
      indicadorConsumoConsciente: item.frequency > 1 ? 'Alto Aproveitamento (Desperdício Zero)' : 'Item Pontual',
    })),
  };
}

function parseBody(options) {
  if (!options.body) return {};
  try {
    return JSON.parse(options.body);
  } catch {
    throw new Error('Não foi possível interpretar os dados enviados no modo demonstração.');
  }
}

export async function demoRequest(endpoint, options = {}) {
  await new Promise((resolve) => window.setTimeout(resolve, 120));
  const method = (options.method || 'GET').toUpperCase();
  const path = endpoint.split('?')[0];

  if (path === '/ingredientes' && method === 'GET') return clone(ingredients);
  if (path === '/receitas' && method === 'GET') return clone(recipes);
  if (path === '/receitas' && method === 'POST') {
    const payload = parseBody(options);
    const created = { ...payload, id: nextRecipeId++, ingredientes: (payload.ingredientes || []).map(enrichRecipeIngredient) };
    recipes.push(created);
    return clone(created);
  }

  const recipeMatch = path.match(/^\/receitas\/(\d+)$/);
  if (recipeMatch) {
    const id = Number(recipeMatch[1]);
    const index = recipes.findIndex((item) => item.id === id);
    if (index < 0) throw new Error('Receita não encontrada no modo demonstração.');
    if (method === 'GET') return clone(recipes[index]);
    if (method === 'PUT') {
      const payload = parseBody(options);
      recipes[index] = { ...recipes[index], ...payload, id, ingredientes: (payload.ingredientes || []).map(enrichRecipeIngredient) };
      return clone(recipes[index]);
    }
    if (method === 'DELETE') {
      recipes.splice(index, 1);
      plans = plans.map((plan) => ({
        ...plan,
        refeicoes: plan.refeicoes.filter((meal) => Number(meal.receitaId) !== id),
      }));
      plans.forEach((plan) => {
        if (shoppingLists.has(plan.id)) createShoppingList(plan.id);
      });
      return { mensagem: `Receita com ID ${id} excluída com sucesso.` };
    }
  }

  if (path === '/planejamentos' && method === 'GET') return clone(plans);
  if (path === '/planejamentos' && method === 'POST') {
    const payload = parseBody(options);
    const created = {
      ...payload,
      id: nextPlanId++,
      refeicoes: (payload.refeicoes || []).map((meal) => ({ ...meal, id: nextMealId++ })),
    };
    plans.unshift(created);
    createShoppingList(created.id);
    return clone(created);
  }

  const listMatch = path.match(/^\/planejamentos\/(\d+)\/lista-compras$/);
  if (listMatch && method === 'GET') {
    const planId = Number(listMatch[1]);
    return clone(shoppingLists.get(planId) || createShoppingList(planId));
  }
  const wasteMatch = path.match(/^\/planejamentos\/(\d+)\/desperdicio$/);
  if (wasteMatch && method === 'GET') return makeWasteReport(wasteMatch[1]);

  const planMatch = path.match(/^\/planejamentos\/(\d+)$/);
  if (planMatch) {
    const id = Number(planMatch[1]);
    const index = plans.findIndex((item) => item.id === id);
    if (index < 0) throw new Error('Planejamento não encontrado no modo demonstração.');
    if (method === 'GET') return clone(plans[index]);
    if (method === 'DELETE') {
      plans.splice(index, 1);
      shoppingLists.delete(id);
      return { mensagem: `Planejamento com ID ${id} excluído com sucesso.` };
    }
  }

  const itemMatch = path.match(/^\/itens-lista\/(\d+)\/status$/);
  if (itemMatch && method === 'PATCH') {
    const id = Number(itemMatch[1]);
    const payload = parseBody(options);
    if (typeof payload.comprado !== 'boolean') throw new Error('O status de compra precisa ser booleano.');
    for (const list of shoppingLists.values()) {
      const item = list.itens.find((entry) => entry.id === id);
      if (item) {
        item.comprado = payload.comprado;
        return { id, comprado: item.comprado };
      }
    }
    throw new Error('Item da lista não encontrado no modo demonstração.');
  }

  throw new Error(`Rota não simulada: ${method} ${path}`);
}
