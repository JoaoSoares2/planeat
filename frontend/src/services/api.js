import { demoRequest } from './demoApi.js';

const configuredBaseUrl = import.meta.env.VITE_API_BASE_URL?.trim();
const API_BASE_URL = (configuredBaseUrl || '/api').replace(/\/+$/, '');
export const isDemoMode = import.meta.env.VITE_DEMO_MODE === 'true';

export async function fetchJson(endpoint, options = {}) {
  if (isDemoMode) return demoRequest(endpoint, options);

  const headers = new Headers(options.headers || {});
  if (options.body && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json');
  }

  let response;
  try {
    response = await fetch(`${API_BASE_URL}${endpoint}`, {
      ...options,
      headers,
    });
  } catch (error) {
    throw new Error(`Não foi possível conectar à API. Verifique se o back-end está ativo. (${error.message})`);
  }

  if (response.status === 204) return null;

  const responseText = await response.text();
  let data = null;
  if (responseText) {
    try {
      data = JSON.parse(responseText);
    } catch {
      if (response.ok) throw new Error('A API retornou uma resposta que não é um JSON válido.');
    }
  }

  if (!response.ok) {
    throw new Error(data?.erro || data?.mensagem || `Falha na requisição (${response.status}).`);
  }
  return data;
}
