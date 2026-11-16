import { fetchJson } from './api.js';

async function fetchArray(endpoint) {
  const result = await fetchJson(endpoint);
  if (!Array.isArray(result)) throw new Error(`A API retornou uma lista inválida para ${endpoint}.`);
  return result;
}

async function fetchObject(endpoint, options) {
  const result = await fetchJson(endpoint, options);
  if (!result || typeof result !== 'object' || Array.isArray(result)) {
    throw new Error(`A API retornou dados inválidos para ${endpoint}.`);
  }
  return result;
}

export const listarReceitas = () => fetchArray('/receitas');
export const buscarReceita = (id) => fetchObject(`/receitas/${id}`);
export const listarIngredientes = () => fetchArray('/ingredientes');
export const criarReceita = (receita) => fetchObject('/receitas', {
  method: 'POST',
  body: JSON.stringify(receita),
});
export const atualizarReceita = (id, receita) => fetchObject(`/receitas/${id}`, {
  method: 'PUT',
  body: JSON.stringify(receita),
});
export const excluirReceita = (id) => fetchJson(`/receitas/${id}`, { method: 'DELETE' });
