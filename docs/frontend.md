# Especificação do Front-end: PlanEat

> **Global Solution 2026/2 — CitizenTech (FIAP)**  
> **Sistema:** PlanEat — Interface Web SPA do Cidadão  
> **Tecnologia:** React 18+ com Vite e React Router  
> **Abordagem de Design:** Mobile-First, Alta Usabilidade Tátil e Acessibilidade  

---

## 1. Visão Geral e Experiência do Usuário (UX)

O front-end do **PlanEat** foi desenvolvido em **React** com foco direto no cidadão, priorizando a usabilidade móvel. A proposta central é acompanhar o usuário em dois momentos distintos:
1. **Em casa (Planejamento):** Interface intuitiva para catalogar receitas e estruturar o cardápio semanal sem fricção;
2. **No supermercado (Execução):** Interface ágil, com botões amplos, fontes legíveis e **checkboxes interativos** operáveis com uma mão só enquanto o usuário caminha pelos corredores.

### Requisitos Atendidos da Avaliação:
* **Mínimo de 3 visões interligadas:** Roteamento via React Router unificando Dashboard, Receitas e Planejamento/Lista de Compras;
* **Navegação Uniforme:** Cabeçalho com menu fixo e responsivo persistente em todas as telas;
* **Validação de Formulários:** Validações de entradas em tempo real com alertas visuais;
* **Diferencial Global Solution (Web Conectada):** Integração real com o back-end Java via chamadas HTTP REST (`fetch`/`axios`), lendo e gravando no banco relacional.

---

## 2. Estrutura de Pastas do Projeto React

```
planeat-frontend/
├── public/                     # Favicon e assets públicos
├── src/
│   ├── assets/                 # Imagens, ícones SVG e ilustrações ODS
│   ├── components/             # Componentes reutilizáveis
│   │   ├── Navbar.jsx          # Barra de navegação unificada
│   │   ├── Footer.jsx          # Rodapé com menção CitizenTech e ODS
│   │   ├── CardImpacto.jsx     # Card métrico (desperdício, economia)
│   │   ├── CardReceita.jsx     # Card de apresentação de receita
│   │   ├── DosadorIngrediente.jsx # Seletor dinâmico de insumos na receita
│   │   └── ItemChecklist.jsx   # Item de lista com checkbox tátil
│   ├── pages/                  # As 3 visões principais (Telas)
│   │   ├── Dashboard.jsx       # Home / Visão Geral e Métricas
│   │   ├── Receitas.jsx        # Catálogo e CRUD de Receitas
│   │   └── Planejamento.jsx    # Cardápio Semanal e Lista de Compras Inteligente
│   ├── services/               # Comunicação com a API REST Java
│   │   ├── api.js              # Instância base do Axios ou Fetch Wrapper
│   │   ├── receitaService.js   # Funções de busca, criação e exclusão
│   │   └── planoService.js     # Geração de lista e status de compras
│   ├── styles/                 # Estilização global e componentes
│   │   ├── index.css           # Reset e variáveis de cor
│   │   └── responsive.css      # Regras de media query mobile-first
│   ├── App.jsx                 # Configuração de rotas (React Router)
│   └── main.jsx                # Ponto de entrada React (DOM Mount)
├── package.json
└── vite.config.js
```

---

## 3. Especificação Detalhada das Telas

```
┌────────────────────────────────────────────────────────────────────────┐
│  🌿 PlanEat          [ Dashboard ]   [ Receitas ]   [ Planejamento ]   │
├────────────────────────────────────────────────────────────────────────┤
│                                                                        │
│  CONTEÚDO DINÂMICO ESPECÍFICO DA TELA (REACT ROUTER OUTLET)           │
│                                                                        │
├────────────────────────────────────────────────────────────────────────┤
│  PlanEat © 2026 — CitizenTech FIAP — ODS 2 e 11                        │
└────────────────────────────────────────────────────────────────────────┘
```

### 3.1. Tela 1: `Dashboard.jsx` (Home & Métricas de Impacto)
* **Rota:** `/` ou `/dashboard`
* **Objetivo:** Engajar o cidadão com os conceitos do CitizenTech e evidenciar a economia financeira e sustentável gerada pelo planejamento.
* **Componentes e Seções:**
  1. **Hero Banner:** Chamada de impacto (*"O futuro sustentável começa na sua despensa"*), botão de ação rápida *"Montar Cardápio da Semana"*;
  2. **Métricas de Impacto Social (ODS 2 e 11):**
     * Indicador de perdas alimentares no Brasil (27M toneladas/ano);
     * Indicador do domicílio: estimativa de refeições planejadas e economia de alimentos;
  3. **Acesso Rápido:** Cards para acessar a última lista de compras ativa ou adicionar uma nova receita em segundos.

### 3.2. Tela 2: `Receitas.jsx` (Catálogo e CRUD de Receitas)
* **Rota:** `/receitas`
* **Objetivo:** Permitir ao cidadão gerenciar seu repertório de receitas caseiras.
* **Componentes e Funcionalidades:**
  1. **Formulário de Cadastro/Edição (`FormReceita`):**
     * Campos de texto: Nome da receita, modo de preparo detalhado;
     * Controles numéricos: Rendimento de porções (validação $> 0$), tempo em minutos;
     * Seletor de tipo de refeição: Café, Almoço, Jantar, Lanche;
     * **Seletor Dinâmico de Ingredientes:** Permite adicionar múltiplas linhas de ingredientes, escolhendo o item do catálogo, digitando a quantidade e a unidade de medida;
  2. **Grid de Receitas Cadastradas:**
     * Cards exibindo imagem/ícone, tempo de preparo, rendimento e resumo dos ingredientes;
     * Botão para visualizar modo de preparo completo (modal);
     * Botão de exclusão com diálogo de confirmação.

### 3.3. Tela 3: `Planejamento.jsx` (Cardápio Semanal & Lista Cirúrgica)
* **Rota:** `/planejamento`
* **Objetivo:** Estruturar o planejamento alimentar da semana e exibir a lista de compras gerada pelo motor do back-end.
* **Componentes e Funcionalidades:**
  1. **Quadro Semanal Interativo:**
     * Grade com os 7 dias da semana (Segunda a Domingo) e divisões de refeição (Almoço / Jantar);
     * Menus dropdown para escolher rapidamente qual receita será preparada em cada dia;
  2. **Gatilho de Inteligência:**
     * Botão de destaque: **"⚡ Gerar Lista de Compras Inteligente"**;
     * Dispara chamada assíncrona ao back-end, consolidando todos os insumos;
  3. **Lista de Compras no Supermercado (Checklist Mobile):**
     * Itens organizados por setor do supermercado (Hortifrúti, Açougue, Mercearia, Laticínios);
     * Exibição clara da soma exata: ex: `Cebola Roxa — 230g` (unificada de duas receitas);
     * **Checkbox tátil em tempo real:** Ao clicar, o item recebe estilo tachado e atualiza a flag `FL_COMPRADO` no banco de dados;
     * **Botão "Compartilhar no WhatsApp":** Copia a lista em texto legível para o clipboard para envio familiar.

---

## 4. Integração com a API REST Java

A comunicação com o back-end Java é centralizada no módulo de serviços utilizando `fetch` ou `axios`:

```javascript
// src/services/api.js
const API_BASE_URL = 'http://localhost:8080/api';

export async function fetchJson(endpoint, options = {}) {
  const response = await fetch(`${API_BASE_URL}${endpoint}`, {
    headers: {
      'Content-Type': 'application/json',
      ...options.headers,
    },
    ...options,
  });

  if (!response.ok) {
    const errorBody = await response.json().catch(() => ({}));
    throw new Error(errorBody.mensagem || `Erro na requisição: ${response.status}`);
  }

  return response.json();
}
```

### Exemplo de Consumo no Componente de Lista:
```javascript
// Chamada para gerar lista de compras consolidada
export async function obterListaCompras(planejamentoId) {
  return await fetchJson(`/planejamentos/${planejamentoId}/lista-compras`);
}

// Atualização tátil do status de compra (Item no carrinho)
export async function alternarStatusItem(itemId, comprado) {
  return await fetchJson(`/itens-lista/${itemId}/status`, {
    method: 'PATCH',
    body: JSON.stringify({ comprado: comprado ? 'S' : 'N' }),
  });
}
```

---

## 5. Diretrizes de Design e Usabilidade Mobile-First

* **Paleta de Cores Consciente:**
  * Verde Principal (Ação e Sustentabilidade): `#10B981` (Emerald 500)
  * Verde Escuro (Contraste e Textos): `#064E3B` (Emerald 900)
  * Fundo Neutro: `#F9FAFB` (Gray 50)
  * Superfície de Cards: `#FFFFFF`
  * Destaques de Alerta/Atenção: `#F59E0B` (Amber 500)
* **Tipografia e Contraste:** Fontes sem serifa (Inter ou Roboto) com tamanho mínimo de `16px` para textos de leitura e `18px` para itens da lista de compras.
* **Área de Toque (Touch Targets):** Mínimo de `48px x 48px` para botões e caixas de checagem, evitando toques acidentais durante a locomoção no mercado.

---

## 6. Guia de Execução Local

```bash
# 1. Instalar dependências
npm install

# 2. Iniciar servidor de desenvolvimento local
npm run dev

# A aplicação estará disponível em http://localhost:5173
```

---

## 7. Implementação do front-end e contrato utilizado

O front-end React/Vite também publica as entradas HTML `index.html` (início), `serviços.html` (receitas) e `planejamentos.html` (planejamento). Todas montam a mesma aplicação React; as rotas equivalentes `/`, `/dashboard`, `/receitas` e `/planejamento` permanecem disponíveis. O Vite usa `/api` como base padrão e encaminha essas chamadas para `http://localhost:8080`; em outro ambiente, configure `VITE_API_BASE_URL`, por exemplo `https://servidor/api`.

No desenvolvimento, `frontend/.env.development` ativa um simulador local das rotas e dados para testar navegação, cadastro, planejamento e checklist sem o servidor Java. A faixa “Modo demonstração” identifica claramente os dados de exemplo; alterações duram apenas até recarregar a aplicação e não são enviadas à API. Para testar a integração real, defina `VITE_DEMO_MODE=false` e inicie o back-end.

Serviços utilizados:

| Tela | Método e rota |
| :--- | :--- |
| Dashboard | `GET /api/receitas`, `GET /api/planejamentos` e `GET /api/planejamentos/{id}/desperdicio` |
| Receitas e catálogo | `GET/POST /api/receitas`, `GET/PUT/DELETE /api/receitas/{id}`, `GET /api/ingredientes` |
| Planejamento | `GET/POST /api/planejamentos`, `GET /api/planejamentos/{id}`, `GET /api/planejamentos/{id}/lista-compras` |
| Checklist | `PATCH /api/itens-lista/{id}/status` |

O PATCH recebe `{"comprado": true}` ou `{"comprado": false}`. Embora a coluna do banco use `S/N`, o controller Java desserializa um booleano JSON. A tela faz atualização otimista e reverte o checkbox se a chamada falhar. A lista consolidada e os indicadores de aproveitamento vêm do back-end; o navegador não calcula quantidades nem valores de economia.

### Dependência conhecida do back-end

O `PlanejamentoService` implementa uma atualização, mas o `PlanejamentoController` não expõe `PUT /api/planejamentos/{id}`. Por isso, a interface carrega planejamentos existentes, mas salvar alterações cria explicitamente um novo planejamento via POST; não simula uma edição persistida. Para editar o mesmo registro, o back-end precisa publicar e documentar o endpoint PUT (incluindo a sincronização das refeições e a lista de compras).
