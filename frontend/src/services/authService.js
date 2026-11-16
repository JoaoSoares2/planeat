import { fetchJson } from './api.js';

export const login = (email, senha) => fetchJson('/auth/login', {
  method: 'POST',
  body: JSON.stringify({ email, senha }),
});

export const cadastro = (dados) => fetchJson('/auth/cadastro', {
  method: 'POST',
  body: JSON.stringify(dados),
});

export const logout = () => fetchJson('/auth/logout', { method: 'POST' });

export const verificarSessao = () => fetchJson('/auth/sessao');
