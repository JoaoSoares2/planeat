const configuredBaseUrl = import.meta.env.VITE_API_BASE_URL?.trim();
const API_BASE_URL = (configuredBaseUrl || '/api').replace(/\/+$/, '');

class ApiError extends Error {
  constructor(message, status) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
  }
}

export async function fetchJson(endpoint, options = {}) {
  const headers = new Headers(options.headers || {});
  if (!headers.has('X-Requested-With')) headers.set('X-Requested-With', 'XMLHttpRequest');
  if (options.body && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json');
  }

  let response;
  try {
    response = await fetch(`${API_BASE_URL}${endpoint}`, {
      ...options,
      headers,
      credentials: 'include',
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
    const message = data?.erro || data?.mensagem || `Falha na requisição (${response.status}).`;
    if ((response.status === 401 || response.status === 403) && !endpoint.startsWith('/auth/')) {
      window.dispatchEvent(new CustomEvent('planeat:session-expired'));
    }
    throw new ApiError(message, response.status);
  }
  return data;
}
