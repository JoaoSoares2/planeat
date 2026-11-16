# 🌿 PlanEat — CitizenTech

> **FIAP — Global Solution 2026/2**  
> **Curso:** Engenharia de Software — 2º Ano  
> **Tema:** CitizenTech — Tecnologia com o cidadão no centro da inovação  
> **Objetivos de Desenvolvimento Sustentável:** ODS 2 e ODS 11  

## 📁 Estrutura do Projeto

```
planeat/
├── 🗄️ database/
│   └── script.sql                  # DDL (tabelas, constraints) e DML (inserts)
│
├── ☕ backend/
│   ├── pom.xml                     # Maven com Java 17, drivers JDBC (Oracle, Postgres, H2) e Gson
│   └── src/main/java/br/com/fiap/planeat/
│       ├── Main.java               # Ponto de entrada da aplicação
│       ├── factory/
│       │   └── ConnectionFactory.java       # Conexão JDBC
│       ├── model/                  # Entidades de domínio com herança de EntidadeBase
│       │   ├── EntidadeBase.java
│       │   ├── Usuario.java
│       │   ├── CategoriaAlimento.java
│       │   ├── Ingrediente.java
│       │   ├── ItemReceita.java
│       │   ├── Receita.java
│       │   ├── PlanejamentoSemanal.java
│       │   ├── ItemPlanejamento.java
│       │   └── ItemListaCompras.java
│       ├── dao/                    # Assinaturas dos métodos CRUD
│       │   ├── UsuarioDAO.java
│       │   ├── IngredienteDAO.java
│       │   ├── ReceitaDAO.java
│       │   └── PlanejamentoDAO.java
│       ├── service/                # Assinaturas dos serviços de negócio
│       │   ├── ReceitaService.java
│       │   └── ListaComprasService.java
│       └── exception/              # Hierarquia de exceções customizadas
│           ├── DatabasePersistenceException.java
│           ├── RegraNegocioException.java
│           └── EntidadeNaoEncontradaException.java
│
├── ⚛️ frontend/
│   ├── package.json                # Configuração React 18 + Vite
│   ├── vite.config.js              # Configuração do Vite e proxy
│   ├── index.html                  # Template base HTML5
│   └── src/
│       ├── main.jsx                # Ponto de montagem da SPA
│       ├── App.jsx                 # Estrutura base de navegação entre as 3 telas
│       ├── index.css               # Estilos base
│       ├── components/
│       │   ├── Navbar.jsx          # Componente de navegação
│       │   └── Footer.jsx          # Componente de rodapé
│       ├── pages/                  # Telas vazias com placeholders para desenvolvimento
│       │   ├── Dashboard.jsx
│       │   ├── Receitas.jsx
│       │   └── Planejamento.jsx
│       └── services/
│           └── api.js              # Funções base de integração HTTP
│
└── 📚 docs/                        # Guias e especificações de referência
    ├── planning.md                 # Planejamento mensal desacoplado (2 times paralelos)
    ├── spec.md                     # Visão geral, arquitetura e critérios da GS
    ├── banco.md                    # DER detalhado, tipos de dados e queries analíticas
    ├── backend.md                  # Diagrama de Classes UML e contratos REST
    └── frontend.md                 # Arquitetura e guia de interface React
```

---

## 🛠️ Como Iniciar o Desenvolvimento

### 1. Banco de Dados
* Consulte as tabelas e tipos especificados em [`docs/banco.md`](/docs/banco.md);
* Implemente os comandos DDL e DML em [`database/script.sql`](/database/script.sql).

### 2. Back-end Java
* Configure `DB_URL`, `DB_USER` e `DB_PASSWORD` (ou `DB_PASS`) no ambiente;
* Configure `CORS_ALLOWED_ORIGINS` como uma lista separada por vírgulas. O padrão permite apenas `http://localhost:5173` e `http://127.0.0.1:5173`;
* A sessão usa token aleatório opaco em cookie `HttpOnly`, `SameSite=Lax`, com validade de 8 horas e revogação no banco. Em produção, mantenha `COOKIE_SECURE=true` e sirva a aplicação por HTTPS; em HTTP local, configure `COOKIE_SECURE=false`;
* Para um banco existente, aplique uma vez [`database/migrations/001_email_case_insensitive.sql`](./database/migrations/001_email_case_insensitive.sql) e [`database/migrations/002_auth_sessions.sql`](./database/migrations/002_auth_sessions.sql). Resolva previamente eventuais e-mails duplicados ignorando maiúsculas/minúsculas;
* Senhas em texto puro de contas antigas são atualizadas para PBKDF2 no primeiro login bem-sucedido. Cadastros novos usam PBKDF2-HMAC-SHA256 com salt individual;
* O script [`database/script.sql`](./database/script.sql) recria as tabelas: use-o somente em uma instalação nova, nunca para atualizar uma base existente. Ele carrega apenas o catálogo global; receitas e planejamentos são dados privados criados após cadastro;
* Para compilar e testar:
  ```bash
  cd backend
  mvn clean test
  ```
* Para iniciar o servidor (após configurar o banco e aplicar o schema/migrações):
  ```powershell
  $env:COOKIE_SECURE = "false"
  mvn exec:java
  ```

### 3. Front-end React
* Configure `VITE_API_BASE_URL` somente se a API não estiver disponível pelo proxy `/api` do Vite;
* Para rodar o ambiente de desenvolvimento:
  ```bash
  cd frontend
  npm install
  npm run dev
  ```
* Em hospedagens estáticas, configure fallback/rewrite das rotas da SPA (`/login`, `/cadastro`, `/dashboard`, `/receitas` e `/planejamento`) para `index.html`.

### Autenticação

* `POST /api/auth/cadastro` — cria uma conta; senha mínima de 8 caracteres;
* `POST /api/auth/login` — valida as credenciais e define o cookie HttpOnly;
* `GET /api/auth/sessao` — valida a sessão no servidor e retorna o usuário autenticado;
* `POST /api/auth/logout` — encerra a sessão no navegador.

Todos os endpoints de receitas, planejamentos, listas, ingredientes e categorias exigem uma sessão válida. O proprietário dos dados é obtido exclusivamente da sessão validada no Java; IDs de usuário enviados pelo cliente são ignorados. Contas antigas com hash incompatível precisam redefinir a senha antes do primeiro acesso.

As requisições que alteram dados também exigem o cabeçalho `X-Requested-With: XMLHttpRequest`; a origem do navegador deve estar em `CORS_ALLOWED_ORIGINS`.
