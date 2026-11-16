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

export const listarPlanejamentos = () => fetchArray('/planejamentos');
export const buscarPlanejamento = (id) => fetchObject(`/planejamentos/${id}`);
export const criarPlanejamento = (plano) => fetchObject('/planejamentos', {
  method: 'POST',
  body: JSON.stringify(plano),
});
export const excluirPlanejamento = (id) => fetchJson(`/planejamentos/${id}`, { method: 'DELETE' });
export const buscarListaCompras = (id) => fetchObject(`/planejamentos/${id}/lista-compras`);
export const buscarRelatorioDesperdicio = (id) => fetchObject(`/planejamentos/${id}/desperdicio`);
export const atualizarStatusItem = (id, comprado) => fetchJson(`/itens-lista/${id}/status`, {
  method: 'PATCH',
  body: JSON.stringify({ comprado }),
});
