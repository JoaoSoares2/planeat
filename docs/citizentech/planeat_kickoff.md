# GS 2026/2 - CitizenTech

Prazo da Sprint ativa: 04/11/2026 → 19/11/2026
Status: Sprint ativa
Tipo: Global Solution

<aside>
📡

**CitizenTech**

Engenharia de Software – 2º Ano – Global Solution 2

</aside>

# PDF

[https://drive.google.com/file/d/1Yp1fzSdbaqa0Q9-uCuqJr9VYDsZqM_Bk/view?usp=drive_link](https://drive.google.com/file/d/1Yp1fzSdbaqa0Q9-uCuqJr9VYDsZqM_Bk/view?usp=drive_link)

---

# **Global Solution -** CitizenTech

> Tecnologia com o cidadão no centro da inovação.
> 

<aside>
🚀

**Entregas**

1 entrega, sendo um MVP Java com banco + Vídeo pitch

- JAVA + JDBC → backend orientado a objetos com CRUD completo no banco
- BANCO → modelo relacional, diagrama ER e script .sql
- WEB → interface web para as funcionalidades principais
- VÍDEO PITCH → até 3 minutos
</aside>

## 📋 Regras do projeto

---

- Conteúdo até a fase 7
- Banco relacional → Oracle, MySQL, PostgreSQL, H2 ou outro relacional, acessado pelo Java via JDBC.
- Um domínio só → as entidades do Java são as tabelas do banco e as telas da web

---

**Back-end em Java com persistência**

- Diagrama de classes (UML)  Todas as classes com atributos, métodos e relacionamentos, batendo com o código
- POO de verdade  3+ entidades relacionadas, encapsuladas, com herança ou interface onde fizer sentido
- CRUD completo via JDBC  Inserir, listar, buscar, atualizar e excluir no banco para as entidades principais
- Camadas separadas  Model, DAO (acesso a dados), service (regras) e interface, cada um no seu pacote
- Tratamento de erros  Exceções de banco e entradas inválidas tratadas com mensagem clara, sem o sistema cair

---

**Modelagem do banco**

- Diagrama entidade-relacionamento → pelo menos 4 tabelas com PK, FK e cardinalidades, incluindo um relacionamento N:N com tabela associativa
- Script .sql → CREATE TABLE com PK, FK, NOT NULL e ao menos um UNIQUE ou CHECK, mais INSERTs de carga inicial

---

**Interface web**

- Navegação → pelo menos 3 páginas ligadas por um menu igual em todas, com HTML semântico
- Telas do CRUD → formulários e listagens para as entidades principais, com validação em JS e mensagens na página
- Responsivo e bem desenhado → funciona no celular e no desktop, com bom design visual e contraste legível.

---

**Vídeo Pitch**

- até 3 minutos → o que passar de 3 minutos não é avaliado.
- mostre o protótipo → nada de vídeo em formato de slides ou leitura de texto da tela: mostre o site funcionando.
- link público → youTube, Google Drive ou Vimeo, com acesso aberto. Teste o link numa aba anônima antes de entregar.
- onde vai o link → no .txt da entrega e no README do projeto.

## 📹 Como defender a solução em 3 minutos

0:00 → O problema
Quem é o cidadão e por que o problema importa. Um dado real ajuda.

0:30 → A solução
O que a CitizenTech faz e qual melhoria concreta ela entrega.

0:50 → Demonstração
Mostre o sistema gravando e lendo do banco e a interface web.

2:10 → Impacto
Quem ganha com a solução e quais ODS ela atende.

2:40 → Fechamento
Por que a sua proposta merece vencer.

Dicas: use o que viu nas lives de pitch e storytelling; o vídeo precisa deixar quem assiste com vontade de conhecer o projeto.

## 🏆 O que separa um bom projeto do vencedor

Web ligada ao backend → A interface web lê e grava os dados reais do banco, não só dados fixos no JS.

Relatórios com SQL → Consultas com JOIN e agregação que geram informações úteis dentro do sistema.

Repositório no GitHub → Código versionado com commits de todos os integrantes e README completo.

Validado com gente real → Conversaram com pessoas do público da solução e mostram o que mudou por causa disso.

Não somam pontos na nota, mas pesam na escolha do vencedor.

## ✅ Checklist do grupo

- [ ]  O projeto compila e roda seguindo só o README
- [ ]  O script .sql roda do início ao fim e o Java conecta no banco
- [ ]  Os diagramas de classes e ER estão em PDF ou imagem
- [ ]  O index.html abre direto no navegador e funciona no celular
- [ ]  O .txt tem nome do grupo, RM e nome de cada integrante e o link do pitch
- [ ]  Enviado na plataforma até 19/11, 23h55